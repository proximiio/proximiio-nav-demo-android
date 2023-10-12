package io.proximi.demo.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.proximi.demo.*
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.MapControlsFragment
import kotlinx.android.synthetic.main.explore_nearby_category.view.*
import kotlinx.android.synthetic.main.fragment_route_navigation.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.debugTextView
import kotlinx.android.synthetic.main.home_fragment.view.*
import kotlinx.android.synthetic.main.search_bar.view.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : MapControlsFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_open, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ensure search does not trigger keyboard
        view.searchEditText.isEnabled = false
        view.searchEditText.isFocusable = false
        view.searchEditText.isClickable = false
        view.searchEditText.isFocusableInTouchMode = false
        view.searchEditText.keyListener = null
        view.searchEditText.movementMethod = null
        view.searchLinearLayout.setOnClickListener {
            val action = HomeFragmentDirections.actionMainFragmentToSearchFragment()
            val extraInfo = FragmentNavigatorExtras(
//                    requireView().searchCardView to "searchCardView",
                    requireView().searchLinearLayout to "searchLinearLayout",
//                    requireView().searchTextInputLayout to "searchTextInputLayout",
                    requireView().searchEditText to "searchEditText",
                    requireView().voiceSearchButton to "voiceSearchButton",
//                    requireView().voiceSearchButton to "voiceSearchButton"
            )
            navigate(action, extraInfo)
        }
        view.voiceSearchButton.setOnClickListener {
            viewModel.searchRequestVoiceInput = true
            view.findNavController().navigate(HomeFragmentDirections.actionMainFragmentToSearchFragment())
        }

        view.resourceRecyclerView.adapter = adapter

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            if (location == null) {
                debugTextView.text = ""
                return@observe
            }
            val accuracy = String.format("%.2f m", location.accuracy)
            val source = location.extras?.getString("io.proximi.proximiiolibrary.type") ?: "[unknown]"
            val time = SimpleDateFormat("HH:MM:SS.SS", Locale.US).format(Date())
            val inputDistanceMap = location.extras?.getSerializable("io.proximi.proximiiolibrary.inputs") as HashMap<String, Double>?
            val inputs = viewModel.inputs
            val inputString = inputDistanceMap?.entries?.joinToString(separator = "\n") { inputEntry ->
                val input = inputs.firstOrNull { it.id == inputEntry.key }
                val inputName = input?.name ?: "[unknown]"
                return@joinToString String.format("%s (floor: %d)", inputName,  input?.floor?.floorNumber ?: 0)
            }
            val position = "${location.latitude},${location.longitude}"
            debugTextView.text = "$inputString\naccuracy: $accuracy\nsource: $source\nposition: $position\ntime: $time"
        }

    }

    private fun navigate(destination: NavDirections, extraInfo: FragmentNavigator.Extras) {
        with(findNavController()) {
            currentDestination?.getAction(destination.actionId)?.let {
                navigate(destination, extraInfo)
            }
        }
    }

    private val adapter = object: RecyclerView.Adapter<ResourceTypeViewHolder>() {
        private val resourceTypeList = io.proximi.demo.exploreNearbyCategoryList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceTypeViewHolder {
            return ResourceTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explore_nearby_category, parent, false))
        }

        override fun onBindViewHolder(holder: ResourceTypeViewHolder, position: Int) {
            holder.load(resourceTypeList[position])
        }

        override fun getItemCount(): Int {
            return resourceTypeList.size
        }
    }

    private inner class ResourceTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun load(category: ExploreNearbyCategory) {
            val backgroundColor = ContextCompat.getColor(requireContext(), category.backgroundColorId)
            (itemView as CardView).setBackgroundTintList(ColorStateList.valueOf(backgroundColor))
            itemView.imageView.setImageResource(category.iconDrawableId)
            itemView.titleTextView.setText(category.titleId)
            itemView.titleTextView.setTextColor(ContextCompat.getColor(requireContext(), category.textColorId))
            itemView.setOnClickListener {
                viewModel.selectExploreNearbyCategory(category)
                itemView.findNavController().navigate(HomeFragmentDirections.actionMainFragmentToSearchFragment())
            }
        }
    }
}