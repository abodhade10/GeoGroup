package com.geogroup.android.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geogroup.android.data.FirebaseGroupRepository
import com.geogroup.shared.model.AlertEvent
import com.geogroup.shared.model.TransitionType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by the OS whenever a registered geofence transition happens,
 * even if the app is fully backgrounded/killed (Android restarts the
 * receiver via the PendingIntent). This is the "member left the area"
 * trigger point — from here we just write one Firestore doc; the fan-out
 * to other members happens via AlertsListenerService's snapshot listener.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            val error = GeofenceStatusCodes.getStatusCodeString(event.errorCode)
            android.util.Log.e("GeofenceReceiver", "Geofencing error: $error")
            return
        }

        val transition = when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> TransitionType.EXIT
            Geofence.GEOFENCE_TRANSITION_ENTER -> TransitionType.ENTER
            else -> return // ignore DWELL etc. for this assignment
        }

        val groupId = event.triggeringGeofences?.firstOrNull()?.requestId ?: return
        val location = event.triggeringLocation
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val alert = AlertEvent(
            groupId = groupId,
            userId = user.uid,
            userName = user.displayName ?: "A member",
            transition = transition,
            timestamp = System.currentTimeMillis(),
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0
        )

        // Fire-and-forget write; goroutine-scoped to a short-lived process-wide scope
        // since BroadcastReceiver.onReceive must return quickly.
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseGroupRepository().postAlert(alert)
        }
    }
}
