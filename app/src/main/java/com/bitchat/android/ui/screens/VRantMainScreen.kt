package com.bitchat.android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.campus.CampusZoneManager
import com.bitchat.android.campus.VITAuthManager
import com.bitchat.android.ui.ChatScreen
import com.bitchat.android.ui.ChatViewModel
import com.bitchat.android.ui.navigation.VRantBottomBar
import com.bitchat.android.ui.navigation.VRantTab
import com.bitchat.android.ui.navigation.VRantTopBar
import kotlinx.coroutines.launch

@Composable
fun VRantMainScreen(
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Managers
    val authManager = remember { VITAuthManager.getInstance(context) }
    val zoneManager = remember { CampusZoneManager.getInstance(context) }
    
    // State
    var currentTab by remember { mutableStateOf(VRantTab.NEARBY) }
    var showProfileSheet by remember { mutableStateOf(false) }
    
    // Collected Data
    val profile by authManager.profile.collectAsStateWithLifecycle()
    val currentZone by zoneManager.currentZone.collectAsStateWithLifecycle()
    val isOnCampus by zoneManager.isOnCampus.collectAsStateWithLifecycle()
    val zonePeerCounts by zoneManager.zonePeerCounts.collectAsStateWithLifecycle()
    
    // Extracted chat states for Top/Bottom bars
    val connectedPeers by chatViewModel.connectedPeers.collectAsStateWithLifecycle()
    val isScanning = false // Handled by background service
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            VRantTopBar(
                currentTab = currentTab,
                isConnected = connectedPeers.isNotEmpty(),
                peerCount = connectedPeers.size,
                onProfileClick = { showProfileSheet = true },
                onSettingsClick = { /* TODO: Open settings */ }
            )
        },
        bottomBar = {
            VRantBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                unreadChatCount = 0, // TODO: Hook up to chat unread count
                nearbyCount = connectedPeers.size
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                VRantTab.NEARBY -> {
                    NearbyScreen(
                        connectedPeers = connectedPeers,
                        currentZone = currentZone,
                        isOnCampus = isOnCampus,
                        isScanning = isScanning,
                        onPeerClick = { peerId ->
                            chatViewModel.showPrivateChatSheet(peerId)
                            // If they tap a peer, keep them on nearby or optionally switch to chat
                        },
                        onStartScan = { /* Handled automatically by foreground service in this app */ }
                    )
                }
                VRantTab.MAP -> {
                    MapScreen(
                        currentZone = currentZone,
                        zonePeerCounts = zonePeerCounts,
                        isOnCampus = isOnCampus,
                        onZoneTapped = { zone ->
                            // TODO: Switch to chat tab and open zone channel
                            currentTab = VRantTab.CHATS
                        }
                    )
                }
                VRantTab.CHATS -> {
                    // Reuse the existing robust ChatScreen, just dropping it into the tab
                    ChatScreen(viewModel = chatViewModel)
                }
                VRantTab.COMMUNITY -> {
                    CommunitiesScreen()
                }
            }
        }
    }

    // Profile Editor Overlay
    if (showProfileSheet) {
        ProfileScreen(
            profile = profile,
            onProfileUpdate = { authManager.updateProfile(it) },
            onVerifyEmail = { authManager.verifyEmail(it) },
            onBack = { showProfileSheet = false }
        )
    }
}
