package com.example.hikingapp.ui.adapters

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.utils.PhotoItemDecorator

class RouteListAdapter(
    var categories: List<String>,
    var routes: List<Route>,
    val context: Context,
    private val itemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<RouteListAdapter.ViewHolder>() {

    private var indexesList = mutableListOf<Long>()

    class ViewHolder(view: View, val itemClickedListener: OnItemClickedListener) : RecyclerView.ViewHolder(view), View.OnClickListener {

         var category: TextView
         var recyclerView: RecyclerView

        init {
            category = view.findViewById(R.id.category_title)
            recyclerView = view.findViewById(R.id.parent_recyclerview)
        }

        override fun onClick(v: View?) {
            itemClickedListener.onItemClicked(adapterPosition, Bundle())
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.route_list, parent, false)
        return ViewHolder(view, itemClickedListener)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val selectedCategory = categories[position]

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val routesCategorizedList = mutableListOf<Route>()

        when (selectedCategory) {
            "Top Rated" -> {
                indexesList = mutableListOf(0L,1L)
                routesCategorizedList.add(routes[0])
                routesCategorizedList.add(routes[1])
            }
            "Popular" -> {
                indexesList = mutableListOf(2L,3L)
                routesCategorizedList.add(routes[2])
                routesCategorizedList.add(routes[3])
            }
            "Easy" -> {
                indexesList = mutableListOf(4L)
                routesCategorizedList.add(routes[4])
            }
        }

        val routeAdapter = RouteAdapter(context,indexesList,routesCategorizedList,itemClickedListener)

        holder.category.text = selectedCategory
        holder.recyclerView.layoutManager = layoutManager
        holder.recyclerView.adapter = routeAdapter

        val itemDecoration = PhotoItemDecorator(5)
        holder.recyclerView.addItemDecoration(itemDecoration)
    }

    override fun getItemCount() = categories.count()

}