package com.example.hikingapp.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R

class PhotoAdapter(
    private val context: Context?,
    private var photos: List<Bitmap>,
    private val itemClickedListener: OnItemClickedListener
) :
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
            imageView = view.findViewById(R.id.photo_item_id)
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
        holder.imageView.setImageDrawable(BitmapDrawable(context!!.resources, photos[position]))
    }

    override fun getItemCount() = photos.count()
}