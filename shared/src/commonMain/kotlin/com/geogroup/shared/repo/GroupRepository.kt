package com.geogroup.shared.repo

import com.geogroup.shared.model.AlertEvent
import com.geogroup.shared.model.GeofenceConfig
import com.geogroup.shared.model.TrackingGroup
import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic contract. The Android app provides a Firestore-backed
 * implementation (see androidApp/.../data/FirebaseGroupRepository.kt).
 * A future iOS target would implement this same interface against the
 * same Firestore project (Firebase KMP SDK or GitLive Firebase-Kotlin-SDK).
 */
interface GroupRepository {
    suspend fun createGroup(name: String, ownerId: String): TrackingGroup
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun setGeofence(groupId: String, config: GeofenceConfig)
    fun observeGroup(groupId: String): Flow<TrackingGroup?>
    fun observeMyGroups(userId: String): Flow<List<TrackingGroup>>
    suspend fun postAlert(alert: AlertEvent)
    fun observeAlerts(groupId: String): Flow<AlertEvent>
}
