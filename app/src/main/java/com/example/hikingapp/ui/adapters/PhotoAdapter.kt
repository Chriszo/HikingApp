package com.example.hikingapp.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R

class PhotoAdapter(var photos: List<Int>, private val itemClickedListener: OnItemClickedListener) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    class ViewHolder(
        private val view: View,
        private val itemClickedListener: OnItemClickedListener,
        private var bundle: Bundle
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val imageView: ImageView

        init {
            bundle = Bundle()
            view.setOnClickListener(this)
            imageView = view.findViewById(R.id.photo_id)
        }

        override fun onClick(v: View?) {
            itemClickedListener.onItemClicked(adapterPosition, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return ViewHolder(view, itemClickedListener, Bundle())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(photos[position])
    }

    override fun getItemCount() = photos.count()
}