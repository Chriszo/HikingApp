package com.example.hikingapp.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.local.LocalDatabase

class RoutesProfileAdapter(
    private val context: Context?,
    private val routes: List<Route>,
    private val itemClickedListener: OnItemClickedListener,
    private val itemLongClickedListener: OnItemLongClickedListener
) : RecyclerView.Adapter<RoutesProfileAdapter.ViewHolder>() {


    init {
        inSelectMode = false
        selectedItems = mutableListOf<Int>()
        routesViewEnabled = true
    }

    companion object {
        var inSelectMode = false
        var selectedItems = mutableListOf<Int>()
        private const val longClick = "routeLongClick"
        private const val routeItems = "routeSelectedItems"
        var routesViewEnabled = true
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
                if (!inSelectMode && routesViewEnabled) {
                    it.setBackgroundColor(Color.rgb(0, 82, 204))
                    selectedItems.add(adapterPosition)
                    inSelectMode = true

                    val bundle = Bundle()
                    bundle.putBoolean(longClick, true)
                    bundle.putSerializable(routeItems, SelectedItemsWrapper(selectedItems))
                    this.itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                }
                true
            }

            view.setOnClickListener {

                if (routesViewEnabled) {

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
                                    routeItems,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            } else {
                                bundle.putBoolean(longClick, true)
                                bundle.putSerializable(
                                    routeItems,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            }

                        } else {
                            inSelectMode = false
                            itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                        }
                    } else {
                        bundle.putSerializable("class", Route::class.java.simpleName)
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectedItems.contains(position)) {
            holder.view.setBackgroundColor(Color.rgb(0, 82, 204))
        } else {
            if (selectedItems.isNullOrEmpty()) {
                val bundle = Bundle()
                bundle.putBoolean(longClick, false)
                bundle.putSerializable(routeItems, SelectedItemsWrapper(selectedItems))
                this.itemLongClickedListener.onItemLongClicked(position, bundle)
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT)
        }

        val route = routes[position]
//        holder.imageView.setImageResource(route.mainPhoto!!)
        holder.imageView.setImageDrawable(BitmapDrawable(context?.resources,LocalDatabase.getMainImage(route.routeId,Route::class.java.simpleName)))
        holder.nameView.text = route.routeName
        holder.stateView.text = route.stateName
        holder.ratingView.rating = route.routeInfo!!.rating!!

        val difficultyLevelText = route.routeInfo!!.difficultyLevel!!.difficultyLevel


        holder.difficultyLevelView.text = difficultyLevelText
        when (route.routeInfo!!.difficultyLevel!!) {
            DifficultyLevel.EASY -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_green)
            DifficultyLevel.MODERATE -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_yellow)
            DifficultyLevel.HARD -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_red)
        }

    }

    fun selectAllItems() {
        routes.withIndex().forEach {
            selectedItems.add(it.index)
            notifyItemChanged(it.index)
        }
    }

    fun setEnabled(isEnabled: Boolean) {
        routesViewEnabled = isEnabled
    }

    fun clearAllItems() {
        selectedItems.clear()
        inSelectMode = false
        notifyDataSetChanged()
    }


    override fun getItemCount() = routes.count()

}