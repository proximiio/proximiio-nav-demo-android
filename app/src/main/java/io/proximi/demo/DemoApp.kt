package io.proximi.demo

import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.mapbox.mapboxsdk.Mapbox

class DemoApp: MultiDexApplication(), LifecycleObserver {

    var foreground = false

    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, TOKEN)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        foreground = true
        ProximiioHelper.apiStart()
        stopService()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        foreground = false
        // Do not start service if not navigating
        if (ProximiioHelper.isNavigating()) {
            startService()
        } else {
            ProximiioHelper.apiStop()
        }
    }

    /* ****************************************************************************************** */
    /* Service */

    private fun startService() {
        val intent = Intent(this, NavigationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopService() {
        stopService(Intent(this, NavigationService::class.java))
    }
}