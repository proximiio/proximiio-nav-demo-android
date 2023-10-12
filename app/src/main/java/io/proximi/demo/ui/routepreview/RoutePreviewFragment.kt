package io.proximi.demo.ui.routepreview

import android.animation.LayoutTransition
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import io.proximi.demo.*
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.MapControlsFragment
import io.proximi.demo.ui.poidetail.PoiDetailImageAdapter
import io.proximi.mapbox.data.model.Feature
import io.proximi.mapbox.library.Route
import kotlinx.android.synthetic.main.fragment_route_preview.*
import kotlinx.android.synthetic.main.fragment_route_preview.view.*


class RoutePreviewFragment : MapControlsFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LinearSnapHelper().apply { attachToRecyclerView(view.imageRecyclerView) }
        view.stepsRecyclerView.adapter = RouteStepsAdapter()
        (view as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        view.navigateButton.setOnClickListener {
            viewModel.routeStart()
            findNavController().navigate(RoutePreviewFragmentDirections.actionRoutePreviewFragmentToRouteNavigationFragment())
        }
        view.closeButton.setOnClickListener {
            viewModel.routeCancel()
        }
        view.showRouteButton.setOnClickListener {
            toggleShowRoute()
//            if (readMoreGroup.visibility == View.VISIBLE) {
//                toggleReadMoreVisibility()
//            }
        }

        viewModel.routeUpdate.observe(viewLifecycleOwner) { routeUpdate ->
            if (routeUpdate?.type?.isRouteEnd() == true) {
                findNavController().navigate(RoutePreviewFragmentDirections.actionGlobalRouteEndFragment())
            }
        }
        viewModel.route.observe(viewLifecycleOwner) { route ->
            if (route == null) {
                view.previewInfoGroup.visibility = View.GONE
                view.calculatingGroup.visibility = View.VISIBLE
            } else {
                view.previewInfoGroup.visibility = View.VISIBLE
                view.calculatingGroup.visibility = View.GONE
            }
            route?.let {
                viewModel.selectedPoiFeature.value?.let {
                    setupFeatureInfo(route, it)
                }
            }
            (view.stepsRecyclerView.adapter as RouteStepsAdapter).updateRoute(route)
        }
        viewModel.selectedPoiFeature.observe(viewLifecycleOwner) { feature ->
            feature?.let {
                viewModel.route.value?.let { route ->
                    setupFeatureInfo(route, feature)
                }
            }
        }

        requireView().readMoreButton.setOnClickListener {
            toggleReadMoreVisibility()
//            if (stepsRecyclerView.visibility == View.VISIBLE) {
//                toggleShowRoute()
//            }
        }
    }

    private fun setupFeatureInfo(route: Route, feature: Feature) {
        val openHours = feature.openHours
        val description = feature.description
        val link = feature.link

        titleTextView.text = feature.getTitle()
        distanceTextView.text = UnitHelper.estimateString(route.distanceMeters, resources)
        descriptionTextView.text = description
        openingHoursTextView.text = openHours
        imageRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        imageRecyclerView.adapter = PoiDetailImageAdapter(feature.imageUrlList)

        if (link != null) {
            linkButton.text = link.first
            linkButton.setOnClickListener {
                try {
                    CustomTabsIntent.Builder()
                        .build()
                        .launchUrl(requireContext(), Uri.parse(link.second))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.data = Uri.parse(link.second)
//                startActivity(intent)
            }
        }

        readMoreButton.visibility =
            if (description != null || openHours != null || link != null || feature.imageUrlList.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun toggleReadMoreVisibility() {
        if (readMoreGroup.visibility == View.VISIBLE) {
            readMoreGroup.visibility = View.GONE
            readMoreButton.setText(R.string.route_preview_read_more)
        } else {
            readMoreGroup.visibility = View.VISIBLE
            openingHoursGroup.visibility =
                if (openingHoursTextView.text.isNullOrEmpty()) View.GONE else View.VISIBLE
            descriptionTextView.visibility =
                if (descriptionTextView.text.isNullOrEmpty()) View.GONE else View.VISIBLE
            linkButton.visibility = if (linkButton.text.isNullOrEmpty()) View.GONE else View.VISIBLE
            readMoreButton.setText(R.string.route_preview_show_less)
        }
    }

    private fun toggleShowRoute() {
        stepsRecyclerView.visibility = if (stepsRecyclerView.visibility == View.VISIBLE) {
            requireView().showRouteButton.setText(R.string.preview_show_steps)
            View.GONE
        } else {
            requireView().showRouteButton.setText(R.string.preview_hide_steps)
            View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_open, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }
}