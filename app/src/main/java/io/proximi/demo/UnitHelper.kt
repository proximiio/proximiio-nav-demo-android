package io.proximi.demo

import android.content.res.Resources
import kotlin.math.roundToInt

object UnitHelper {

    val STEP_LENGTH = 0.65 // meters
    val WALKING_SPEED = 1.4 // meters per second
    val METER_TO_STEP_COEFFICIENT get() = 1.0 / STEP_LENGTH

    fun convertMetersToStep(meters: Double): Int {
        return (meters / STEP_LENGTH).roundToInt()
    }

    fun distanceToSeconds(distance: Double): Int {
        return (distance / WALKING_SPEED).roundToInt()
    }

    fun timeEstimateString(distanceInMeters: Double, resources: Resources): String {
        val minutesRemaining = (distanceToSeconds(distanceInMeters) / 60.0).roundToInt()
        return resources.getQuantityString(R.plurals.minutes, minutesRemaining, minutesRemaining)
    }

    fun distanceEstimateString(distanceInMeters: Double, resources: Resources): String {
        val meters = distanceInMeters.roundToInt()
        return resources.getQuantityString(R.plurals.meters, meters, meters)
    }

    fun estimateString(distanceInMeters: Double, resources: Resources): String {
        return resources.getString(
            R.string.preview_estimate,
            timeEstimateString(distanceInMeters, resources),
            distanceEstimateString(distanceInMeters, resources)
        )
    }
}