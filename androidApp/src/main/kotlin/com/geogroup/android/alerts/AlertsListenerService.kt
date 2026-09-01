package com.geogroup.android.alerts

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geogroup.android.GeoGroupApp
import com.geogroup.android.data.FirebaseGroupRepository
import com.geogroup.shared.model.TransitionType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Foreground service, started once the user has at least one group with a
 * geofence set. Holds a live Firestore listener on that group's alerts
 * subcollection so that when ANY member (including this device) triggers
 * an exit, every other member's phone shows a notification in real time
 * without needing a server-side push trigger.
 *
 * Note: for a production app this fan-out is better done with a Cloud
 * Function -> FCM push so it works even if the app process is fully
 * killed (Doze). For the scope of this assignment, a foreground service
 * with a live listener is a reasonable, easily-demoable tradeoff.
 */
class AlertsListenerService : Service() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val repo = FirebaseGroupRepository()

    companion object {
        const val EXTRA_GROUP_ID = "groupId"
        const val EXTRA_GROUP_NAME = "groupName"
        private const val NOTIF_ID = 1001
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val groupId = intent?.getStringExtra(EXTRA_GROUP_ID) ?: return START_NOT_STICKY
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "your group"

        startForeground(NOTIF_ID, buildForegroundNotification(groupName))

        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        scope.launch {
            repo.observeAlerts(groupId).collect { alert ->
                // Don't notify the person about their own exit -- everyone ELSE gets notified.
                if (alert.userId != myUid && alert.transition == TransitionType.EXIT) {
                    NotificationHelper.showMemberLeftNotification(applicationContext, groupName, alert)
                }
            }
        }
        return START_STICKY
    }

    private fun buildForegroundNotification(groupName: String): Notification =
        NotificationCompat.Builder(this, GeoGroupApp.ALERT_CHANNEL_ID)
            .setContentTitle("GeoGroup active")
            .setContentText("Watching \"$groupName\" for geofence exits")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
