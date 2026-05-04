package com.bitchat.android.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.ui.theme.VRantColors

/**
 * Communities tab — campus-level group channels for departments, clubs, events.
 * Full implementation with community categories and join functionality.
 */

data class CommunityItem(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val memberCount: Int,
    val category: CommunityCategory,
    val color: Color,
    val isJoined: Boolean = false
)

enum class CommunityCategory(val displayName: String) {
    DEPARTMENT("Departments"),
    CLUB("Clubs"),
    EVENT("Events"),
    GENERAL("General")
}

@Composable
fun CommunitiesScreen(
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<CommunityCategory?>(null) }

    // Sample community data
    val communities = remember {
        listOf(
            // Departments
            CommunityItem("cse", "CSE Batch", "Computer Science & Engineering", "💻", 342, CommunityCategory.DEPARTMENT, VRantColors.MeshBlue),
            CommunityItem("ece", "ECE Batch", "Electronics & Communication", "📡", 228, CommunityCategory.DEPARTMENT, VRantColors.ZoneSports),
            CommunityItem("mech", "MECH Batch", "Mechanical Engineering", "⚙️", 195, CommunityCategory.DEPARTMENT, VRantColors.EncryptedOrange),
            CommunityItem("eee", "EEE Batch", "Electrical & Electronics", "⚡", 167, CommunityCategory.DEPARTMENT, VRantColors.Gold500),
            CommunityItem("civil", "Civil Batch", "Civil Engineering", "🏗️", 134, CommunityCategory.DEPARTMENT, VRantColors.ZoneHostel),
            // Clubs
            CommunityItem("acm", "ACM VIT", "Association for Computing Machinery", "🖥️", 89, CommunityCategory.CLUB, VRantColors.Maroon500),
            CommunityItem("ieee", "IEEE VIT", "Institute of Electrical Engineers", "🔬", 76, CommunityCategory.CLUB, VRantColors.MeshBlue),
            CommunityItem("gdsc", "GDSC VIT", "Google Developer Student Club", "🚀", 156, CommunityCategory.CLUB, VRantColors.OnlineGreen),
            CommunityItem("music", "VIT Music Club", "Harmonix - Music Society", "🎵", 112, CommunityCategory.CLUB, VRantColors.ZoneHostel),
            CommunityItem("sports", "VIT Sports", "Sports Federation", "🏆", 201, CommunityCategory.CLUB, VRantColors.ZoneSports),
            // Events
            CommunityItem("riviera", "Riviera 2026", "Annual Cultural & Tech Fest", "🎭", 458, CommunityCategory.EVENT, VRantColors.Maroon400),
            CommunityItem("gravitas", "GraVITas 2026", "Technical Fest", "🔧", 389, CommunityCategory.EVENT, VRantColors.Gold500),
            // General
            CommunityItem("memes", "VIT Memes", "Campus humor & memes", "😂", 567, CommunityCategory.GENERAL, VRantColors.EncryptedOrange),
            CommunityItem("food", "Foodie VIT", "Food reviews & recommendations", "🍕", 298, CommunityCategory.GENERAL, VRantColors.ZoneCafeteria),
            CommunityItem("placement", "Placement Talk", "Placement prep & discussions", "📊", 412, CommunityCategory.GENERAL, VRantColors.OnlineGreen)
        )
    }

    val filteredCommunities = if (selectedCategory != null) {
        communities.filter { it.category == selectedCategory }
    } else {
        communities
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VRantColors.DarkBase)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = "Campus Communities",
            style = MaterialTheme.typography.headlineMedium,
            color = VRantColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "Join VIT communities to connect with your peers",
            style = MaterialTheme.typography.labelSmall,
            color = VRantColors.TextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VRantColors.Maroon500.copy(alpha = 0.2f),
                    selectedLabelColor = VRantColors.Maroon400
                )
            )
            CommunityCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(category.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VRantColors.Maroon500.copy(alpha = 0.2f),
                        selectedLabelColor = VRantColors.Maroon400
                    )
                )
            }
        }

        // Communities list
        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredCommunities.size) { index ->
                CommunityCard(community = filteredCommunities[index])
            }
        }
    }
}

@Composable
private fun CommunityCard(community: CommunityItem) {
    var isJoined by remember { mutableStateOf(community.isJoined) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Community icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(community.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = community.emoji, fontSize = 22.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VRantColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = VRantColors.TextMuted,
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = VRantColors.TextMuted
                        )
                        Text(
                            text = "${community.memberCount} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = VRantColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Join button
            AnimatedContent(
                targetState = isJoined,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "joinButton"
            ) { joined ->
                if (joined) {
                    OutlinedButton(
                        onClick = { isJoined = false },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VRantColors.OnlineGreen
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Joined", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { isJoined = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = community.color,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Join", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
