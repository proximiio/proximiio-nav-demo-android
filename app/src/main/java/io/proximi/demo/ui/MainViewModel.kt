package io.proximi.demo.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.proximi.demo.ExploreNearbyCategory
import io.proximi.demo.ProximiioHelper
import io.proximi.demo.RouteUpdate
import io.proximi.demo.ui.search.SearchFilter
import io.proximi.mapbox.data.model.Feature
import io.proximi.mapbox.library.Route
import io.proximi.mapbox.library.SyncStatus
import io.proximi.proximiiolibrary.ProximiioPlace

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var searchRequestVoiceInput = false

    /* ****************************************************************************************** */
    /* Live Data */

    private val selectedExploreNearbyCategoryLiveData =
        MutableLiveData<ExploreNearbyCategory?>(null)
    private val selectedPoiFeatureLiveData = MutableLiveData<Feature?>(null)

    val displayLevel: LiveData<Int> get() = ProximiioHelper.getDisplayLevel()
    val features: LiveData<List<Feature>> get() = ProximiioHelper.getFeatureList()
    val mapboxSyncStatus: LiveData<SyncStatus> get() = ProximiioHelper.getMapboxSyncStatus()
    val place: LiveData<ProximiioPlace?> get() = ProximiioHelper.getPlaceLiveData()
    val route: LiveData<Route?> get() = ProximiioHelper.getRouteLiveData()
    val routeUpdate: LiveData<RouteUpdate?> get() = ProximiioHelper.getRouteUpdateLiveData()
    val style: LiveData<String?> get() = ProximiioHelper.getMapInstance()!!.style
    val selectedExploreNearbyCategory: LiveData<ExploreNearbyCategory?> get() = selectedExploreNearbyCategoryLiveData
    val selectedPoiFeature: LiveData<Feature?> get() = selectedPoiFeatureLiveData
    val userLocation: LiveData<Location?> get() = ProximiioHelper.getUserLocation()
    val userLevel: LiveData<Int> get() = ProximiioHelper.getUserLevel()
    val visitorId = ProximiioHelper.getVisitorId()
    val inputs = ProximiioHelper.getInputs()
    val markers = MutableLiveData<List<com.mapbox.geojson.Feature>>().apply { postValue(listOf()) }

    fun selectExploreNearbyCategory(exploreNearbyCategory: ExploreNearbyCategory) {
        selectedExploreNearbyCategoryLiveData.postValue(exploreNearbyCategory)
    }

    fun clearExploreNearbyCategory() {
        selectedExploreNearbyCategoryLiveData.postValue(null)
    }

    fun selectPoi(feature: Feature) {
        selectedPoiFeatureLiveData.postValue(feature)
    }

    fun clearSelectedPoi() {
        selectedPoiFeatureLiveData.postValue(null)
    }

    /* ****************************************************************************************** */
    /* Lifecycle callback */

    fun onStart() {
        ProximiioHelper.getMapInstance()?.onStart()
    }

    fun onStop() {
        ProximiioHelper.getMapInstance()?.onStop()
    }

    override fun onCleared() {
        ProximiioHelper.mapDestroyed()
        super.onCleared()
    }

    /* ****************************************************************************************** */
    /* Navigation controls */

    fun routeStart() {
        ProximiioHelper.routeStart()
    }

    fun routeClear() {
        ProximiioHelper.routeClear()
        clearSelectedPoi()
    }

    fun routeCancel() {
        ProximiioHelper.routeCancel()
        clearSelectedPoi()
    }

    fun routeFindAndPreview(feature: Feature) {
        selectPoi(feature)
        ProximiioHelper.routeFindAndPreview(feature)
    }

    fun search(
        searchFilter: SearchFilter,
        titleFilter: String?,
        amenityFilter: String?
    ): List<Feature> {
        return ProximiioHelper.getMapInstance()!!.search(searchFilter, titleFilter, amenityFilter)
    }
}