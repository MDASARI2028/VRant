package com.bitchat.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bitchat.android.campus.CampusMapView
import com.bitchat.android.campus.CampusZone
import com.bitchat.android.ui.theme.VRantColors

/**
 * Map tab — wraps the CampusMapView with data from CampusZoneManager
 */
@Composable
fun MapScreen(
    currentZone: CampusZone?,
    zonePeerCounts: Map<String, Int>,
    isOnCampus: Boolean,
    onZoneTapped: (CampusZone) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VRantColors.DarkBase)
    ) {
        CampusMapView(
            currentZone = currentZone,
            zonePeerCounts = zonePeerCounts,
            isOnCampus = isOnCampus,
            onZoneTapped = onZoneTapped,
            modifier = Modifier.fillMaxSize()
        )
    }
}
