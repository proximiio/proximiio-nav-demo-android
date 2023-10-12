package io.proximi.demo.ui.poidetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.proximi.demo.R
import kotlinx.android.synthetic.main.fragment_poi_detail_image_item.view.*
import kotlin.math.max

private const val TYPE_ITEM = 0
private const val TYPE_EMPTY = 1

class PoiDetailImageAdapter(private val imageUrlList: List<String>): RecyclerView.Adapter<PoiDetailImageAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (imageUrlList.isNotEmpty()) TYPE_ITEM else TYPE_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                if (viewType == TYPE_ITEM) R.layout.fragment_poi_detail_image_item else R.layout.fragment_poi_detail_item_empty,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return max(imageUrlList.size, 1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (imageUrlList.isNotEmpty()) {
            holder.loadImage(imageUrlList[position])
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun loadImage(url: String) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_image_placeholder_wrapped)
                .error(R.drawable.ic_broken_image)
                .into(itemView.titleView)
        }
    }
}
