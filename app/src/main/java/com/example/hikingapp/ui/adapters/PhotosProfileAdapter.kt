package com.example.hikingapp.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.users.PhotoItem

class PhotosProfileAdapter(
    private val context: Context?,
    private var photos: List<PhotoItem?>,
    private val itemClickedListener: OnItemClickedListener,
    private val itemLongClickedListener: OnItemLongClickedListener
) :
    RecyclerView.Adapter<PhotosProfileAdapter.ViewHolder>() {

    companion object {
        var inSelectMode = false
        var selectedItems = mutableListOf<Int>()
        private const val longClick = "photoLongClick"
        private const val items = "photoSelectedItems"
        var photosViewEnabled = true
    }

    init {
        inSelectMode = false
        selectedItems = mutableListOf<Int>()
        photosViewEnabled = true
    }

    class ViewHolder(
        val view: View,
        private val itemClickedListener: OnItemClickedListener,
        private val itemLongClickedListener: OnItemLongClickedListener,
        private var bundle: Bundle
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        val imageView: ImageView
        val imageName: TextView

        /*init {
            bundle = Bundle()
            view.setOnClickListener(this)
            imageView = view.findViewById(R.id.photo_item_id)
        }*/


        init {
            view.setOnLongClickListener {
                if (!inSelectMode && photosViewEnabled) {
                    it.setBackgroundColor(Color.rgb(0, 82, 204))
                    selectedItems.add(adapterPosition)
                    inSelectMode = true

                    val bundle = Bundle()
                    bundle.putBoolean(longClick, true)
                    bundle.putSerializable(items, SelectedItemsWrapper(selectedItems))
                    this.itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                }
                true
            }

            view.setOnClickListener {

                if (photosViewEnabled) {

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
                                    items,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            } else {
                                bundle.putBoolean(longClick, true)
                                bundle.putSerializable(
                                    items,
                                    SelectedItemsWrapper(selectedItems)
                                )
                                itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                            }

                        } else {
                            inSelectMode = false
                            itemLongClickedListener.onItemLongClicked(adapterPosition, bundle)
                        }
                    } else {
                        bundle.putSerializable("class", PhotoItem::class.java.simpleName)
                        itemClickedListener.onItemClicked(adapterPosition, bundle)
                    }
                }
            }

            imageView = view.findViewById(R.id.photo_imageView)
            imageName = view.findViewById(R.id.photo_name)
        }

        override fun onClick(v: View?) {
            itemClickedListener.onItemClicked(adapterPosition, bundle)
        }

        override fun onLongClick(v: View?): Boolean {
            TODO("Not yet implemented")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_photo_item, parent, false)
        return ViewHolder(view, itemClickedListener, itemLongClickedListener, Bundle())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (selectedItems.contains(position)) {
            holder.view.setBackgroundColor(Color.rgb(0, 82, 204))
        } else {
            if (selectedItems.isNullOrEmpty()) {
                val bundle = Bundle()
                bundle.putBoolean(longClick, false)
                bundle.putSerializable(
                    items, SelectedItemsWrapper(
                        selectedItems
                    )
                )
                this.itemLongClickedListener.onItemLongClicked(position, bundle)
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT)
        }
        holder.imageView.setImageDrawable(
            BitmapDrawable(
                context!!.resources,
                photos[position]!!.imageBitmap
            )
        )

        holder.imageName.text = photos[position]!!.imageName
    }

    override fun getItemCount() = photos.count()


    fun selectAllItems() {
        photos.withIndex().forEach {
            selectedItems.add(it.index)
            notifyItemChanged(it.index)
        }
    }

    fun setEnabled(isEnabled: Boolean) {
        photosViewEnabled = isEnabled
    }

    fun clearAllItems() {
        selectedItems.clear()
        inSelectMode = false
        notifyDataSetChanged()
    }
}