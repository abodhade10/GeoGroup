package com.geogroup.android.data

import com.geogroup.android.alerts.NotificationHelper
import com.geogroup.shared.model.AlertEvent
import com.geogroup.shared.model.TransitionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Production-grade fan-out path (kept here as the "next step" beyond the
 * foreground-service demo): a Cloud Function watching
 * groups/{groupId}/alerts/{alertId} onCreate would look up the other
 * members' fcmToken values and send each of them a data message that
 * lands here even if the app process isn't running. This class also
 * keeps each user's fcmToken fresh in Firestore.
 */
class GeoGroupMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val groupName = data["groupName"] ?: "Your group"
        val alert = AlertEvent(
            groupId = data["groupId"].orEmpty(),
            userId = data["userId"].orEmpty(),
            userName = data["userName"] ?: "A member",
            transition = TransitionType.EXIT
        )
        NotificationHelper.showMemberLeftNotification(applicationContext, groupName, alert)
    }
}
