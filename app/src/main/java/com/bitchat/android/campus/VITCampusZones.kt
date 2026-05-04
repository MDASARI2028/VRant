package com.bitchat.android.campus

import androidx.compose.ui.graphics.Color
import com.bitchat.android.ui.theme.VRantColors

/**
 * VIT Vellore Campus Zone Definitions
 * Static coordinate mapping for campus zones — no real GPS needed for zone identification
 * but actual coordinates used when GPS is available.
 *
 * Coordinate system: VIT Vellore campus (~12.9692° N, 79.1559° E)
 */

data class CampusZone(
    val id: String,
    val displayName: String,
    val shortName: String,
    val category: ZoneCategory,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
    val color: Color,
    val emoji: String,
    val description: String
)

enum class ZoneCategory(val displayName: String, val emoji: String) {
    ACADEMIC("Academic", "📚"),
    HOSTEL("Hostel", "🏠"),
    FOOD("Food & Dining", "🍕"),
    LIBRARY("Library", "📖"),
    SPORTS("Sports", "⚽"),
    ADMIN("Administration", "🏛️"),
    COMMON("Common Area", "🌳"),
    TECH("Tech Hub", "💻")
}

/**
 * All predefined VIT Vellore campus zones
 */
object VITCampusZones {

    // ── Academic Blocks ──
    val TECHNOLOGY_TOWER = CampusZone(
        id = "VIT_TT",
        displayName = "Technology Tower",
        shortName = "TT",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9716,
        centerLng = 79.1595,
        radiusMeters = 80.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏢",
        description = "Technology Tower — main academic building"
    )

    val CDMM = CampusZone(
        id = "VIT_CDMM",
        displayName = "CDMM Building",
        shortName = "CDMM",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9708,
        centerLng = 79.1582,
        radiusMeters = 60.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "Centre for Disaster Mitigation & Management"
    )

    val SJT = CampusZone(
        id = "VIT_SJT",
        displayName = "SJT Building",
        shortName = "SJT",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9698,
        centerLng = 79.1563,
        radiusMeters = 100.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "Silver Jubilee Tower — largest academic block"
    )

    val GDN = CampusZone(
        id = "VIT_GDN",
        displayName = "GDN Building",
        shortName = "GDN",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9690,
        centerLng = 79.1555,
        radiusMeters = 70.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "G.D. Naidu Block"
    )

    val PRP = CampusZone(
        id = "VIT_PRP",
        displayName = "PRP Building",
        shortName = "PRP",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9684,
        centerLng = 79.1548,
        radiusMeters = 60.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "Pandit Ravi Shankar Shukla Block"
    )

    val SMV = CampusZone(
        id = "VIT_SMV",
        displayName = "SMV Building",
        shortName = "SMV",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9705,
        centerLng = 79.1575,
        radiusMeters = 60.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "Sir M. Visvesvaraya Block"
    )

    val MB = CampusZone(
        id = "VIT_MB",
        displayName = "Main Building",
        shortName = "MB",
        category = ZoneCategory.ACADEMIC,
        centerLat = 12.9695,
        centerLng = 79.1570,
        radiusMeters = 50.0,
        color = VRantColors.ZoneAcademic,
        emoji = "🏫",
        description = "Main Building"
    )

    // ── Hostels ──
    val MENS_HOSTEL = CampusZone(
        id = "VIT_MH",
        displayName = "Men's Hostel",
        shortName = "MH",
        category = ZoneCategory.HOSTEL,
        centerLat = 12.9725,
        centerLng = 79.1610,
        radiusMeters = 150.0,
        color = VRantColors.ZoneHostel,
        emoji = "🏠",
        description = "Men's Hostel blocks"
    )

    val WOMENS_HOSTEL = CampusZone(
        id = "VIT_WH",
        displayName = "Women's Hostel",
        shortName = "WH",
        category = ZoneCategory.HOSTEL,
        centerLat = 12.9672,
        centerLng = 79.1535,
        radiusMeters = 120.0,
        color = VRantColors.ZoneHostel,
        emoji = "🏠",
        description = "Women's Hostel blocks"
    )

    // ── Food & Dining ──
    val FOOD_COURT = CampusZone(
        id = "VIT_FC",
        displayName = "Food Court",
        shortName = "FC",
        category = ZoneCategory.FOOD,
        centerLat = 12.9700,
        centerLng = 79.1560,
        radiusMeters = 50.0,
        color = VRantColors.ZoneCafeteria,
        emoji = "🍕",
        description = "Main campus food court"
    )

