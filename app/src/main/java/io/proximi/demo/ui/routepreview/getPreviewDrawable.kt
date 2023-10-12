package io.proximi.demo.ui.routepreview

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import io.proximi.demo.R
import io.proximi.mapbox.library.RouteStepDirection

/**
 * Get drawable resource ID [RouteStepDirection] shown in route preview.
 */
fun RouteStepDirection.getPreviewDrawable(context: Context): Int {
    return when (this) {
        RouteStepDirection.START-> R.drawable.ic_current_position
        RouteStepDirection.TURN_AROUND -> R.drawable.ic_turn_around
        RouteStepDirection.HARD_LEFT -> R.drawable.ic_turn_sharp_left
        RouteStepDirection.LEFT -> R.drawable.ic_turn_left
        RouteStepDirection.SLIGHT_LEFT -> R.drawable.ic_turn_slight_left
        RouteStepDirection.STRAIGHT -> R.drawable.ic_turn_straight
        RouteStepDirection.SLIGHT_RIGHT -> R.drawable.ic_turn_slight_right
        RouteStepDirection.RIGHT -> R.drawable.ic_turn_right
        RouteStepDirection.HARD_RIGHT -> R.drawable.ic_turn_sharp_right
        RouteStepDirection.UP_ELEVATOR -> R.drawable.ic_elevator_up
        RouteStepDirection.UP_ESCALATOR -> R.drawable.ic_stairs_up
        RouteStepDirection.UP_STAIRS -> R.drawable.ic_stairs_up
        RouteStepDirection.DOWN_ELEVATOR -> R.drawable.ic_elevator_down
        RouteStepDirection.DOWN_ESCALATOR -> R.drawable.ic_stairs_down
        RouteStepDirection.DOWN_STAIRS -> R.drawable.ic_stairs_down
        RouteStepDirection.FINISH -> R.drawable.ic_destination
        else -> error("No drawable available for direction $this")
    }
}

fun RouteStepDirection.getNotificationDrawable(): Int {
    return when (this) {
        RouteStepDirection.TURN_AROUND -> R.drawable.ic_turn_around
        RouteStepDirection.HARD_LEFT -> R.drawable.ic_turn_sharp_left
        RouteStepDirection.LEFT -> R.drawable.ic_turn_left
        RouteStepDirection.SLIGHT_LEFT -> R.drawable.ic_turn_slight_left
        RouteStepDirection.STRAIGHT -> R.drawable.ic_turn_straight
        RouteStepDirection.SLIGHT_RIGHT -> R.drawable.ic_turn_slight_right
        RouteStepDirection.RIGHT -> R.drawable.ic_turn_right
        RouteStepDirection.HARD_RIGHT -> R.drawable.ic_turn_sharp_right
        RouteStepDirection.UP_ELEVATOR -> R.drawable.ic_elevator_up
        RouteStepDirection.UP_ESCALATOR -> R.drawable.ic_stairs_up
        RouteStepDirection.UP_STAIRS -> R.drawable.ic_stairs_up
        RouteStepDirection.DOWN_ELEVATOR -> R.drawable.ic_elevator_down
        RouteStepDirection.DOWN_ESCALATOR -> R.drawable.ic_stairs_down
        RouteStepDirection.DOWN_STAIRS -> R.drawable.ic_stairs_down
        RouteStepDirection.FINISH -> R.drawable.ic_destination
        else -> error("No drawable available for levelDirection $this")
    }
}

/**
 * Convert attribute value to Color Int value.
 */
fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

/**
 * Convert attribute value to drawale resource ID.
 */
fun Context.getDrawableIdFromAttr(@AttrRes attrDrawable: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    theme.resolveAttribute(attrDrawable, typedValue, true)
    return typedValue.resourceId
}