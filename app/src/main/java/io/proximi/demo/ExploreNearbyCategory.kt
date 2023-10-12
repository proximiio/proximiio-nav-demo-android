package io.proximi.demo

data class ExploreNearbyCategory(
        val amenityId: String = "",
        val iconDrawableId: Int,
        val textColorId: Int,
        val backgroundColorId: Int,
        val titleId: Int,
        val searchBarTitleId: Int,
        var available: Int = 16,
        var total: Int = 24,
)

val exploreNearbyCategoryList = arrayListOf(
        ExploreNearbyCategory(
                "c1eaab1a-3f02-4491-a515-af8d628f74fb:20b56e81-a640-4d59-aaab-9cdbe2b353d1",
                R.drawable.ic_explore_nearby_bathroom,
                R.color.white,
                R.color.exploreNearbyBathroom,
                R.string.explore_nearby_bathrooms,
                R.string.resource_type_search_bar_1
        ),
        ExploreNearbyCategory(
                "c1eaab1a-3f02-4491-a515-af8d628f74fb:109c0242-6346-4333-b6a9-8315841a82a9",
                R.drawable.ic_explore_nearby_cafe,
                R.color.white,
                R.color.colorCafeBackground,
                R.string.explore_nearby_cafes,
                R.string.resource_type_search_bar_2
        ),
        ExploreNearbyCategory(
                "c1eaab1a-3f02-4491-a515-af8d628f74fb:9da478a4-b0ce-47ba-8b44-32a4b31150a8",
                R.drawable.ic_explore_nearby_parking,
                R.color.white,
                R.color.exploreNearbyParking,
                R.string.explore_nearby_parking,
                R.string.resource_type_search_bar_3
        ),
        ExploreNearbyCategory(
                "c1eaab1a-3f02-4491-a515-af8d628f74fb:b2b59e42-de48-442c-b591-a5f8fbc5031d",
                R.drawable.ic_explore_nearby_exits,
                R.color.white,
                R.color.exploreNearbyExits,
                R.string.explore_nearby_exits,
                R.string.resource_type_search_bar_4
        ),
        ExploreNearbyCategory(
                "c1eaab1a-3f02-4491-a515-af8d628f74fb:65a02cc9-2c78-4ace-8105-5cf5b27f4a6e",
                R.drawable.ic_explore_nearby_meeting_room,
                R.color.white,
                R.color.exploreNearbyMeetingRoom,
                R.string.explore_nearby_meeting_rooms,
                R.string.resource_type_search_bar_4
        ),
)