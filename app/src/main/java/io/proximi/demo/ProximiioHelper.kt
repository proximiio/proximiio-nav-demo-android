package io.proximi.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.maps.MapView
import io.proximi.demo.ui.Preferences
import io.proximi.mapbox.data.model.Feature
import io.proximi.mapbox.library.*
import io.proximi.proximiiolibrary.*
import io.proximi.proximiiolibrary.routesnapping.database.PxWayfindingRoutable

// Proximi.io
const val TOKEN: String =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImlzcyI6ImQ4ODM5OTEzLTFiZDgtNDI0OC04N2IzLWI4ZjUzYjU5ZWIyNyIsInR5cGUiOiJhcHBsaWNhdGlvbiIsImFwcGxpY2F0aW9uX2lkIjoiMmQzZGZjOTUtYWJkNi00NWMzLWI3NDktM2I1NjM2MzI0Zjk0In0.pNTWHrLl9VT7K8ndNLiZjkXnKEuO0U8KyMVy_5omEh4"
private const val SNAPPING_DISTANCE = 10.0
private const val PATH_FIX_DISTANCE = 10.0
private const val REROUTING_DISTANCE = 14.0

@SuppressLint("StaticFieldLeak")
object ProximiioHelper {
    private var proximiioMapbox: ProximiioMapbox? = null
    private var proximiioAPI: ProximiioAPI? = null
    private var context: Context? = null
    private val displayLevel: MutableLiveData<Int> = MutableLiveData(0)
    private val userLevel: MutableLiveData<Int> = MutableLiveData(0)

    private val userLocation: MutableLiveData<Location?> = MutableLiveData(null)
    private val place: MutableLiveData<ProximiioPlace?> = MutableLiveData(null)
    private val route: MutableLiveData<Route?> = MutableLiveData(null)
    private val routeUpdate: MutableLiveData<RouteUpdate?> = MutableLiveData(null)
    private val visitorId: MutableLiveData<String> = MutableLiveData(null)
    private var tts: TextToSpeech? = null
    private val inputList = mutableListOf<ProximiioInput>()
//    private var simulationProcessor: ProximiioSimulationProcessor? = null

    fun initialize(activity: Activity) {
        if (proximiioMapbox == null) {
            this.context = activity.baseContext
            this.proximiioMapbox = ProximiioMapbox.getInstance(context!!, TOKEN, null)
                .apply {
                    setUserLocationToRouteSnappingEnabled(true)
                    setUserLocationToRouteSnappingThreshold(SNAPPING_DISTANCE)
                    setRouteFinishThreshold(1.0)
                    setStepImmediateThreshold(0.8)
                    setStepPreparationThreshold(1.5)
                    setRoutePathFixDistance(PATH_FIX_DISTANCE)
                    setRerouteThreshold(REROUTING_DISTANCE)
                }


            // setup processors
//            simulationProcessor = ProximiioSimulationProcessor(this.context!!)
//            simulationProcessor?.avgStepLength = 0.70
//            simulationProcessor?.supportOnlyNavigation = false

            val proximiioOptions = ProximiioOptions().apply {
//                notificationMode = ProximiioOptions.NotificationMode.DISABLED
                // show persistant notification
                notificationMode = ProximiioOptions.NotificationMode.ENABLED
            }
            this.proximiioAPI = ProximiioAPI("api", this.context!!, proximiioOptions).apply {
                setAuth(TOKEN, true)
                setListener(apiListener)
                trySavedLogin()
                setActivity(activity)
            }

//            this.proximiioAPI!!.setProcessors(arrayListOf(simulationProcessor))

            this.visitorId.postValue(this.proximiioAPI!!.visitorID)
            updateMapboxSettings()
        }
    }

    fun getMapInstance(): ProximiioMapbox? {
        return proximiioMapbox
    }

    fun getApiInstance(): ProximiioAPI? {
        return proximiioAPI
    }

    fun getPlaceLiveData(): LiveData<ProximiioPlace?> {
        return place
    }

    fun getDisplayLevel(): LiveData<Int> {
        return displayLevel
    }

    fun getMapboxSyncStatus(): LiveData<SyncStatus> {
        return getMapInstance()!!.syncStatus
    }

    fun getUserLevel(): LiveData<Int> {
        return userLevel
    }

    fun getInputs(): MutableList<ProximiioInput> {
        return inputList
    }

    fun getVisitorId(): LiveData<String> {
        return visitorId
    }

    fun getUserLocation(): LiveData<Location?> {
        return userLocation
    }

    fun getRouteLiveData(): LiveData<Route?> {
        return route
    }

    fun getRouteUpdateLiveData(): LiveData<RouteUpdate?> {
        return routeUpdate
    }

    fun getPoiFeatures(): List<Feature> {
        return getMapInstance()!!.features.value!!.filter { it.getType() == ProximiioFeatureType.POI }
    }

    fun getFeatureList(): LiveData<List<Feature>> {
        return getMapInstance()!!.features
    }

    /* ------------------------------------------------------------------------------------------ */
    /* API */

    fun apiStart() {
        proximiioAPI?.onStart()
    }

    fun apiStop() {
        proximiioAPI?.onStop()
    }

