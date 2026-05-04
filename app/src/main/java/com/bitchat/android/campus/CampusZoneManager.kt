package com.bitchat.android.campus

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages campus zone detection and transitions.
 * Integrates with the existing LocationChannelManager to provide
 * campus-specific zone awareness on top of the geohash system.
 */
class CampusZoneManager(private val context: Context) {

    companion object {
        private const val TAG = "CampusZoneManager"

        @Volatile
        private var instance: CampusZoneManager? = null

        fun getInstance(context: Context): CampusZoneManager {
            return instance ?: synchronized(this) {
                instance ?: CampusZoneManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /** Currently detected zone */
    private val _currentZone = MutableStateFlow<CampusZone?>(null)
    val currentZone: StateFlow<CampusZone?> = _currentZone.asStateFlow()

    /** Whether user is on campus */
    private val _isOnCampus = MutableStateFlow(false)
    val isOnCampus: StateFlow<Boolean> = _isOnCampus.asStateFlow()

    /** Active users per zone (from mesh peer discovery) */
    private val _zonePeerCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val zonePeerCounts: StateFlow<Map<String, Int>> = _zonePeerCounts.asStateFlow()

    /** All zones with their active status */
    private val _activeZones = MutableStateFlow<List<ZoneStatus>>(emptyList())
    val activeZones: StateFlow<List<ZoneStatus>> = _activeZones.asStateFlow()

    /** Zone transition history for this session */
    private val transitionHistory = mutableListOf<ZoneTransition>()

    data class ZoneStatus(
        val zone: CampusZone,
        val activeUsers: Int,
        val isCurrentZone: Boolean
    )

    data class ZoneTransition(
        val fromZone: CampusZone?,
        val toZone: CampusZone?,
        val timestamp: Long
    )

    /**
     * Update the user's position and detect zone transitions.
     * Call this whenever a location update is received.
     */
    fun updateLocation(lat: Double, lng: Double) {
        val wasOnCampus = _isOnCampus.value
        val previousZone = _currentZone.value

        _isOnCampus.value = VITCampusZones.isOnCampus(lat, lng)

        if (_isOnCampus.value) {
            val newZone = VITCampusZones.findNearestZone(lat, lng)
            _currentZone.value = newZone

            if (newZone?.id != previousZone?.id) {
                onZoneTransition(previousZone, newZone)
            }
        } else {
            _currentZone.value = null
            if (wasOnCampus) {
                onZoneTransition(previousZone, null)
            }
        }

        refreshActiveZones()
    }

    /**
     * Update peer counts per zone (called from mesh service when peers report their zone)
     */
    fun updateZonePeerCount(zoneId: String, count: Int) {
        val updated = _zonePeerCounts.value.toMutableMap()
        if (count > 0) {
            updated[zoneId] = count
        } else {
            updated.remove(zoneId)
        }
        _zonePeerCounts.value = updated
        refreshActiveZones()
    }

    /**
     * Bulk update from mesh discovery
     */
    fun updateAllZonePeerCounts(counts: Map<String, Int>) {
        _zonePeerCounts.value = counts
        refreshActiveZones()
    }

    /**
     * Get the chat channel name for a zone (for mesh integration)
     */
    fun getZoneChannelName(zone: CampusZone): String = "vit_${zone.id.lowercase()}"

    /**
     * Get all zone channel names
     */
    fun getAllZoneChannelNames(): List<String> =
        VITCampusZones.ALL_ZONES.map { getZoneChannelName(it) }

    /**
     * Get zone from channel name
     */
    fun getZoneFromChannelName(channelName: String): CampusZone? {
        val zoneId = channelName.removePrefix("vit_").uppercase()
        return VITCampusZones.getZoneById("VIT_$zoneId")
    }

    /**
     * Get transition history
     */
    fun getTransitionHistory(): List<ZoneTransition> = transitionHistory.toList()

    /**
     * Get total campus peer count
     */
    fun getTotalCampusPeerCount(): Int = _zonePeerCounts.value.values.sum()

    // ── Private ──

    private fun onZoneTransition(from: CampusZone?, to: CampusZone?) {
        val transition = ZoneTransition(from, to, System.currentTimeMillis())
        transitionHistory.add(transition)

        if (transitionHistory.size > 50) {
            transitionHistory.removeAt(0)
        }

        when {
            from == null && to != null -> {
                Log.d(TAG, "📍 Entered zone: ${to.displayName} ${to.emoji}")
            }
            from != null && to == null -> {
                Log.d(TAG, "📍 Left zone: ${from.displayName}")
            }
            from != null && to != null -> {
                Log.d(TAG, "📍 Zone transition: ${from.shortName} → ${to.shortName}")
            }
        }
    }

    private fun refreshActiveZones() {
        val currentZoneId = _currentZone.value?.id
        val counts = _zonePeerCounts.value

        _activeZones.value = VITCampusZones.ALL_ZONES.map { zone ->
            ZoneStatus(
                zone = zone,
                activeUsers = counts[zone.id] ?: 0,
                isCurrentZone = zone.id == currentZoneId
            )
        }
    }
}
