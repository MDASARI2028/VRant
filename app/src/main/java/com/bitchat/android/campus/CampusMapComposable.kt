package com.bitchat.android.campus

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.ui.theme.VRantColors
import kotlin.math.*

/**
 * Custom Compose Canvas-based VIT campus map.
 * Renders campus zones as interactive overlays with peer counts and animations.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun CampusMapView(
    currentZone: CampusZone?,
    zonePeerCounts: Map<String, Int>,
    isOnCampus: Boolean,
    onZoneTapped: (CampusZone) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedZone by remember { mutableStateOf<CampusZone?>(null) }

    // Pulse animation for current zone
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by pulseAnim.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Glow animation for active zones
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(VRantColors.DarkBase)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Find tapped zone
                        val tapped = findTappedZone(
                            tapOffset, scale, offsetX, offsetY,
                            size.width.toFloat(), size.height.toFloat()
                        )
                        tapped?.let {
                            selectedZone = it
                            onZoneTapped(it)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw grid background
            drawGrid(canvasWidth, canvasHeight, scale, offsetX, offsetY)

            // Draw campus boundary
            drawCampusBoundary(canvasWidth, canvasHeight, scale, offsetX, offsetY)

            // Draw road network
            drawCampusRoads(canvasWidth, canvasHeight, scale, offsetX, offsetY)

            // Draw zones
            VITCampusZones.ALL_ZONES.forEach { zone ->
                val pos = geoToScreen(
                    zone.centerLat, zone.centerLng,
                    canvasWidth, canvasHeight, scale, offsetX, offsetY
                )
                val zoneRadius = (zone.radiusMeters / 3.0 * scale).toFloat()
                val peerCount = zonePeerCounts[zone.id] ?: 0
                val isCurrentZone = zone.id == currentZone?.id
                val isSelected = zone.id == selectedZone?.id

                // Pulse effect for current zone
                if (isCurrentZone) {
                    drawCircle(
                        color = zone.color.copy(alpha = pulseAlpha),
                        radius = zoneRadius * pulseRadius,
                        center = pos
                    )
                }

                // Glow for zones with active peers
                if (peerCount > 0 && !isCurrentZone) {
                    drawCircle(
                        color = zone.color.copy(alpha = glowAlpha * 0.3f),
                        radius = zoneRadius * 1.2f,
                        center = pos
                    )
                }

                // Zone fill
                drawCircle(
                    color = zone.color.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = zoneRadius,
                    center = pos
                )

                // Zone border
                drawCircle(
                    color = zone.color.copy(alpha = if (isSelected || isCurrentZone) 0.9f else 0.5f),
                    radius = zoneRadius,
                    center = pos,
                    style = Stroke(
                        width = if (isSelected || isCurrentZone) 3f else 1.5f,
                        pathEffect = if (!isCurrentZone && !isSelected)
                            PathEffect.dashPathEffect(floatArrayOf(8f, 4f)) else null
                    )
                )

                // Zone label
                if (scale > 0.7f) {
                    val labelText = if (peerCount > 0) {
                        "${zone.emoji} ${zone.shortName}\n${peerCount}👥"
                    } else {
                        "${zone.emoji} ${zone.shortName}"
                    }

                    val textLayout = textMeasurer.measure(
                        text = AnnotatedString(labelText),
                        style = TextStyle(
                            color = if (isCurrentZone) Color.White else zone.color,
                            fontSize = (11 * scale).sp,
                            fontWeight = if (isCurrentZone) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            pos.x - textLayout.size.width / 2,
                            pos.y - textLayout.size.height / 2
                        )
                    )
                }

                // Current location marker
                if (isCurrentZone) {
                    drawCircle(
                        color = VRantColors.OnlineGreen,
                        radius = 6f * scale,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f * scale,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }
            }

            // "You Are Here" indicator if on campus but not in a zone
            if (isOnCampus && currentZone == null) {
                val centerPos = geoToScreen(
                    VITCampusZones.CAMPUS_CENTER_LAT,
                    VITCampusZones.CAMPUS_CENTER_LNG,
                    canvasWidth, canvasHeight, scale, offsetX, offsetY
                )
                drawCircle(
                    color = VRantColors.OnlineGreen.copy(alpha = pulseAlpha),
                    radius = 20f * scale * pulseRadius,
                    center = centerPos
                )
                drawCircle(
                    color = VRantColors.OnlineGreen,
                    radius = 8f * scale,
                    center = centerPos
                )
            }
        }

        // Map controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom controls
            FloatingActionButton(
                onClick = { scale = (scale * 1.3f).coerceAtMost(3f) },
                modifier = Modifier.size(40.dp),
                containerColor = VRantColors.DarkElevated.copy(alpha = 0.9f),
                contentColor = VRantColors.TextPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { scale = (scale / 1.3f).coerceAtLeast(0.5f) },
                modifier = Modifier.size(40.dp),
                containerColor = VRantColors.DarkElevated.copy(alpha = 0.9f),
                contentColor = VRantColors.TextPrimary
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { scale = 1f; offsetX = 0f; offsetY = 0f },
                modifier = Modifier.size(40.dp),
                containerColor = VRantColors.DarkElevated.copy(alpha = 0.9f),
                contentColor = VRantColors.TextPrimary
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset view", modifier = Modifier.size(20.dp))
            }
        }

        // Campus status indicator
        if (isOnCampus) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        VRantColors.DarkElevated.copy(alpha = 0.9f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VRantColors.OnlineGreen)
                )
                Text(
                    text = currentZone?.let { "📍 ${it.displayName}" } ?: "📍 On Campus",
                    style = MaterialTheme.typography.labelSmall,
                    color = VRantColors.TextPrimary
                )
            }
        }

        // Zone info card when selected
        selectedZone?.let { zone ->
            ZoneInfoCard(
                zone = zone,
                peerCount = zonePeerCounts[zone.id] ?: 0,
                isCurrentZone = zone.id == currentZone?.id,
                onDismiss = { selectedZone = null },
                onJoinChat = { onZoneTapped(zone) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        // Legend
        if (scale > 0.8f) {
            MapLegend(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ZoneInfoCard(
    zone: CampusZone,
    peerCount: Int,
    isCurrentZone: Boolean,
    onDismiss: () -> Unit,
    onJoinChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = VRantColors.DarkElevated.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = zone.emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = zone.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = zone.color
                        )
                        Text(
                            text = zone.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = VRantColors.TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = VRantColors.TextMuted
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentZone) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VRantColors.OnlineGreen)
                        )
                        Text(
                            text = "You're here",
                            style = MaterialTheme.typography.labelSmall,
                            color = VRantColors.OnlineGreen
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = VRantColors.TextSecondary
                    )
                    Text(
                        text = "$peerCount active",
                        style = MaterialTheme.typography.labelSmall,
                        color = VRantColors.TextSecondary
                    )
                }
            }

            Button(
                onClick = onJoinChat,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = zone.color,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Join ${zone.shortName} Chat",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                VRantColors.DarkElevated.copy(alpha = 0.85f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Legend",
            style = MaterialTheme.typography.labelSmall,
            color = VRantColors.TextMuted
        )
        ZoneCategory.entries.take(5).forEach { category ->
            val color = when (category) {
                ZoneCategory.ACADEMIC -> VRantColors.ZoneAcademic
                ZoneCategory.HOSTEL -> VRantColors.ZoneHostel
                ZoneCategory.FOOD -> VRantColors.ZoneCafeteria
                ZoneCategory.LIBRARY -> VRantColors.ZoneLibrary
                ZoneCategory.SPORTS -> VRantColors.ZoneSports
                else -> VRantColors.ZoneDefault
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = "${category.emoji} ${category.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VRantColors.TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ── Drawing Helpers ──

private fun DrawScope.drawGrid(
    width: Float, height: Float, scale: Float, ox: Float, oy: Float
) {
    val gridSpacing = 40f * scale
    val gridColor = VRantColors.DarkBorder.copy(alpha = 0.15f)

    var x = (ox % gridSpacing)
    while (x < width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 0.5f)
        x += gridSpacing
    }
    var y = (oy % gridSpacing)
    while (y < height) {
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 0.5f)
        y += gridSpacing
    }
}

private fun DrawScope.drawCampusBoundary(
    width: Float, height: Float, scale: Float, ox: Float, oy: Float
) {
    val center = geoToScreen(
        VITCampusZones.CAMPUS_CENTER_LAT,
        VITCampusZones.CAMPUS_CENTER_LNG,
        width, height, scale, ox, oy
    )
    val radius = (VITCampusZones.CAMPUS_RADIUS_METERS / 3.0 * scale).toFloat()

    drawCircle(
        color = VRantColors.Maroon500.copy(alpha = 0.08f),
        radius = radius,
        center = center
    )
    drawCircle(
        color = VRantColors.Maroon500.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
        )
    )
}

private fun DrawScope.drawCampusRoads(
    width: Float, height: Float, scale: Float, ox: Float, oy: Float
) {
    val roadColor = VRantColors.DarkMuted.copy(alpha = 0.25f)
    val roadWidth = 2f * scale

    // Main campus road (approximate line from west to east)
    val roadPoints = listOf(
        12.9680 to 79.1530,
        12.9695 to 79.1555,
        12.9700 to 79.1570,
        12.9710 to 79.1590,
        12.9720 to 79.1610
    )

    for (i in 0 until roadPoints.size - 1) {
        val from = geoToScreen(roadPoints[i].first, roadPoints[i].second, width, height, scale, ox, oy)
        val to = geoToScreen(roadPoints[i + 1].first, roadPoints[i + 1].second, width, height, scale, ox, oy)
        drawLine(roadColor, from, to, strokeWidth = roadWidth)
    }

    // North-south road
    val nsRoad = listOf(
        12.9730 to 79.1565,
        12.9705 to 79.1560,
        12.9680 to 79.1555,
        12.9665 to 79.1550
    )
    for (i in 0 until nsRoad.size - 1) {
        val from = geoToScreen(nsRoad[i].first, nsRoad[i].second, width, height, scale, ox, oy)
        val to = geoToScreen(nsRoad[i + 1].first, nsRoad[i + 1].second, width, height, scale, ox, oy)
        drawLine(roadColor, from, to, strokeWidth = roadWidth)
    }
}

/**
 * Convert geographic coordinates to screen coordinates.
 * Uses Mercator-like projection centered on campus.
 */
