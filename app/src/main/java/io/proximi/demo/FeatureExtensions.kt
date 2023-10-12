package io.proximi.demo

import io.proximi.mapbox.data.model.Feature
import java.util.*

val Feature.available
    get() = true

val Feature.capacity
    get() = 10

val Feature.airQuality
    get() = 1

val Feature.soundLevel
    get() = 1

val Feature.amenityWifi
    get() = true

val Feature.amenityAc
    get() = true

val Feature.amenityTv
    get() = true

val Feature.amenityPlug
    get() = true

val Feature.imageUrl: String?
    get() {
        val urlList = getImageUrlList(TOKEN)
        return if (urlList.isNullOrEmpty()) {
            null
        } else {
            urlList[0]
        }
    }

val Feature.imageUrlList: List<String>
    get() {
        return getImageUrlList(TOKEN) ?: listOf()
    }

val Feature.description: String?
    get() {
        return if (
            getMetadata() != null
            && getMetadata()!!.has("description")
            && getMetadata()!!["description"].isJsonObject
            && getMetadata()!!["description"].asJsonObject.has(Locale.getDefault().language))
        {
            val description = getMetadata()!!["description"].asJsonObject[Locale.getDefault().language].asString
            if (description.isNullOrEmpty()) {
                null
            } else {
                description
            }
        } else {
            null
        }
    }

val Feature.link: Pair<String, String>?
    get() {
        return if (
            getMetadata() != null
            && getMetadata()!!.has("link")
            && getMetadata()!!["link"].isJsonObject
            && getMetadata()!!["link"].asJsonObject.has("url")
            && getMetadata()!!["link"].asJsonObject.has("title")
            && getMetadata()!!["link"].asJsonObject.get("title").isJsonObject
            && getMetadata()!!["link"].asJsonObject.get("title").asJsonObject.has(Locale.getDefault().language)
        ) {
            val url = getMetadata()!!["link"].asJsonObject.get("url").asString
            val title = getMetadata()!!["link"].asJsonObject.get("title").asJsonObject.get(Locale.getDefault().language).asString
            Pair<String, String>(title, url)
        } else {
            null
        }
    }


/**
 * Obtain 'open hours' from feature metadata.
 */
val Feature.openHours: String?
    get() {
        if (
            getMetadata() != null
            && getMetadata()!!.has("openHours")
            && getMetadata()!!["openHours"].isJsonObject
        ) {
            val hours = getMetadata()!!["openHours"].asJsonObject
            if (hours.size() == 7) {
                val dayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).toString()
                if (hours.has(dayOfWeek) && hours[dayOfWeek].isJsonObject) {
                    val hoursForDay = hours[dayOfWeek].asJsonObject
                    if (hoursForDay.has(Locale.getDefault().language)) {
                        return hoursForDay[Locale.getDefault().language].asString
                    }
                }
            }
        }
        return null
    }