    val DARLING_CAFETERIA = CampusZone(
        id = "VIT_CAFE",
        displayName = "Darling Cafeteria",
        shortName = "Cafe",
        category = ZoneCategory.FOOD,
        centerLat = 12.9688,
        centerLng = 79.1545,
        radiusMeters = 40.0,
        color = VRantColors.ZoneCafeteria,
        emoji = "☕",
        description = "Darling Cafeteria"
    )

    // ── Library ──
    val LIBRARY = CampusZone(
        id = "VIT_LIB",
        displayName = "Central Library",
        shortName = "Lib",
        category = ZoneCategory.LIBRARY,
        centerLat = 12.9692,
        centerLng = 79.1565,
        radiusMeters = 60.0,
        color = VRantColors.ZoneLibrary,
        emoji = "📖",
        description = "Central Library"
    )

    // ── Sports ──
    val SPORTS_COMPLEX = CampusZone(
        id = "VIT_SPORTS",
        displayName = "Sports Complex",
        shortName = "Sports",
        category = ZoneCategory.SPORTS,
        centerLat = 12.9678,
        centerLng = 79.1590,
        radiusMeters = 100.0,
        color = VRantColors.ZoneSports,
        emoji = "⚽",
        description = "Sports complex and grounds"
    )

    // ── Admin ──
    val ADMIN_BLOCK = CampusZone(
        id = "VIT_ADMIN",
        displayName = "Admin Block",
        shortName = "Admin",
        category = ZoneCategory.ADMIN,
        centerLat = 12.9710,
        centerLng = 79.1558,
        radiusMeters = 50.0,
        color = VRantColors.ZoneAdmin,
        emoji = "🏛️",
        description = "Administrative offices"
    )

    // ── Common ──
    val ANNA_AUDITORIUM = CampusZone(
        id = "VIT_ANNA",
        displayName = "Anna Auditorium",
        shortName = "Anna",
        category = ZoneCategory.COMMON,
        centerLat = 12.9703,
        centerLng = 79.1552,
        radiusMeters = 40.0,
        color = VRantColors.ZoneDefault,
        emoji = "🎭",
        description = "Anna Auditorium"
    )

    val GREENOS = CampusZone(
        id = "VIT_GREENOS",
        displayName = "Greenos",
        shortName = "Greenos",
        category = ZoneCategory.COMMON,
        centerLat = 12.9695,
        centerLng = 79.1540,
        radiusMeters = 50.0,
        color = VRantColors.ZoneSports,
        emoji = "🌳",
        description = "Greenos open area"
    )

    // ── Tech Hub ──
    val TBI = CampusZone(
        id = "VIT_TBI",
        displayName = "VIT-TBI",
        shortName = "TBI",
        category = ZoneCategory.TECH,
        centerLat = 12.9715,
        centerLng = 79.1588,
        radiusMeters = 40.0,
        color = VRantColors.MeshBlue,
        emoji = "💻",
        description = "Technology Business Incubator"
    )

    /** All zones in the campus */
    val ALL_ZONES: List<CampusZone> = listOf(
        TECHNOLOGY_TOWER, CDMM, SJT, GDN, PRP, SMV, MB,
        MENS_HOSTEL, WOMENS_HOSTEL,
        FOOD_COURT, DARLING_CAFETERIA,
        LIBRARY,
        SPORTS_COMPLEX,
        ADMIN_BLOCK,
        ANNA_AUDITORIUM, GREENOS,
        TBI
    )

    /** Zone lookup by ID */
    private val zoneMap: Map<String, CampusZone> = ALL_ZONES.associateBy { it.id }

    fun getZoneById(id: String): CampusZone? = zoneMap[id]

    /** Get zones by category */
    fun getZonesByCategory(category: ZoneCategory): List<CampusZone> =
        ALL_ZONES.filter { it.category == category }

    /** Campus center coordinates (for map centering) */
    const val CAMPUS_CENTER_LAT = 12.9692
    const val CAMPUS_CENTER_LNG = 79.1559
    const val CAMPUS_RADIUS_METERS = 600.0

    /** Find the nearest zone to a given lat/lng */
    fun findNearestZone(lat: Double, lng: Double): CampusZone? {
        val results = FloatArray(1)
        var nearest: CampusZone? = null
        var minDistance = Double.MAX_VALUE

        for (zone in ALL_ZONES) {
            android.location.Location.distanceBetween(
                lat, lng, zone.centerLat, zone.centerLng, results
            )
            val distance = results[0].toDouble()
            if (distance < zone.radiusMeters && distance < minDistance) {
                minDistance = distance
                nearest = zone
            }
        }
        return nearest
    }

    /** Check if coordinates are within the VIT campus bounds */
    fun isOnCampus(lat: Double, lng: Double): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            lat, lng, CAMPUS_CENTER_LAT, CAMPUS_CENTER_LNG, results
        )
        return results[0] < CAMPUS_RADIUS_METERS
    }
}
