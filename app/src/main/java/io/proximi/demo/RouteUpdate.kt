package io.proximi.demo

import io.proximi.mapbox.library.RouteUpdateData
import io.proximi.mapbox.library.RouteUpdateType

data class RouteUpdate(
    val type: RouteUpdateType,
    val text: String?,
    val additionalText: String?,
    val data: RouteUpdateData?
)