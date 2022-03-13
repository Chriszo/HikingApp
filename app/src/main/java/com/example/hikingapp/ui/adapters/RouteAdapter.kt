package com.example.hikingapp.ui.adapters

import android.content.Context
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

class RouteAdapter(
    val context: Context,
    private var indexesList: MutableList<Long>?,
    val routes: List<Route>,
    private val itemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {

    class ViewHolder(
        val view: View,
        var indexesList: MutableList<Long>?,
        private val itemClickedListener: OnItemClickedListener
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        var imageView: ImageView
        var nameView: TextView
        var stateView: TextView
        var ratingView: RatingBar
        var difficultyLevelView: TextView

        init {
            view.setOnClickListener(this)
            imageView = view.findViewById(R.id.route_imageView)
            nameView = view.findViewById(R.id.route_name)
            stateView = view.findViewById(R.id.route_state)
            ratingView = view.findViewById(R.id.routeRating)
            difficultyLevelView = view.findViewById(R.id.difficulty_level)
        }

        override fun onClick(v: View?) {
            val bundle = Bundle()
            bundle.putSerializable("class", Route::class.java.simpleName)
            if (indexesList.isNullOrEmpty()) {
                itemClickedListener.onItemClicked(adapterPosition, bundle)
            } else {
                itemClickedListener.onItemClicked(indexesList!![adapterPosition].toInt(), bundle)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false)
        return ViewHolder(view, indexesList, itemClickedListener)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        holder.imageView.setImageResource(routes[position].mainPhoto!!)
        holder.imageView.setImageDrawable(
            BitmapDrawable(
                context.resources,
                LocalDatabase.getMainImage(routes[position].routeId, Route::class.java.simpleName)
            )
        )
        holder.nameView.text = routes[position].routeName
        holder.stateView.text = context.getString(R.string.secondary_text, routes[position].stateName)
        holder.ratingView.rating = routes[position].routeInfo!!.rating!!

        val difficultyLevelText = routes[position].routeInfo!!.difficultyLevel!!.difficultyLevel


        holder.difficultyLevelView.text = difficultyLevelText
        when (routes[position].routeInfo!!.difficultyLevel!!) {
            DifficultyLevel.EASY -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_green)
            DifficultyLevel.MODERATE -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_yellow)
            DifficultyLevel.HARD -> holder.difficultyLevelView.background =
                ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_red)
        }
    }

    override fun getItemCount() = routes.count()
}