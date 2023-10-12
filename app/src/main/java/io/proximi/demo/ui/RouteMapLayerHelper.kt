package io.proximi.demo.ui

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.proximi.demo.ProximiioHelper
import io.proximi.demo.R
import io.proximi.demo.RouteUpdate
import io.proximi.mapbox.library.Route

private val MAPBOX_BASE_LAYER = "proximiio-pois-icons"
private val SOURCE_ROUTE = "io.demo.proximi.routepoints.source"
private val INPUT_ROUTE = "io.demo.proximi.routepoints.source"
private val WAYPOINT_IMAGE_ID = "io.demo.proximi.routepoints.waypoint.image_id"
private val DESTINATION_IMAGE_ID = "io.demo.proximi.routepoints.routepoints.destination.image_id"

class RouteMapLayerHelper(
    private val context: Context,
    private val mapboxMap: MapboxMap,
    private val lifecycleOwner: LifecycleOwner,
    private var displayLevel: LiveData<Int>,
    private var style: LiveData<String?>,
    private var route: LiveData<Route?>,
    private var routeUpdate: LiveData<RouteUpdate?>
) {

    private val routeObserver = Observer<Route?> { route ->
        mapboxMap.getStyle { style ->
            val source = style.getSource(SOURCE_ROUTE) as GeoJsonSource? ?: GeoJsonSource(SOURCE_ROUTE).apply { style.addSource(this) }
            val iconLayer = style.getLayer(INPUT_ROUTE) ?: SymbolLayer(INPUT_ROUTE, SOURCE_ROUTE).withProperties(
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                PropertyFactory.iconImage(Expression.get("image")),
                // "icon-size":["interpolate",["exponential",0.5],["zoom"],17,0.1,22,0.5]
                PropertyFactory.iconSize(Expression.raw("[\"interpolate\",[\"exponential\",0.5],[\"zoom\"],17,0.35,22,1.75]")),
                PropertyFactory.iconAllowOverlap(true)
            ).apply {
                this.minZoom = 17.0f
                this.maxZoom = 24.0f
                style.addLayerBelow(this, MAPBOX_BASE_LAYER)
            }
            iconLayer.setProperties(
                PropertyFactory.visibility(if (route!= null && routeUpdate.value != null && !routeUpdate.value!!.type.isRouteEnd()) Property.VISIBLE else Property.NONE)
            )

            // Add Waypoint image if not exists
            if (style.getImage(WAYPOINT_IMAGE_ID) == null) {
                style.addImage(WAYPOINT_IMAGE_ID, ResourcesCompat.getDrawable(context.resources, R.drawable.waypoint_marker_highlight, null)!!)
            }
            // Add Destination image if not exists
            if (style.getImage(DESTINATION_IMAGE_ID) == null) {
                style.addImage(DESTINATION_IMAGE_ID, ResourcesCompat.getDrawable(context.resources, R.drawable.destination_marker_highlight, null)!!)
            }
            // Create and inject points
            val pointList = mutableListOf<Feature>()
            route?.let {
                val waypoints = route.nodeList
                    .filter { it.isWaypoint }
                    .filter { it.level == displayLevel.value }
                    .map { routeNode -> ProximiioHelper.getFeatureList().value?.firstOrNull { it.id == routeNode.waypointId } }
                    .filterNotNull()
                    .map { waypoint ->
                        Feature.fromGeometry(waypoint.featureGeometry as Point).apply {
                            addStringProperty("image", WAYPOINT_IMAGE_ID)
                        }
                    }
                pointList.addAll(waypoints)

                val destination = Feature.fromGeometry(route.destinationPoint)
                    .apply {
                        addStringProperty("image", DESTINATION_IMAGE_ID)
                    }
                if (displayLevel.value == route.nodeList.last().level) {
                    pointList.add(destination)
                }
            }
            source.setGeoJson(FeatureCollection.fromFeatures(pointList))
        }
    }

    private fun areMarkersVisible(displayLevel: Int?, poiLevel: Int, routeUpdate: RouteUpdate?): Boolean {
        return displayLevel ?: 0 == poiLevel && routeUpdate != null && !routeUpdate.type.isRouteEnd()
    }

    init {
        style.observe(lifecycleOwner) {
            if (it != null) {
                routeObserver.onChanged(route.value)
            }
        }
        displayLevel.observe(lifecycleOwner) {
            routeObserver.onChanged(route.value)
        }
        routeUpdate.observe(lifecycleOwner) {
            routeObserver.onChanged(route.value)
        }
        route.observe(lifecycleOwner, routeObserver)
    }
}