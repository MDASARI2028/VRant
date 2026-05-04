package com.bitchat.android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.ui.theme.VRantColors

/**
 * VRant bottom navigation tab definitions
 */
enum class VRantTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
) {
    NEARBY(
        title = "Nearby",
        selectedIcon = Icons.Filled.Sensors,
        unselectedIcon = Icons.Outlined.Sensors,
        contentDescription = "Nearby VITians"
    ),
    MAP(
        title = "Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        contentDescription = "Campus Map"
    ),
    CHATS(
        title = "Chats",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        contentDescription = "Conversations"
    ),
    COMMUNITY(
        title = "Community",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups,
        contentDescription = "Campus Communities"
    )
}

/**
 * Custom animated bottom navigation bar for VRant.
 * Maroon accent on selected tab with smooth transitions.
 */
@Composable
fun VRantBottomBar(
    currentTab: VRantTab,
    onTabSelected: (VRantTab) -> Unit,
    unreadChatCount: Int = 0,
    nearbyCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = VRantColors.DarkBase,
        tonalElevation = 0.dp
    ) {
        // Top border accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            VRantColors.Maroon500.copy(alpha = 0.4f),
                            VRantColors.Gold500.copy(alpha = 0.3f),
                            VRantColors.Maroon500.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        VRantTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            val badgeCount = when (tab) {
                VRantTab.CHATS -> unreadChatCount
                VRantTab.NEARBY -> nearbyCount
                else -> 0
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.contentDescription,
                            modifier = Modifier.size(24.dp)
                        )

                        // Badge
                        if (badgeCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-4).dp),
                                containerColor = VRantColors.Maroon500,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (badgeCount > 99) "99+" else "$badgeCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VRantColors.Maroon500,
                    selectedTextColor = VRantColors.Maroon500,
                    unselectedIconColor = VRantColors.NavUnselected,
                    unselectedTextColor = VRantColors.NavUnselected,
                    indicatorColor = VRantColors.NavIndicator
                )
            )
        }
    }
}

/**
 * VRant top app bar with branding and connection status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VRantTopBar(
    currentTab: VRantTab,
    isConnected: Boolean,
    peerCount: Int,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VRantColors.DarkBase,
            titleContentColor = VRantColors.TextPrimary,
            actionIconContentColor = VRantColors.TextSecondary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Brand name
                Text(
                    text = "VRant",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VRantColors.Maroon500,
                    fontWeight = FontWeight.Bold
                )

                // Connection status
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(
                                if (isConnected) VRantColors.OnlineGreen.copy(alpha = 0.1f)
                                else VRantColors.OfflineGray.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) VRantColors.OnlineGreen
                                    else VRantColors.OfflineGray
                                )
                        )
                        Text(
                            text = if (isConnected) "$peerCount nearby" else "offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) VRantColors.OnlineGreen
                            else VRantColors.OfflineGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        actions = {
            // Settings
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Profile avatar placeholder
            IconButton(onClick = onProfileClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VRantColors.Maroon500, VRantColors.Gold500)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    )
}
