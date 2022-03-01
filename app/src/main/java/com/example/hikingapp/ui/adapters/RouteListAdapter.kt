package com.example.hikingapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route

class RouteListAdapter(
    var categories: List<String>,
    var routes: List<Route>,
    val context: Context,
    val itemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<RouteListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

         var category: TextView
         var recyclerView: RecyclerView

        init {
            category = view.findViewById(R.id.category_title)
            recyclerView = view.findViewById(R.id.parent_recyclerview)
        }

        override fun onClick(v: View?) {
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.route_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val selectedCategory = categories[position]

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val routesCategorizedList = mutableListOf<Route>()

        when (selectedCategory) {
            "Top Rated" -> {
                routesCategorizedList.add(routes[0])
                routesCategorizedList.add(routes[1])
            }
            "Popular" -> {
                routesCategorizedList.add(routes[2])
                routesCategorizedList.add(routes[3])
            }
            "Easy" -> {
                routesCategorizedList.add(routes[4])
            }
        }

        val routeAdapter = RouteAdapter(routesCategorizedList,itemClickedListener)

        holder.category.text = selectedCategory
        holder.recyclerView.layoutManager = layoutManager
        holder.recyclerView.adapter = routeAdapter
    }

    override fun getItemCount() = categories.count()
}