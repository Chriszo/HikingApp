package com.example.hikingapp.ui.adapters

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.users.PhotoItem

class ViewPagerAdapter(private val photos: MutableList<PhotoItem?>?): RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val photoIcon: ImageView = itemView.findViewById(R.id.photo_id)
       /* val voteIcon: ImageView = itemView.findViewById(R.id.vote_icon)
        val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_photo_icon)*/

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_photo_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.photoIcon.setImageBitmap(photos?.get(position)?.imageBitmap)
    }

    override fun getItemCount() = photos?.count() ?:0
}