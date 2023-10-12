package io.proximi.demo.ui.search

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.squareup.picasso.Picasso
import io.proximi.demo.ExploreNearbyCategory
import io.proximi.demo.R
import io.proximi.demo.imageUrl
import io.proximi.demo.ui.MainViewModel
import io.proximi.mapbox.data.model.Feature
import jp.wasabeef.picasso.transformations.CropSquareTransformation
import jp.wasabeef.picasso.transformations.MaskTransformation
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.search_bar.*
import kotlinx.android.synthetic.main.search_bar.view.*
import kotlinx.android.synthetic.main.search_result_home_button.view.*
import kotlinx.android.synthetic.main.search_result_item.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

open class SearchFragment : Fragment(), CoroutineScope {

    private val VOICE_CODE = 10
    private val viewModel: MainViewModel by activityViewModels()
    private val adapter = SearchItemAdapter()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.searchRequestVoiceInput) {
            viewModel.searchRequestVoiceInput = false
            startVoiceInput()
        }
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_open, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.search_in)
        requireView().searchRecyclerView.adapter = adapter
        adapter.refreshPoiFeatureList() // TODO: 22.2.2021 is this call necessary?
        requireView().searchEditText.addTextChangedListener(searchFilterTextWatcher)
        viewModel.selectedExploreNearbyCategory.observe(viewLifecycleOwner) { category ->
            exploreNearbyCategoryUpdated(category)
        }
//        exploreNearbyCategoryUpdated(category)
        exploreNearbyCategoryChipView.setOnClickListener {
            viewModel.clearExploreNearbyCategory()
        }
        exploreNearbyCategoryChipView.setOnCloseIconClickListener {
            viewModel.clearExploreNearbyCategory()
        }

        startPostponedEnterTransition()
    }

    open fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireActivity())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        // Clear selected category when popped out
        viewModel.clearExploreNearbyCategory()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == VOICE_CODE && data != null) {
            val strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (strings != null && strings.size > 0) {
                searchEditText.setText(strings[0])
            }
        }
    }

    private val searchFilterTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(string: CharSequence?, p1: Int, p2: Int, p3: Int) {
            updateSearchFilterString(string?.toString())
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun updateSearchFilterString(string: String?) {
        adapter.updateFilter(string)
    }

    private fun navigateToPoi(feature: Feature) {
        viewModel.routeFindAndPreview(feature)
        requireView().findNavController()
            .navigate(SearchFragmentDirections.actionSearchFragmentToRoutePreviewFragment())
    }

    private fun exploreNearbyCategoryUpdated(category: ExploreNearbyCategory?) {
        adapter.updateAmenityFilter(category?.amenityId)
        if (category != null) {
            exploreNearbyCategoryChipView.visibility = View.VISIBLE
            exploreNearbyCategoryChipView.setText(category.titleId)
            exploreNearbyCategoryChipView.setBackgroundColor(category.backgroundColorId)
        } else {
            exploreNearbyCategoryChipView.visibility = View.GONE
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        startActivityForResult(intent, VOICE_CODE)
    }

    private inner class SearchItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var fullPoiFeatureList = listOf<Feature>()
        private var poiFeatureList = listOf<Feature>()
        private var titleFilter: String? = null
        private var amenityFilter: String? = null
        private val TYPE_COUNT = 0
        private val TYPE_ITEM = 1
        private val TYPE_NO_RESULTS = 2

        //        private val TYPE_BUTTON_HOME = 3
        private var searchJob: Job? = null
        private var searchFilter = SearchFilter()

        fun updateFilter(filter: String?) {
            this.titleFilter = filter
            refreshPoiFeatureList()
        }

        fun updateAmenityFilter(amenityId: String?) {
            this.amenityFilter = amenityId
            refreshPoiFeatureList()
        }

        fun refreshPoiFeatureList() {

            // Create search block to not stall UI thread
            searchJob?.cancel()
            searchJob = launch(Dispatchers.Default) {
                val featureList = viewModel.search(searchFilter, titleFilter, amenityFilter)
                    .sortedBy { it.getTitle() }
                withContext(Dispatchers.Main) {
                    poiFeatureList = featureList
                    searchJob = null
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_ITEM) {
                SearchItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_result_item, parent, false)
                )
            } else if (viewType == TYPE_NO_RESULTS) {
                SearchItemNoResultsViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_no_result_item, parent, false)
                )
            } else if (viewType == TYPE_COUNT) {
                SearchItemCounter(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_result_count, parent, false)
                )
            } else {
                error("")
//                SearchItemButtonHomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_result_home_button, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (poiFeatureList.isEmpty()) {
                TYPE_NO_RESULTS
            } else {
                when (position) {
                    0 -> TYPE_COUNT
//                    poiFeatureList.size + 1 -> TYPE_BUTTON_HOME
                    else -> TYPE_ITEM
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is SearchItemViewHolder -> holder.load(poiFeatureList[position - 1], titleFilter)
//                is SearchItemButtonHomeViewHolder -> holder.setup()
                is SearchItemNoResultsViewHolder -> holder.setup()
                is SearchItemCounter -> holder.setup(poiFeatureList.size)
            }
        }

        override fun getItemCount(): Int {
            return if (poiFeatureList.isEmpty()) {
                1
            } else {
                poiFeatureList.size + 1
            }
        }
    }

    private inner class SearchItemCounter(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setup(size: Int) {
            (itemView as TextView).text =
                resources.getQuantityString(R.plurals.item_count, size, size)
        }
    }

    private inner class SearchItemNoResultsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun setup() {
            itemView.homeButton.setOnClickListener {
                findNavController().navigate(SearchFragmentDirections.actionGlobalMainFragment())
            }
        }
    }