private fun geoToScreen(
    lat: Double, lng: Double,
    canvasWidth: Float, canvasHeight: Float,
    scale: Float, offsetX: Float, offsetY: Float
): Offset {
    val centerLat = VITCampusZones.CAMPUS_CENTER_LAT
    val centerLng = VITCampusZones.CAMPUS_CENTER_LNG

    // 1 degree lat ≈ 111km, 1 degree lng ≈ cos(lat)*111km
    val metersPerDegreeLat = 111_320.0
    val metersPerDegreeLng = 111_320.0 * cos(Math.toRadians(centerLat))

    val dx = ((lng - centerLng) * metersPerDegreeLng).toFloat()
    val dy = ((centerLat - lat) * metersPerDegreeLat).toFloat() // Inverted Y

    // Scale to pixels (campus ~1200m across → map to ~80% of canvas)
    val pixelsPerMeter = minOf(canvasWidth, canvasHeight) * 0.8f / 1200f

    val screenX = canvasWidth / 2 + dx * pixelsPerMeter * scale + offsetX
    val screenY = canvasHeight / 2 + dy * pixelsPerMeter * scale + offsetY

    return Offset(screenX, screenY)
}

/**
 * Find which zone was tapped
 */
private fun findTappedZone(
    tapOffset: Offset,
    scale: Float, offsetX: Float, offsetY: Float,
    canvasWidth: Float, canvasHeight: Float
): CampusZone? {
    var closestZone: CampusZone? = null
    var closestDistance = Float.MAX_VALUE

    VITCampusZones.ALL_ZONES.forEach { zone ->
        val pos = geoToScreen(
            zone.centerLat, zone.centerLng,
            canvasWidth, canvasHeight, scale, offsetX, offsetY
        )
        val zoneRadius = (zone.radiusMeters / 3.0 * scale).toFloat()
        val distance = sqrt(
            (tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2)
        )
        if (distance < zoneRadius && distance < closestDistance) {
            closestDistance = distance
            closestZone = zone
        }
    }
    return closestZone
}
