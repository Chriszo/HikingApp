package com.example.hikingapp.ui.adapters

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

class SightsAdapter(var sights: List<Sight>, var itemClicked: OnItemClickedListener?): RecyclerView.Adapter<SightsAdapter.ViewHolder>() {

    class ViewHolder(view: View, itemClicked: OnItemClickedListener): RecyclerView.ViewHolder(view), View.OnClickListener {

        private val mView = view
        private val itemClickListener = itemClicked
        var imageView: ImageView
        var nameView: TextView
        var stateView: TextView
        var ratingView: RatingBar
        var difficultyLevelView: TextView

        init {
            mView.setOnClickListener(this)
            imageView = view.findViewById(R.id.route_imageView)
            nameView = view.findViewById(R.id.route_name)
            stateView = view.findViewById(R.id.route_state)
            ratingView = view.findViewById(R.id.routeRating)
            difficultyLevelView = view.findViewById(R.id.difficulty_level)
        }

        override fun onClick(v: View?) {
            val bundle = Bundle()
            bundle.putSerializable("class", Sight::class.java.simpleName)
            itemClickListener.onItemClicked(adapterPosition, bundle)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_route_item, parent, false)

        return ViewHolder(view, itemClicked!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sight = sights[position]
        holder.imageView.setImageResource(sight.mainPhoto!!)
        holder.nameView.text = sight.name
        holder.stateView.text = sight.description
        holder.ratingView.rating = sight.rating!!
        holder.difficultyLevelView.visibility = View.GONE
    }

    override fun getItemCount() = sights.count()

}
