package io.proximi.demo

import android.content.Context
import android.graphics.Color
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import io.proximi.mapbox.library.MapboxLocationComponentActivator

/**
 * Custom Proximi.io Mapbox SDK MapboxLocationComponentActivator.
 * Override to:
 * - get desired location marker style and behaviour
 * - provide mapbox location component activation callback.
 */
class CustomLocationComponentActivator(
    private val context: Context,
    var large: Boolean = false,
    var onActivationCallback: (() -> Unit)? = null
): MapboxLocationComponentActivator(context) {

    override fun getLocationComponentOptions(): LocationComponentOptions {
        return LocationComponentOptions.builder(context)
            .foregroundDrawable(if (large) R.drawable.location_marker_large else R.drawable.location_marker)
            .bearingDrawable(if (large) R.drawable.location_marker_bearing_large else R.drawable.location_marker_bearing)
            .foregroundStaleTintColor(Color.GRAY)
            .gpsDrawable(if (large) R.drawable.navigation_marker_large else R.drawable.navigation_marker)
            .trackingGesturesManagement(true)
            .staleStateTimeout(20000)
            .accuracyAlpha(0f)
            .elevation(0.0001f)
            .build()
    }

    override fun onLocationComponentActivated() {
        onActivationCallback?.invoke()
        onActivationCallback = null
    }
}