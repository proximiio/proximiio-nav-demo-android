package io.proximi.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NavigationCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ProximiioHelper.routeCancel()
        stopService(context)
    }

    private fun stopService(context: Context) {
        context.stopService(Intent(context, NavigationService::class.java))
    }
}