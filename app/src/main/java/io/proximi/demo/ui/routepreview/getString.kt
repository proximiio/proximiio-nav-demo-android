package io.proximi.demo.ui.routepreview

import io.proximi.demo.R
import io.proximi.mapbox.library.RouteStepDirection

/**
 * Get description of route step direction as String resource ID.
 */
fun RouteStepDirection.getString(): Int {
    return when (this) {
        RouteStepDirection.START-> R.string.route_preview_turn_start
        RouteStepDirection.TURN_AROUND -> R.string.route_preview_turn_around
        RouteStepDirection.HARD_LEFT -> R.string.route_preview_turn_left_hard
        RouteStepDirection.LEFT -> R.string.route_preview_turn_left
        RouteStepDirection.SLIGHT_LEFT -> R.string.route_preview_turn_left_slight
        RouteStepDirection.STRAIGHT -> R.string.route_preview_turn_straight
        RouteStepDirection.SLIGHT_RIGHT -> R.string.route_preview_turn_right_slight
        RouteStepDirection.RIGHT -> R.string.route_preview_turn_right
        RouteStepDirection.HARD_RIGHT -> R.string.route_preview_turn_right_hard
        RouteStepDirection.UP_ELEVATOR -> R.string.route_preview_up_elevator
        RouteStepDirection.UP_ESCALATOR -> R.string.route_preview_up_escalator
        RouteStepDirection.UP_STAIRS -> R.string.route_preview_up_stairs
        RouteStepDirection.DOWN_ELEVATOR -> R.string.route_preview_down_elevator
        RouteStepDirection.DOWN_ESCALATOR -> R.string.route_preview_down_escalator
        RouteStepDirection.DOWN_STAIRS -> R.string.route_preview_down_stairs
        RouteStepDirection.FINISH -> R.string.route_preview_finish
        else -> error("No drawable available for levelDirection $this")
    }
}