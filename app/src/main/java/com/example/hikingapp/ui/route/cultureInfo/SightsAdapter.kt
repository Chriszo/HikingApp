package com.example.hikingapp.ui.route.cultureInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.culture.Sight

class SightsAdapter(var sights: List<Sight>, var itemClicked: OnItemClickedListener?): RecyclerView.Adapter<SightsAdapter.ViewHolder>() {

    constructor(): this(emptyList(), null)

    class ViewHolder(view: View, itemClicked: OnItemClickedListener): RecyclerView.ViewHolder(view), View.OnClickListener {

        private val mView = view
        private val itemClickListener = itemClicked
        val sightNameView: TextView = view.findViewById(R.id.sight_name)
        val sightDescriptionView: TextView = view.findViewById(R.id.sight_description)
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

        holder.sightNameView.text = sights[position].name
        holder.sightDescriptionView.text = sights[position].description
    }

    override fun getItemCount() = sights.count()

}
