package com.bitchat.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.campus.CampusZone
import com.bitchat.android.ui.theme.VRantColors

/**
 * Nearby tab — shows mesh peers with VIT-themed cards and zone context
 */
@Composable
fun NearbyScreen(
    connectedPeers: List<String>,
    currentZone: CampusZone?,
    isOnCampus: Boolean,
    isScanning: Boolean,
    onPeerClick: (String) -> Unit,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Scanning animation
    val scanAnim = rememberInfiniteTransition(label = "scan")
    val scanAlpha by scanAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VRantColors.DarkBase)
            .padding(horizontal = 16.dp)
    ) {
        // Zone banner
        if (isOnCampus && currentZone != null) {
            ZoneBanner(zone = currentZone)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scanning status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isScanning) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VRantColors.MeshBlue.copy(alpha = scanAlpha)
                    )
                }
                Text(
                    text = if (isScanning) "Scanning campus mesh..." else "Mesh scan paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isScanning) VRantColors.MeshBlue else VRantColors.TextMuted
                )
            }

            Text(
                text = "${connectedPeers.size} VITians",
                style = MaterialTheme.typography.labelMedium,
                color = VRantColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        if (connectedPeers.isEmpty()) {
            // Empty state
            EmptyNearbyState(
                isScanning = isScanning,
                onStartScan = onStartScan
            )
        } else {
            // Peer list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(connectedPeers, key = { it }) { peer ->
                    PeerCard(
                        peerName = peer,
                        onClick = { onPeerClick(peer) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneBanner(zone: CampusZone) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = zone.color.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = zone.emoji, fontSize = 28.sp)
                Column {
                    Text(
                        text = zone.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = zone.color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "You're in this zone",
                        style = MaterialTheme.typography.labelSmall,
                        color = VRantColors.TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(VRantColors.OnlineGreen)
            )
        }
    }
}

@Composable
private fun PeerCard(
    peerName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = VRantColors.DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    VRantColors.Maroon500,
                                    VRantColors.Maroon700
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peerName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = peerName,
                        style = MaterialTheme.typography.titleSmall,
                        color = VRantColors.TextPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = VRantColors.MeshBlue
                        )
                        Text(
                            text = "via mesh",
                            style = MaterialTheme.typography.labelSmall,
                            color = VRantColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // E2EE indicator
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    modifier = Modifier.size(14.dp),
                    tint = VRantColors.EncryptedOrange
                )

                // Chat button
                FilledTonalButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = VRantColors.Maroon500.copy(alpha = 0.2f),
                        contentColor = VRantColors.Maroon400
                    )
                ) {
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNearbyState(
    isScanning: Boolean,
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Outlined.WifiFind,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = VRantColors.TextMuted
        )

        Text(
            text = "No VITians nearby",
            style = MaterialTheme.typography.titleMedium,
            color = VRantColors.TextSecondary
        )

        Text(
            text = if (isScanning)
                "Keep Bluetooth enabled and stay close to other VRant users"
            else
                "Enable Bluetooth to discover nearby VITians via mesh",
            style = MaterialTheme.typography.bodySmall,
            color = VRantColors.TextMuted,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        if (!isScanning) {
            Button(
                onClick = onStartScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VRantColors.Maroon500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Scanning")
            }
        }
    }
}
