package com.example.hikingapp.ui.adapters

import android.content.Context
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

class SightsAdapter(
    private val context: Context?,
    private var sights: List<Sight>,
    private var itemClicked: OnItemClickedListener?
) : RecyclerView.Adapter<SightsAdapter.ViewHolder>() {

    class ViewHolder(view: View, itemClicked: OnItemClickedListener) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        private val mView = view
        private val itemClickListener = itemClicked
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val sightNameView: TextView = view.findViewById(R.id.sight_name)
        val sightDescriptionView: TextView = view.findViewById(R.id.sight_description)
        val ratingView: RatingBar = view.findViewById(R.id.routeRating)
        private val bundle = Bundle()

        init {
            mView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            itemClickListener.onItemClicked(adapterPosition, bundle)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sight_item, parent, false)

        return ViewHolder(view, itemClicked!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setImageDrawable(
            BitmapDrawable(
                context!!.resources,
                sights[position].mainPhoto
            )
        )
        holder.sightNameView.text = sights[position].name
        holder.sightDescriptionView.text = sights[position].description
        holder.ratingView.rating = sights[position].rating!!
    }

    override fun getItemCount() = sights.count()
}
