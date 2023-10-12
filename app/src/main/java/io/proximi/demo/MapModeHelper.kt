package io.proximi.demo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap

private const val DEFAULT_ZOOM_INSIDE = 19.5

/**
 * Helper class that handles map mode switching.
 */
class MapModeHelper(
    private val context: Context
//    private val activity: MainActivity
) {

    private var map: MapboxMap? = null
    private var locationButton: FloatingActionButton? = null
    private var orientationButton: FloatingActionButton? = null
    private var positionInitiated = false

    /** Mapbox location component stale status */
    var stale = false
        set(value) {
            field = value; applyState()
        }

    /** Evaluate if device has compass capabilites */
    private val hasCompassCapabilities = deviceHasCompassCapability(context)

    /** Flag if we compass heading or 'route' heading should be used for navigation */
    private var useCompassHeadingForNavigation = true

    /** Flag if navigation is in progress currently */
    private var isNavigating = false

    /** Current map settings */
    private var currentState = States.TRACKING_BEARING
        set(value) {
            if (field != value) {
                field = value; applyState(); }
        }

    fun onMapInit(map: MapboxMap) {
        this.map = map
        map.addOnMoveListener(object : MapboxMap.OnMoveListener {
            override fun onMove(detector: MoveGestureDetector) {}
            override fun onMoveEnd(detector: MoveGestureDetector) {}
            override fun onMoveBegin(detector: MoveGestureDetector) {
                currentState = when (currentState) {
                    States.TRACKING_NORTH -> States.CUSTOM_NORTH
                    States.TRACKING_BEARING -> States.CUSTOM_CUSTOM
                    States.TRACKING_CUSTOM -> States.CUSTOM_CUSTOM
                    States.CUSTOM_NORTH -> States.CUSTOM_CUSTOM
                    States.CUSTOM_CUSTOM -> States.CUSTOM_CUSTOM
                }
            }
        })
        map.addOnRotateListener(object : MapboxMap.OnRotateListener {
            override fun onRotate(detector: RotateGestureDetector) {}
            override fun onRotateEnd(detector: RotateGestureDetector) {}
            override fun onRotateBegin(detector: RotateGestureDetector) {
                currentState = when (currentState) {
                    States.TRACKING_NORTH -> States.TRACKING_CUSTOM
                    States.TRACKING_BEARING -> States.TRACKING_CUSTOM
                    States.TRACKING_CUSTOM -> States.TRACKING_CUSTOM
                    States.CUSTOM_NORTH -> States.CUSTOM_CUSTOM
                    States.CUSTOM_CUSTOM -> States.CUSTOM_CUSTOM
                }
            }
        })
        currentState = States.CUSTOM_NORTH
    }

    fun initControls(
        locationButton: FloatingActionButton,
        orientationButton: FloatingActionButton,
        checkPlace: () -> Boolean
    ) {
        this.locationButton = locationButton
        this.orientationButton = orientationButton
        locationButton.setOnClickListener { showCurrentLocation(checkPlace) }
        orientationButton.setOnClickListener { toggleMapOrientation() }
        applyButtonState()
    }

    fun onLocationUpdate(location: Location?) {
        if (map == null || positionInitiated) {
            return
        }
        location?.let {
            positionInitiated = true
            zoomToLocation()
        }
    }

    /**
     * Should be called when navigation starts to adjust map view behaviour.
     */
    fun onNavigationStart() {
        if (!isNavigating) {
            isNavigating = true
            showCurrentLocation(null)
        }
    }

    /**
     * Should be called when navigation ends to adjust map view behaviour.
     */
    fun onNavigationEnd() {
        if (isNavigating) {
            isNavigating = false
            currentState = States.TRACKING_BEARING
            applyState()
        }
    }

    /**
     * Toggle between using compass heading or route heading (i.e. direction based on current navigation route)
     * to orient map location component.
     */
    fun setUseCompassHeadingForNavigation(useCompassHeading: Boolean) {
        useCompassHeadingForNavigation = useCompassHeading
        applyState()
    }

    /**
     * Process current state ([currentState]) and apply [CameraMode] and [RenderMode].
     * Also updates orientation button view to reflect current state.
     */
    private fun applyState() {
        if (map!!.locationComponent.isLocationComponentActivated && stale) {
            map!!.locationComponent.cameraMode = CameraMode.TRACKING
            map!!.locationComponent.renderMode = RenderMode.NORMAL
        } else if (map!!.locationComponent.isLocationComponentActivated) {
            map!!.locationComponent.cameraMode =
                if (isNavigating && !useCompassHeadingForNavigation) currentState.navigationCameraMode else currentState.mapCameraMode
            applyRenderMode()
        }
        applyButtonState()
    }

    private fun applyButtonState() {
        if (currentState.orientationEnabled) {
            orientationButton?.setImageResource(R.drawable.ic_compass)
        } else {
            orientationButton?.setImageResource(R.drawable.ic_compass_disabled)
        }
    }

    /**
     * Evalues current state and (safely) applies appropriate [RenderMode].
     */
    private fun applyRenderMode() {
        if (map!!.locationComponent.isLocationComponentActivated) {
            map!!.locationComponent.renderMode =
                if (isNavigating && (!useCompassHeadingForNavigation || !hasCompassCapabilities))
                    RenderMode.GPS
                else if (hasCompassCapabilities)
                    RenderMode.COMPASS
                else
                    RenderMode.NORMAL
        }
    }

    /**
     * Shows (moves to) current location on map. Also swiches map mode to follow current location.
     */
    private fun showCurrentLocation(checkPlace: (() -> Boolean)?) {
        if (checkPlace?.invoke() == false) {
            return
        }
        map?.let { map ->
            map.getStyle {
                if (map.locationComponent.isLocationComponentActivated) {
                    zoomToLocation()
                    currentState = when (currentState) {
                        States.CUSTOM_CUSTOM -> States.TRACKING_BEARING
                        States.CUSTOM_NORTH -> States.TRACKING_NORTH
                        else -> {
                            applyState(); currentState
                        }
                    }
                    ProximiioHelper.updateDisplayLevel(ProximiioHelper.getUserLevel().value ?: 0)
                }
            }
        }
    }

    /**
     * Zoom to current location on map.
     */
    private fun zoomToLocation() {
        if (map!!.locationComponent.lastKnownLocation != null) {
            val location = map!!.locationComponent.lastKnownLocation!!
            map!!.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), getDefaultZoom()
                ), object : MapboxMap.CancelableCallback {
                    override fun onCancel() {}

                    override fun onFinish() {
                        currentState = States.TRACKING_BEARING
                    }
                })
        }
    }

    fun reapplyZoom() {
        if (positionInitiated) {
            map!!.animateCamera(CameraUpdateFactory.zoomTo(getDefaultZoom()))
        }
    }

    /***
     * Toggles map orientation  between north and following heading.
     */
    private fun toggleMapOrientation() {
        if (map!!.locationComponent.isLocationComponentActivated) {
            if (hasCompassCapabilities) {
                currentState = when (currentState) {
                    States.TRACKING_NORTH -> States.TRACKING_BEARING
                    States.TRACKING_BEARING -> States.TRACKING_NORTH
                    States.TRACKING_CUSTOM -> States.TRACKING_BEARING
                    States.CUSTOM_NORTH -> States.TRACKING_BEARING
                    States.CUSTOM_CUSTOM -> States.TRACKING_BEARING
                }
            } else {
                when (currentState) {
                    States.TRACKING_CUSTOM -> currentState = States.TRACKING_NORTH
                    States.CUSTOM_CUSTOM -> currentState = States.CUSTOM_NORTH
                    else -> currentState = currentState
                }
            }
        }
    }

    /**
     * Check if device supports compass.
     *
     * This check is based on default implementation of mapbox's default [CompassEngine].
     */
    private fun deviceHasCompassCapability(context: Context): Boolean {
        ContextCompat.getSystemService(context, SensorManager::class.java)?.apply {
            var compassSensor = getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            var gravitySensor: Sensor? = null
            var magneticFieldSensor: Sensor? = null
            if (compassSensor == null) {
                if (getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                    compassSensor = getDefaultSensor(Sensor.TYPE_ORIENTATION)
                } else {
                    gravitySensor = getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    magneticFieldSensor = getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                }
            }
            return compassSensor != null || (gravitySensor != null && magneticFieldSensor != null)
        }
        return false
    }

    private fun getDefaultZoom(): Double {
        return DEFAULT_ZOOM_INSIDE
    }

    /**
     * List of desired map states for easier manipulation.
     */
    private enum class States(
        val mapCameraMode: Int,
        val navigationCameraMode: Int,
        val orientationEnabled: Boolean
    ) {
        TRACKING_NORTH(CameraMode.TRACKING_GPS_NORTH, CameraMode.TRACKING_GPS_NORTH, false),
        TRACKING_CUSTOM(CameraMode.TRACKING, CameraMode.TRACKING, false),
        TRACKING_BEARING(CameraMode.TRACKING_COMPASS, CameraMode.TRACKING_GPS, true),
        CUSTOM_NORTH(CameraMode.NONE, CameraMode.TRACKING_GPS_NORTH, false),
        CUSTOM_CUSTOM(CameraMode.NONE, CameraMode.NONE, false);
    }
}

/**
 * Convert attribute value to Color Int value.
 */
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}