//    private inner class SearchItemButtonHomeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        fun setup() {
//            itemView.homeButton.setOnClickListener {
//                // TODO: 4.1.2021 setup action
//            }
//        }
//    }

    private inner class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun load(itemFeature: Feature, filterTitle: String?) {
            val picasso = Picasso.get()
            picasso.cancelRequest(itemView.imageView)
            itemView.imageView.background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_poi_detail_background,
                requireContext().theme
            )
            itemView.imageView.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            if (itemFeature.imageUrl == null) {
                val amenityIconBitmap = itemFeature.amenity?.getIconBitMap()
                if (amenityIconBitmap != null) {
                    itemView.imageView.setImageBitmap(amenityIconBitmap)
                } else {
                    itemView.imageView.setImageResource(R.drawable.ic_image_dummy)
                }
            } else {
                val url = itemFeature.imageUrl
                picasso
                    .load(url)
                    // TODO: 4.1.2021 replace dummy images
                    .placeholder(R.drawable.ic_image_dummy)
                    .error(R.drawable.ic_image_dummy)
                    .transform(
                        listOf(
                            CropSquareTransformation(),
                            MaskTransformation(itemView.context, R.drawable.mask_search_image)
                        )
                    )
                    .into(itemView.imageView)
            }

            itemView.subtitleTextView.text = null
            itemView.subtitleTextView.visibility = View.GONE
            val title = if (!filterTitle.isNullOrEmpty()) {
                val keywords =
                    itemFeature.getMetadata()?.getAsJsonArray("keywords")?.map { it.asString }
                val matchingKeyword = keywords?.firstOrNull { it.contains(filterTitle, true) }
                if (matchingKeyword != null) {
                    itemView.subtitleTextView.visibility = View.VISIBLE
                    itemView.subtitleTextView.text = itemFeature.getTitle()
                    matchingKeyword
                } else {
                    itemFeature.getTitle()
                }
            } else {
                itemFeature.getTitle()
            }


            itemView.titleTextView.text = title
            // TODO: 4.1.2021 load proper data here
            itemView.floorTextView.text =
                resources.getStringArray(R.array.floors)[resources.getIntArray(R.array.floors_levels)
                    .indexOf(itemFeature.getLevel() ?: 0)]
            // TODO: 4.1.2021 setup action here
            itemView.setOnClickListener {
                navigateToPoi(itemFeature)
            }
        }
    }
}