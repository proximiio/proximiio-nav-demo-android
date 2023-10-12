package io.proximi.demo.ui.routeend

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.proximi.demo.R
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.MapControlsFragment
import kotlinx.android.synthetic.main.fragment_route_end.view.*

class RouteEndFragment : MapControlsFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_route_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.backToHomeButton.setOnClickListener {
            viewModel.routeClear()
            findNavController().navigate(RouteEndFragmentDirections.actionGlobalMainFragment())
        }
        view.closeButton.setOnClickListener {
            viewModel.routeClear()
            findNavController().navigate(RouteEndFragmentDirections.actionGlobalMainFragment())
        }
        viewModel.routeUpdate.observe(viewLifecycleOwner, { routeUpdate ->
            if (routeUpdate?.type?.isRouteEnd() == true) {
                view.titleTextView.text = routeUpdate.text
            } // else if (routeUpdate != null) {
//                error("Route should have ended!")
            //}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_open, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }
}