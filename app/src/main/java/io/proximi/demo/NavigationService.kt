package io.proximi.demo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleService
import io.proximi.demo.ui.routepreview.getNotificationDrawable
import io.proximi.mapbox.library.RouteUpdateType

private const val NOTIFICATION_CHANNEL_ID = "demo_navigation_notification"
private const val NOTIFICATION_ID = 1

class NavigationService: LifecycleService() {

    private var notificationLastAlertNodeIndex: Int? = null

    override fun onCreate() {
        super.onCreate()
        createNavigationNotificationChannel()
        ProximiioHelper.getRouteUpdateLiveData().observe(this) { update ->
            if (update != null) {
                showNotification(update)
            } else {
                hideNotification()
                this@NavigationService.stopSelf()
            }
        }
    }

    override fun onDestroy() {
        if (!(application as DemoApp).foreground) {
            ProximiioHelper.apiStop()
            hideNotification()
        }
        super.onDestroy()
    }

    private fun showNotification(routeUpdate: RouteUpdate) {
        val openActivityPendingIntent = PendingIntent.getActivity(
                baseContext,
                0,
                Intent(baseContext, MainActivity::class.java). apply {
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                },
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_navigate)
            .setLargeIcon(getNotificationImage(routeUpdate))
            .setContentTitle(routeUpdate.text)
            .setContentText(routeUpdate.additionalText)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(!notificationShouldAlert(routeUpdate))
            .setContentIntent(openActivityPendingIntent)


        if (!routeUpdate.type.isRouteEnd()) {
            notificationBuilder
                .setAutoCancel(false)
                .setOngoing(true)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.colorNotification))
                .addAction(NotificationCompat.Action(
                    R.drawable.ic_close,
                    getString(R.string.notification_stop),
                    PendingIntent.getBroadcast(baseContext, 0, Intent(baseContext, NavigationCancelReceiver::class.java), 0)
                ))
        }

        val notification = notificationBuilder.build()
        NotificationManagerCompat.from(baseContext).notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun hideNotification() {
        Companion.hideNotification(baseContext)
    }

    /**
     * Evaluate if notification should alert (ping) user.
     */
    private fun notificationShouldAlert(routeEvent: RouteUpdate): Boolean {
        val alert = routeEvent.type == RouteUpdateType.DIRECTION_IMMEDIATE
                 && notificationLastAlertNodeIndex != routeEvent.data!!.nodeIndex
        if(alert) {
            notificationLastAlertNodeIndex = routeEvent.data?.nodeIndex ?: 0
        }
        return alert
    }

    /**
     * Create notification channel (for background navigation guidance).
     */
    private fun createNavigationNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_navigation_channel_name)
            val descriptionText = getString(R.string.notification_navigation_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText

            }
            // Register the channel with the system
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }

    /**
     * Translate navigation guidance event into Drawable IDs.
     */
    private fun getNotificationIcon(routeUpdate: RouteUpdate): Int {
        return when (routeUpdate.type) {
            RouteUpdateType.DIRECTION_UPDATE,
            RouteUpdateType.DIRECTION_IMMEDIATE,
            RouteUpdateType.DIRECTION_NEW,
            RouteUpdateType.DIRECTION_SOON      -> routeUpdate.data!!.stepDirection.getNotificationDrawable()
            RouteUpdateType.FINISHED            -> R.drawable.ic_destination
            RouteUpdateType.RECALCULATING       -> R.drawable.ic_recalculate
            else                                -> R.drawable.ic_close
        }
    }

    private fun getNotificationImage(routeUpdate: RouteUpdate): Bitmap {
        return getNotificationIcon(routeUpdate).let {
            val drawable = ResourcesCompat.getDrawable(resources, it, null)!!
            DrawableCompat.setTint(drawable, Color.WHITE)
            drawable.toBitmap()
        }
    }

    companion object {

        private fun hideNotification(context: Context) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }

    }
}