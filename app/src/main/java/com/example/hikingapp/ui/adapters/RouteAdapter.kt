package com.example.hikingapp.ui.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.route.Route

class RouteAdapter(
    val routes: List<Route>,
    private val itemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {

    class ViewHolder(val view: View, private val itemClickedListener: OnItemClickedListener) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        var imageView: ImageView
        var nameView: TextView
        var stateView: TextView
        var ratingView: RatingBar
        var difficultyLevelView: TextView
        var bundle: Bundle = Bundle()

        init {
            view.setOnClickListener(this)
            imageView = view.findViewById(R.id.route_imageView)
            nameView = view.findViewById(R.id.route_name)
            stateView = view.findViewById(R.id.route_state)
            ratingView = view.findViewById(R.id.routeRating)
            difficultyLevelView = view.findViewById(R.id.difficulty_level)
        }

        override fun onClick(v: View?) {
            itemClickedListener.onItemClicked(adapterPosition, bundle)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false)
        return ViewHolder(view, itemClickedListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setImageResource(routes[position].mainPhoto!!)
        holder.nameView.text = routes[position].routeName
        holder.stateView.text = routes[position].stateName
        holder.ratingView.rating = routes[position].routeInfo!!.rating!!

        val difficultyLevelText = routes[position].routeInfo!!.difficultyLevel!!.difficultyLevel


        holder.difficultyLevelView.text = difficultyLevelText
        when (routes[position].routeInfo!!.difficultyLevel!!) {
            DifficultyLevel.EASY -> holder.difficultyLevelView.background = ContextCompat.getDrawable(holder.view.context,R.drawable.rounded_green)
            DifficultyLevel.MODERATE -> holder.difficultyLevelView.background = ContextCompat.getDrawable(holder.view.context,R.drawable.rounded_yellow)
            DifficultyLevel.HARD -> holder.difficultyLevelView.background = ContextCompat.getDrawable(holder.view.context,R.drawable.rounded_red)
        }
    }

    override fun getItemCount() = routes.count()
}