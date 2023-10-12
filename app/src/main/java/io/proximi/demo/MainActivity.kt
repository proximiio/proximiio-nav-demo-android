package io.proximi.demo

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.Preferences
import io.proximi.demo.ui.RouteMapLayerHelper
import io.proximi.demo.ui.privacypolicy.PrivacyPolicyFragmentDirections
import io.proximi.mapbox.data.model.Feature
import io.proximi.mapbox.library.SyncStatus
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navigationController: NavController
    lateinit var mapModeHelper: MapModeHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var routeMapLayerHelper: RouteMapLayerHelper
    private lateinit var customMarkerHelper: CustomMarkerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(MainViewModel::class.java)

        // Setup Logo in the appbar
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setLogo(R.mipmap.logo_proximiio)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
        setContentView(R.layout.main_activity)
        title = ""
        initializeNavigation()

        if (Preferences(baseContext).getPrivacyPolicyAccepted()) {
            onPrivacyPolicyAccepted(savedInstanceState)
        }
    }

    override fun onNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.navHostFragment)
            .navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        viewModel.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        viewModel.onStop()
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ProximiioHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ProximiioHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> return onNavigateUp()
            R.id.menu_settings -> navHostFragment.navController.navigate(R.id.action_global_settingsFragment)
                .also { return true }
            R.id.menu_close -> navHostFragment.navController.popBackStack().also { return true }
        }
        return super.onOptionsItemSelected(item)
    }

    /* ****************************************************************************************** */

    private fun initializeNavigation() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navigationController = navHostFragment.navController
        val appbarConfiguration = AppBarConfiguration.Builder(
            R.id.mainFragment,
            R.id.privacyNotificationFragment
        ).build()
        setupActionBarWithNavController(this, navigationController, appbarConfiguration)
        if (Preferences(baseContext).getPrivacyPolicyAccepted()) {
            navigationController.navigate(PrivacyPolicyFragmentDirections.actionGlobalMainFragment())
        }
    }


    fun onPrivacyPolicyAccepted(savedInstanceState: Bundle? = null) {
        ProximiioHelper.initialize(this)
        initializeMap(savedInstanceState)
        ProximiioHelper.getMapInstance()!!.onStart()
        ProximiioHelper.getApiInstance()!!.onStart()
    }

    private fun initializeMap(savedInstanceState: Bundle?) {
        mapModeHelper = MapModeHelper(baseContext)
        ProximiioHelper.setupMap(mapView, savedInstanceState) {
            mapView.getMapAsync { mapboxMap ->
                mapboxMap.addOnMapClickListener { onMapClicked(mapboxMap, it) }
                mapModeHelper.onMapInit(mapboxMap)
                // configure mapbox UI
                mapboxMap.uiSettings.attributionGravity =
                    Gravity.CENTER_VERTICAL or GravityCompat.START
                mapboxMap.uiSettings.logoGravity = Gravity.CENTER_VERTICAL or GravityCompat.START
                mapboxMap.uiSettings.isCompassEnabled = false
                // Overriding default logo drawable, we need to adjust margins
                val margin = resources.getDimension(R.dimen.mapbox_four_dp).toInt()
                val topMargin = resources.getDimension(R.dimen.mapbox_ninety_two_dp).toInt() / 3 * 2
                mapboxMap.uiSettings.setAttributionMargins(margin, topMargin, margin, margin)
                initializeRoutePois(mapboxMap)

                ProximiioHelper.getMapInstance()?.let {
                    customMarkerHelper =
                        CustomMarkerHelper(
                            baseContext,
                            this,
                            mapboxMap,
                            it.style,
                            viewModel.markers
                        )
                }

                viewModel.markers.postValue(
                    listOf<com.mapbox.geojson.Feature>(
                        com.mapbox.geojson.Feature.fromGeometry(
                            Point.fromLngLat(24.921695923476054, 60.1671950369849)
                        ),
                        com.mapbox.geojson.Feature.fromGeometry(
                            Point.fromLngLat(24.921695923476054, 60.16746993048443)
                        ),
                        com.mapbox.geojson.Feature.fromGeometry(
                            Point.fromLngLat(24.921695923476054, 60.16714117139544)
                        )
                    )
                )
            }
        }
        initializeObservers()
        viewModel.mapboxSyncStatus.observe(this) { status ->
            if (
                status == SyncStatus.INITIAL_ERROR
                || status == SyncStatus.INITIAL_NETWORK_ERROR
            ) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.error_map_init)
                    .setPositiveButton(getString(R.string.error_map_init_retry)) { dialog, _ ->
                        ProximiioHelper.getMapInstance()!!.startSyncNow()
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun initializeObservers() {
        viewModel.userLocation.observe(this) {
            mapModeHelper.onLocationUpdate(it)
        }
        viewModel.displayLevel.observe(this) {
            toggleMapboxLocationComponentVisibility()
        }
        viewModel.userLevel.observe(this) {
            toggleMapboxLocationComponentVisibility()
        }
        viewModel.style.observe(this) { style ->
            if (style != null) {
                toggleMapboxLocationComponentVisibility()
            }
        }
        navigationController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.routeNavigationFragment) {
                mapModeHelper.onNavigationStart()
            } else {
                mapModeHelper.onNavigationEnd()
            }
        }
    }

    fun initializeRoutePois(mapboxMap: MapboxMap) {
        routeMapLayerHelper = RouteMapLayerHelper(
            baseContext,
            mapboxMap,
            this,
            viewModel.displayLevel,
            viewModel.style,
            viewModel.route,
            viewModel.routeUpdate
        )
    }

    private fun onMapClicked(mapboxMap: MapboxMap, latLng: LatLng): Boolean {
        return mapboxMap.queryRenderedFeatures(
            mapboxMap.projection.toScreenLocation(latLng),
            "proximiio-pois-icons"
        ) // , "proximiio-levelchangers"
            .map { poi -> ProximiioHelper.getPoiFeatures().firstOrNull { poi.id() == it.id } }
            .firstOrNull()
            ?.let { feature -> previewRouteToFeature(feature) } != null
    }

    private fun previewRouteToFeature(feature: Feature) {
        viewModel.routeFindAndPreview(feature)
        navigationController.navigate(MainNavigationDirections.actionGlobalRoutePreviewFragment())
    }

    private fun toggleMapboxLocationComponentVisibility() {
        val displayLevel = viewModel.displayLevel.value
        val userLevel = viewModel.userLevel.value
        mapView.getMapAsync { mapboxMap ->
            if (mapboxMap.locationComponent.isLocationComponentActivated) {
                mapboxMap.locationComponent.isLocationComponentEnabled =
                    (displayLevel == userLevel) // && inCoveredArea)
            }
        }
    }
}