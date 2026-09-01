package com.geogroup.android.data

import com.geogroup.shared.model.AlertEvent
import com.geogroup.shared.model.GeofenceConfig
import com.geogroup.shared.model.TrackingGroup
import com.geogroup.shared.model.TransitionType
import com.geogroup.shared.repo.GroupRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore schema:
 *
 * groups/{groupId}
 *    name, ownerId, memberIds: [uid], geofence: {label, lat, lng, radiusMeters}, createdAt
 *
 * groups/{groupId}/alerts/{alertId}
 *    userId, userName, transition, timestamp, latitude, longitude
 */
class FirebaseGroupRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GroupRepository {

    private val groups get() = db.collection("groups")

    override suspend fun createGroup(name: String, ownerId: String): TrackingGroup {
        val doc = groups.document()
        val group = TrackingGroup(
            id = doc.id,
            name = name,
            ownerId = ownerId,
            memberIds = listOf(ownerId),
            createdAt = System.currentTimeMillis()
        )
        doc.set(group).await()
        return group
    }

    override suspend fun joinGroup(groupId: String, userId: String) {
        groups.document(groupId)
            .update("memberIds", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
            .await()
    }

    override suspend fun setGeofence(groupId: String, config: GeofenceConfig) {
        groups.document(groupId).update("geofence", config).await()
    }

    override fun observeGroup(groupId: String): Flow<TrackingGroup?> = callbackFlow {
        val reg: ListenerRegistration = groups.document(groupId).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject(TrackingGroup::class.java))
        }
        awaitClose { reg.remove() }
    }

    override fun observeMyGroups(userId: String): Flow<List<TrackingGroup>> = callbackFlow {
        val reg = groups.whereArrayContains("memberIds", userId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(TrackingGroup::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun postAlert(alert: AlertEvent) {
        val alertsCol = groups.document(alert.groupId).collection("alerts")
        alertsCol.document().set(alert).await()
    }

    /** Emits every new alert doc added to this group's alerts subcollection. */
    override fun observeAlerts(groupId: String): Flow<AlertEvent> = callbackFlow {
        val reg = groups.document(groupId).collection("alerts")
            .addSnapshotListener { snap, _ ->
                snap?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        change.document.toObject(AlertEvent::class.java).let { trySend(it) }
                    }
                }
            }
        awaitClose { reg.remove() }
    }
}