    fun apiDestroy(stopService: Boolean) {
        if (stopService) {
            proximiioAPI!!.destroyService(false)
        } else {
            proximiioAPI!!.destroy()
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    /* Mapbox */

    fun setupMap(mapView: MapView, savedInstanceState: Bundle?, activatedCallback: () -> Unit) {
        proximiioMapbox!!.setFloorPlanVisibility(false)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            proximiioMapbox!!.onMapReady(mapboxMap)
            mapboxMap.getStyle {
                proximiioMapbox!!.updateDisplayLevel(displayLevel.value!!)
                proximiioMapbox!!.updateUserLevel(userLevel.value!!)
                activatedCallback()
            }
        }
    }

    fun mapDestroyed() {
        proximiioMapbox?.onDestroy()
    }

    fun isNavigating(): Boolean {
        return route.value != null
                && routeUpdate.value?.type?.isRouteEnd() != true
                && routeUpdate.value?.type != RouteUpdateType.CALCULATING
    }

    /* ------------------------------------------------------------------------------------------ */
    /* ??? */

    private var vibrator: Vibrator? = null
    private var vibratedSegmentNode: Int? = null

    fun updateMapboxSettings() {
        val preferences = Preferences(context!!)
        enableTts(preferences.getUseVoiceGuidance())
        enableVibration(preferences.getUseVibration())
    }

    private fun enableTts(enable: Boolean) {
        if (enable) {
            tts = TextToSpeech(context!!) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    proximiioMapbox!!.ttsEnable(tts!!)
                    proximiioMapbox!!.ttsHeadingCorrectionEnabled(true)
                    proximiioMapbox!!.ttsReassuranceInstructionEnabled(false)
                }
            }
        } else {
            tts = null
            proximiioMapbox!!.ttsDisable()
        }
    }

    private fun enableVibration(enable: Boolean) {
        vibrator = if (enable) {
            context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } else {
            null
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator?.vibrate(75)
        }
    }

    fun updateDisplayLevel(level: Int) {
        proximiioMapbox!!.updateDisplayLevel(level)
        displayLevel.postValue(level)
    }

    fun routeFindAndPreview(feature: Feature) {
        proximiioMapbox!!.routeCancel()
        val preferences = Preferences(context!!)
        val routeConfiguration = RouteConfiguration.Builder()
            .setDestination(feature)
            .setAvoidElevators(!preferences.getUseElevators())
            .setAvoidStaircases(!preferences.getUseStairs())
            .setAvoidNarrowPaths(!preferences.getUseAccessibleRoutes())
            .build()
        proximiioMapbox!!.routeFindAndPreview(routeConfiguration, routeCallback)
    }

    fun routeStart() {
        proximiioMapbox!!.routeStart()
    }

    fun routeCancel() {
        proximiioMapbox!!.routeCancel()
    }

    fun routeClear() {
        routeUpdate.postValue(null)
        route.postValue(null)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        proximiioAPI!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        proximiioAPI!!.onActivityResult(requestCode, resultCode, data)
    }

    // Simulation processor logic
    private fun simulationProcessorRoutes(currentRoute: Route? = null) {
        val currentFloor = userLevel.value

        val routesForCurrentFloor = currentRoute?.nodeList?.filter { it.level == currentFloor }
            ?.mapNotNull { it.lineStringFeatureTo }
            ?.map { PxWayfindingRoutable.fromGeoJsonFeature(it, null) }
            ?.toCollection(ArrayList())

//        simulationProcessor?.set(routesForCurrentFloor ?: arrayListOf())
    }

    private val routeCallback = object : RouteCallback {
        override fun onRoute(route: Route?) {
            this@ProximiioHelper.route.postValue(route)
            simulationProcessorRoutes(route)
        }

        override fun routeEvent(
            eventType: RouteUpdateType,
            text: String,
            additionalText: String?,
            data: RouteUpdateData?
        ) {
            routeUpdate.postValue(RouteUpdate(eventType, text, additionalText, data))

            if (eventType.isRouteEnd()) {
                simulationProcessorRoutes()
            }

            if (
                (eventType == RouteUpdateType.DIRECTION_IMMEDIATE && data!!.nodeIndex != vibratedSegmentNode)
                || eventType.isRouteEnd()
                || eventType == RouteUpdateType.RECALCULATING
            ) {
                vibratedSegmentNode = data?.nodeIndex
                vibrate()
            }
        }
    }

    private val apiListener = object : ProximiioListener() {

        override fun activatedApplication(application: ProximiioApplication?) {
            Log.d("ProximiioHelper", "")
        }

        override fun addedInput(input: ProximiioInput) {
            inputList.add(input)
        }

        override fun removedInput(input: ProximiioInput) {
            inputList.remove(input)
        }

        override fun position(location: Location) {
            val processedLocation = proximiioMapbox!!.updateUserLocation(location)?.apply {
                if (extras == null) {
                    extras = location.extras
//                    extras = bundleOf(
//                        "io.proximi.proximiiolibrary.type" to location.extras?.getString("io.proximi.proximiiolibrary.type")
//                    )
                }
            }
            userLocation.postValue(processedLocation)
//            userLocation.postValue(proximiioMapbox!!.updateUserLocation(location))
        }

        override fun changedFloor(floor: ProximiioFloor?) {
            super.changedFloor(floor)

            place.postValue(floor?.place)

            val previousUserLevel = userLevel.value!!
            val newUserLevel = floor?.floorNumber ?: 0

            userLevel.postValue(newUserLevel)
            proximiioMapbox!!.updateUserLevel(newUserLevel)
            if (previousUserLevel == displayLevel.value) {
                updateDisplayLevel(newUserLevel)
            }

            if (previousUserLevel != newUserLevel) {
                simulationProcessorRoutes()
            }
        }
    }
}