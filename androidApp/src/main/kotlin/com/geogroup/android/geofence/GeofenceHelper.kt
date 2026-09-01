package com.geogroup.android.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.geogroup.shared.model.GeofenceConfig
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Wraps the Play Services GeofencingClient. One geofence is registered per
 * group the user belongs to; the geofence's requestId is the groupId so the
 * BroadcastReceiver can map an exit event straight back to a group.
 */
class GeofenceHelper(private val context: Context) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission") // caller is required to have checked runtime permissions
    fun registerGeofence(groupId: String, config: GeofenceConfig) {
        val geofence = Geofence.Builder()
            .setRequestId(groupId)
            .setCircularRegion(config.latitude, config.longitude, config.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
    }

    fun removeGeofence(groupId: String) {
        geofencingClient.removeGeofences(listOf(groupId))
    }
}
