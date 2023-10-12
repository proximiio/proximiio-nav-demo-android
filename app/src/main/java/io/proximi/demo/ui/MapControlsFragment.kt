package io.proximi.demo.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.proximi.demo.MainActivity
import io.proximi.demo.ProximiioHelper
import io.proximi.demo.R
import kotlinx.android.synthetic.main.map_controls.*
import kotlinx.android.synthetic.main.map_controls.view.*
import kotlinx.android.synthetic.main.map_controls_floor.view.*

abstract class MapControlsFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProximiioHelper.getDisplayLevel().observe(viewLifecycleOwner) {
            val index = resources.getIntArray(R.array.floors_levels).indexOf(it)
            currentFloorTextView.text = resources.getStringArray(R.array.floors)[index]
        }

        view.floorSwitchRecyclerView?.apply {
            adapter = floorAdapter
            layoutManager = LinearLayoutManager(requireContext())
            view.floorSelectionCardView.setOnClickListener {
                if (floorSwitchRecyclerView.visibility == View.VISIBLE) {
                    floorSwitchRecyclerView.visibility = View.GONE
                } else {
                    floorSwitchRecyclerView.visibility = View.VISIBLE
                }
            }
        }
        view.locationFab?.let {
            (requireActivity() as MainActivity).mapModeHelper.initControls(view.locationFab, view.compassFab) {
                    true
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private val floorAdapter = object: RecyclerView.Adapter<FloorViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorViewHolder {
            return FloorViewHolder(LayoutInflater.from(context).inflate(R.layout.map_controls_floor, parent, false))
        }

        override fun onBindViewHolder(holder: FloorViewHolder, position: Int) {
            holder.setup(position)
        }

        override fun getItemCount(): Int {
            return resources.getStringArray(R.array.floors).size
        }
    }

    private inner class FloorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun setup(floorIndex: Int) {
            itemView.floorTextView.text = resources.getStringArray(R.array.floors)[floorIndex]
            itemView.floorCardView.setOnClickListener {
                ProximiioHelper.updateDisplayLevel(resources.getIntArray(R.array.floors_levels)[floorIndex])
                requireView().floorSwitchRecyclerView.visibility = View.GONE

            }
        }
    }

//    /**
//     * Listener for global layout changes in order to update [MainActivity]'s UI items that should float above
//     * this fragment.
//     */
//    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
//        Log.d("NavigationFragment", "global layout")
//        requireView().findViewById<View>(R.id.bottomBackgroundView)?.let { bottomBackgroundView ->
//            (requireActivity() as MainActivity).setBottomOffset(bottomBackgroundView.height)
//        }
//    }
}