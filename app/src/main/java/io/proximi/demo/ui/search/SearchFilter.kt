package io.proximi.demo.ui.search

import io.proximi.mapbox.data.model.Feature
import io.proximi.mapbox.library.ProximiioFeatureType
import io.proximi.mapbox.library.ProximiioSearchFilter

/**
 * Custom implementation of Proximi.io [ProximiioSearchFilter].
 * Providing custom filter to search allows us to override search behaviour when searching features.
 */
class SearchFilter: ProximiioSearchFilter {

    private val allowedFeatureTypes = arrayOf(
        ProximiioFeatureType.POI,
//        ProximiioFeatureType.ELEVATOR,
//        ProximiioFeatureType.ESCALATOR,
//        ProximiioFeatureType.STAIRCASE
    )

    /**
     * Tag that will show up in proximi.io analytics tools.
     */
    override fun tag(): String {
        return "search"
    }

    /**
     * Names of inputs used to filter items. This is used to name search values in proximi.io analytics tools.
     */
    override fun inputNames(): Array<String> {
        return arrayOf("title", "amenityCategoryId")
    }

    /**
     * Actual filter method used when searching for features.
     * @param feature feature to test if it fits
     * @param input values to match feature with
     *
     * @return true if feature should be included in search results.
     */
    override fun filterItem(feature: Feature, vararg input: String?): Boolean {
        val name = input[0]
        val amenityCategory = input[1]

        return (
            isAllowedType(feature)
            && (
                name.isNullOrEmpty()
                || matchesName(feature, name)
            )
            && (
                amenityCategory.isNullOrEmpty()
                || amenityCategory == feature.amenityId
            )
        )
    }

    private fun matchesName(feature: Feature, name: String): Boolean {
        if (feature.getTitle()?.contains(name, true) == true) {
            return true
        }

        val keywords = feature.getMetadata()?.getAsJsonArray("keywords")?.map { it.asString }
        val keywordMatch = keywords?.firstOrNull { it.contains(name, true) }
        return keywordMatch != null
    }

    private fun isAllowedType(it: Feature): Boolean {
        return allowedFeatureTypes.contains(it.getType())
    }
}