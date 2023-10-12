package io.proximi.demo.ui.routenavigation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.proximi.demo.R
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.MapControlsFragment
import io.proximi.demo.ui.routepreview.getPreviewDrawable
import kotlinx.android.synthetic.main.fragment_route_navigation.*
import kotlinx.android.synthetic.main.home_fragment.*
import java.util.*

class RouteNavigationFragment : MapControlsFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton.setOnClickListener {
            viewModel.routeCancel()
        }
        viewModel.routeUpdate.observe(viewLifecycleOwner, { update ->
            update?.let {
                if (update.type.isRouteEnd()) {
                    findNavController().navigate(RouteNavigationFragmentDirections.actionGlobalRouteEndFragment())
                } else {
                    navigationTextView.text = update.text
                    navigationImageView.setImageResource(update.data?.stepDirection?.getPreviewDrawable(requireContext()) ?: R.drawable.ic_destination)
                }
            }

//            update?.data?.nodeIndex?.let { currentNodeIndex ->
//                adapter.updateNodeIndex(currentNodeIndex)
//                (view.stepsRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(0)
//            }
        })

//        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
//            if (location == null) {
//                debugTextView.text = ""
//                return@observe
//            }
//            val accuracy = String.format("%.2f m", location.accuracy)
//            val source = location.extras?.getString("io.proximi.proximiiolibrary.type") ?: "[unknown]"
//            val time = SimpleDateFormat("HH:MM:SS.SS", Locale.US).format(Date())
//            val inputDistanceMap = location.extras?.getSerializable("io.proximi.proximiiolibrary.inputs") as HashMap<String, Double>?
//            val inputs = viewModel.inputs
//            val inputString = inputDistanceMap?.entries?.joinToString(separator = "\n") { inputEntry ->
//                val input = inputs.firstOrNull { it.id == inputEntry.key }
//                val inputName = input?.name ?: "[unknown]"
//                return@joinToString String.format("%s (%d): %.1f", inputName,  input?.floor?.floorNumber ?: 0, inputEntry.value)
//            }
//            val transitional = location.extras?.get("io.proximi.proximiiolibrary.transitional")
//            val moving = location.extras?.get("io.proximi.proximiiolibrary.deviceMoving")
//            debugTextView.text = "$inputString\ntransitional: $transitional\nmoving: $moving\naccuracy: $accuracy\nsource: $source\ntime: $time"
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_open, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }
}