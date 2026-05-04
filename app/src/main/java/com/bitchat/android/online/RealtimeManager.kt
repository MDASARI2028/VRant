package com.bitchat.android.online

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * WebSocket-based realtime manager for online campus features.
 * Subscribes to channel updates, presence, and community activity.
 *
 * Compatible with Supabase Realtime or any WebSocket backend.
 */
class RealtimeManager {

    companion object {
        private const val TAG = "RealtimeManager"

        // TODO: Replace with actual WebSocket URL
        private const val WS_URL = "wss://your-supabase-project.supabase.co/realtime/v1/websocket"

        @Volatile
        private var instance: RealtimeManager? = null

        fun getInstance(): RealtimeManager {
            return instance ?: synchronized(this) {
                instance ?: RealtimeManager().also { instance = it }
            }
        }
    }

    private val gson: Gson = GsonBuilder().create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var webSocket: WebSocket? = null
    private var authToken: String? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** Flow of incoming realtime messages */
    private val _incomingMessages = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 100)
    val incomingMessages: SharedFlow<RealtimeEvent> = _incomingMessages.asSharedFlow()

    /** Flow of presence updates */
    private val _presenceUpdates = MutableSharedFlow<PresenceUpdate>(extraBufferCapacity = 50)
    val presenceUpdates: SharedFlow<PresenceUpdate> = _presenceUpdates.asSharedFlow()

    /** Currently subscribed channels */
    private val subscribedChannels = mutableSetOf<String>()

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    /**
     * Connect to the realtime server
     */
    fun connect(token: String) {
        authToken = token
        reconnectAttempts = 0
        doConnect()
    }

    /**
     * Disconnect from realtime server
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        subscribedChannels.clear()
        reconnectAttempts = 0
    }

    /**
     * Subscribe to a channel for realtime updates
     */
    fun subscribe(channelId: String) {
        if (subscribedChannels.add(channelId)) {
            val payload = mapOf(
                "event" to "phx_join",
                "topic" to "realtime:$channelId",
                "payload" to mapOf<String, Any>(),
                "ref" to System.currentTimeMillis().toString()
            )
            sendMessage(payload)
            Log.d(TAG, "📡 Subscribed to channel: $channelId")
        }
    }

    /**
     * Unsubscribe from a channel
     */
    fun unsubscribe(channelId: String) {
        if (subscribedChannels.remove(channelId)) {
            val payload = mapOf(
                "event" to "phx_leave",
                "topic" to "realtime:$channelId",
                "payload" to mapOf<String, Any>(),
                "ref" to System.currentTimeMillis().toString()
            )
            sendMessage(payload)
            Log.d(TAG, "📡 Unsubscribed from channel: $channelId")
        }
    }

    /**
     * Send a realtime message (e.g., typing indicator, presence update)
     */
    fun sendRealtimeEvent(channelId: String, event: String, payload: Map<String, Any>) {
        val message = mapOf(
            "event" to event,
            "topic" to "realtime:$channelId",
            "payload" to payload,
            "ref" to System.currentTimeMillis().toString()
        )
        sendMessage(message)
    }

    /**
     * Update presence in a zone
     */
    fun updateZonePresence(zoneId: String, nickname: String) {
        sendRealtimeEvent(
            "zone:$zoneId",
            "presence",
            mapOf(
                "zone_id" to zoneId,
                "nickname" to nickname,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    // ── Private ──

    private fun doConnect() {
        _connectionState.value = ConnectionState.CONNECTING

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("$WS_URL?apikey=$authToken&vsn=1.0.0")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempts = 0
                Log.d(TAG, "✅ WebSocket connected")

                // Start heartbeat
                startHeartbeat()

                // Re-subscribe to all channels
                subscribedChannels.toList().forEach { channel ->
                    subscribe(channel)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "⚠️ WebSocket closing: $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                Log.d(TAG, "🔌 WebSocket closed: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WebSocket failure: ${t.message}")
                _connectionState.value = ConnectionState.DISCONNECTED
                attemptReconnect()
            }
        })
    }

    private fun handleMessage(text: String) {
        try {
            val json = gson.fromJson(text, Map::class.java)
            val event = json["event"] as? String ?: return
            val topic = json["topic"] as? String ?: return
            @Suppress("UNCHECKED_CAST")
            val payload = json["payload"] as? Map<String, Any> ?: emptyMap()

            when (event) {
                "INSERT", "UPDATE", "DELETE" -> {
                    val channelId = topic.removePrefix("realtime:")
                    scope.launch {
                        _incomingMessages.emit(
                            RealtimeEvent(
                                channelId = channelId,
                                event = event,
                                payload = payload
                            )
                        )
                    }
                }
                "presence_state", "presence_diff" -> {
                    val channelId = topic.removePrefix("realtime:")
                    scope.launch {
                        _presenceUpdates.emit(
                            PresenceUpdate(
                                channelId = channelId,
                                event = event,
                                users = payload
                            )
                        )
                    }
                }
                "phx_reply" -> {
                    // Phoenix reply - connection confirmation
                    Log.d(TAG, "📨 Reply for $topic: ${payload["status"]}")
                }
                "heartbeat" -> {
                    // Heartbeat response — no-op
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse realtime message: ${e.message}")
        }
    }

    private fun sendMessage(payload: Any) {
        val json = gson.toJson(payload)
        webSocket?.send(json) ?: Log.w(TAG, "⚠️ Cannot send — WebSocket not connected")
    }

    private fun startHeartbeat() {
        scope.launch {
            while (_connectionState.value == ConnectionState.CONNECTED) {
                delay(30_000) // 30 second heartbeat
                val heartbeat = mapOf(
                    "event" to "heartbeat",
                    "topic" to "phoenix",
                    "payload" to mapOf<String, Any>(),
                    "ref" to System.currentTimeMillis().toString()
                )
                sendMessage(heartbeat)
            }
        }
    }

    private fun attemptReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.e(TAG, "❌ Max reconnect attempts reached")
            return
        }

        _connectionState.value = ConnectionState.RECONNECTING
        reconnectAttempts++

        val delay = minOf(1000L * (1 shl reconnectAttempts), 30_000L) // Exponential backoff, max 30s

        scope.launch {
            Log.d(TAG, "🔄 Reconnecting in ${delay}ms (attempt $reconnectAttempts)")
            delay(delay)
            doConnect()
        }
    }

    fun shutdown() {
        disconnect()
        scope.cancel()
    }
}

data class RealtimeEvent(
    val channelId: String,
    val event: String,
    val payload: Map<String, Any>
)

data class PresenceUpdate(
    val channelId: String,
    val event: String,
    val users: Map<String, Any>
)
