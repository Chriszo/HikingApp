package com.example.hikingapp.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.culture.Sight

class SightsProfileAdapter(
    private val context: Context?,
    private val sights: List<Sight>,
    private val itemClickedListener: OnItemClickedListener,
    private val itemLongClickedListener: OnItemLongClickedListener
) : RecyclerView.Adapter<SightsProfileAdapter.ViewHolder>() {


    init {
        inSelectMode = false
        selectedItems = mutableListOf<Int>()
        sightsViewEnabled = true
    }

    companion object {
        var inSelectMode = false
        var selectedItems = mutableListOf<Int>()
        var sightsViewEnabled = true
        private const val longClick = "sightLongClick"
        private const val sightItems = "sightSelectedItems"
    }

    class ViewHolder(
        val view: View,
        private val itemClickedListener: OnItemClickedListener,
        private val itemLongClickedListener: OnItemLongClickedListener
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        var imageView: ImageView
        var nameView: TextView
        var stateView: TextView
        var ratingView: RatingBar
        var difficultyLevelView: TextView

        init {

            view.setOnLongClickListener {
                if (!inSelectMode && sightsViewEnabled) {
                    it.setBackgroundColor(Color.rgb(0, 82, 204))
                    selectedItems.add(adapterPosition)
                    inSelectMode = true

                    val bundle = Bundle()
                    bundle.putBoolean(longClick, true)
                    bundle.putSerializable(sightItems, SelectedItemsWrapper(selectedItems))
                    this.itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                }
                true
            }

            view.setOnClickListener {

                if (sightsViewEnabled) {

                    val bundle = Bundle()
                    if (inSelectMode) {
                        if (selectedItems.size > 0) {
                            if (selectedItems.contains(adapterPosition)) {
                                it.setBackgroundColor(Color.TRANSPARENT)
                                selectedItems.remove(adapterPosition)
                            } else {
                                selectedItems.add(adapterPosition)
                                it.setBackgroundColor(Color.rgb(0, 82, 204))
                            }

                            if (selectedItems.size == 0) {
                                inSelectMode = false
                                bundle.putBoolean(longClick, false)
                                bundle.putSerializable(
                                    sightItems,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            } else {
                                bundle.putBoolean(longClick, true)
                                bundle.putSerializable(
                                    sightItems,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            }
                        } else {
                            inSelectMode = false
                            itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                        }
                    } else {
                        bundle.putSerializable("class", Sight::class.java.simpleName)
                        itemClickedListener.onItemClicked(adapterPosition, bundle)
                    }
                }
            }

            imageView = view.findViewById(R.id.route_imageView)
            nameView = view.findViewById(R.id.route_name)
            stateView = view.findViewById(R.id.route_state)
            ratingView = view.findViewById(R.id.routeRating)
            difficultyLevelView = view.findViewById(R.id.difficulty_level)
        }

        override fun onClick(v: View?) {
        }

        override fun onLongClick(v: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_route_item, parent, false)
        return ViewHolder(view, itemClickedListener, itemLongClickedListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectedItems.contains(position)) {
            holder.view.setBackgroundColor(Color.rgb(0, 82, 204))
        } else {
            if (selectedItems.isNullOrEmpty()) {
                val bundle = Bundle()
                bundle.putBoolean(longClick, false)
                bundle.putSerializable(sightItems, SelectedItemsWrapper(selectedItems))
                this.itemLongClickedListener.onItemLongClicked(position, bundle)
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT)
        }

        val sight = sights[position]
//        holder.imageView.setImageResource(sight.mainPhoto!!)
        holder.imageView.setImageDrawable(BitmapDrawable(context!!.resources, sight.mainPhoto))
        holder.nameView.text = sight.name
        holder.stateView.text = context.getString(R.string.secondary_text, sight.description)
        holder.ratingView.rating = sight.rating!!
        holder.difficultyLevelView.visibility = View.GONE

    }

    fun selectAllItems() {
        sights.withIndex().forEach {
            selectedItems.add(it.index)
            notifyItemChanged(it.index)
        }
    }

    fun clearAllItems() {
        selectedItems.clear()
        inSelectMode = false
        notifyDataSetChanged()
    }


    override fun getItemCount() = sights.count()

    fun setEnabled(isEnabled: Boolean) {
        sightsViewEnabled = isEnabled
    }

}