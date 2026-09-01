package com.geogroup.shared.model

/**
 * Pure data models shared across platforms.
 * Kept free of any Android/Firebase types so this module stays truly multiplatform.
 */

data class AppUser(
    val uid: String,
    val displayName: String,
    val fcmToken: String? = null
)

data class TrackingGroup(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val geofence: GeofenceConfig? = null,
    val createdAt: Long = 0L
)

data class GeofenceConfig(
    val label: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 200f
)

enum class TransitionType { ENTER, EXIT }

data class AlertEvent(
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val userName: String = "",
    val transition: TransitionType = TransitionType.EXIT,
    val timestamp: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
