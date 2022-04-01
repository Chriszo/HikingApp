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
import com.example.hikingapp.domain.users.reviews.Review

class ReviewsAdapter(
    private val context: Context,
    private val reviews: MutableList<Review>,
    private val itemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    class ViewHolder(
        private val view: View,
        private val itemClickedListener: OnItemClickedListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        val userImage: ImageView = view.findViewById(R.id.user_image)
        val userName: TextView = view.findViewById(R.id.user_name)
        val reviewDescription: TextView = view.findViewById(R.id.review_description)
        val reviewRating: RatingBar = view.findViewById(R.id.review_rating)

        override fun onClick(p0: View?) {
            itemClickedListener.onItemClicked(adapterPosition, Bundle())
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ViewHolder(view, itemClickedListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //TODO define userImage
        holder.userImage.setImageDrawable(
            BitmapDrawable(
                context.resources,
                reviews[position].userImage
            )
        )
        holder.userName.text = reviews[position].userName
        holder.reviewDescription.text = reviews[position].review
        holder.reviewRating.rating = (reviews[position].rating!!).toFloat()
    }

    override fun getItemCount() = reviews.count()

}
