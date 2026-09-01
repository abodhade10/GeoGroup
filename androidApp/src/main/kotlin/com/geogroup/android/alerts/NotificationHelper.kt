package com.geogroup.android.alerts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.geogroup.android.GeoGroupApp
import com.geogroup.shared.model.AlertEvent
import kotlin.random.Random

object NotificationHelper {
    fun showMemberLeftNotification(context: Context, groupName: String, alert: AlertEvent) {
        // POST_NOTIFICATIONS is a runtime permission on API 33+; the user may have
        // denied it, so bail out rather than let notify() throw a SecurityException.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, GeoGroupApp.ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$groupName: member left the area")
            .setContentText("${alert.userName} moved outside the geofenced region")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }
}
