package com.example.hikingapp.ui.adapters

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.enums.ActionType
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.ui.navigation.NavigationFragment
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

class RouteAdapter(
    val context: Context,
    private var indexesList: MutableList<Long>?,
    val routes: List<Route>,
    private val itemClickedListener: OnItemClickedListener,
    val itemCheckedListener: OnItemCheckedListener? = null,
    private val onSearch: Boolean = false,
    private val userLoggedIn: Boolean,
    private val navigableRoutes: MutableSet<String> = mutableSetOf(),
    private val actionType: ActionType = ActionType.NORMAL,
    private val currentLocation: Point? = null
) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {

    class ViewHolder(
        val context: Context,
        val view: View,
        var indexesList: MutableList<Long>?,
        private val itemClickedListener: OnItemClickedListener,
        private val itemCheckedListener: OnItemCheckedListener?,
        private val onSearch: Boolean,
        private val userLoggedIn: Boolean,
        private val navigableRoutes: MutableSet<String>,
        private val actionType: ActionType,
        private val currentLocation: Point?
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        var imageView: ImageView
        var nameView: TextView
        var stateView: TextView
        var ratingView: RatingBar
        var difficultyLevelView: TextView? = null
        var distanceFrom: TextView? = null
        var selectedForNavigationView: ImageView? = null
//        var deleteNavigableRouteView: ImageView? = null
        var routeIsChecked = false

        init {
            view.setOnClickListener(this)
            imageView = view.findViewById(R.id.route_imageView)
            nameView = view.findViewById(R.id.route_name)
            stateView = view.findViewById(R.id.route_state)
            ratingView = view.findViewById(R.id.routeRating)
            if (itemClickedListener is NavigationFragment) {
                distanceFrom = view.findViewById(R.id.route_distance_from)
            } else {

                difficultyLevelView = view.findViewById(R.id.difficulty_level)
                selectedForNavigationView = view.findViewById(R.id.navigation_selected) as ImageView
//                deleteNavigableRouteView = view.findViewById(R.id.delete_route) as ImageView

                when (actionType) {
                    ActionType.DISCOVER -> {
                        selectedForNavigationView!!.visibility = View.VISIBLE
//                        deleteNavigableRouteView!!.visibility = View.GONE
                    }
                    ActionType.NAVIGATION -> {
                        selectedForNavigationView!!.visibility = View.GONE
//                        deleteNavigableRouteView!!.visibility = View.VISIBLE
                    }
                    ActionType.NORMAL -> {

                        selectedForNavigationView!!.visibility = View.GONE
//                        deleteNavigableRouteView!!.visibility = View.GONE
                    }
                    ActionType.SEARCH -> {
                        if (userLoggedIn) {

                            selectedForNavigationView!!.visibility = View.VISIBLE
//                            deleteNavigableRouteView!!.visibility = View.GONE
                        } else {
                            selectedForNavigationView!!.visibility = View.GONE
//                            deleteNavigableRouteView!!.visibility = View.GONE
                        }
                    }
                }
                if (itemCheckedListener == null || !userLoggedIn) {
                    selectedForNavigationView!!.visibility = View.GONE
//                    deleteNavigableRouteView!!.visibility = View.GONE
                } else if (itemCheckedListener != null && !onSearch) {
                    selectedForNavigationView!!.visibility = View.GONE
//                    deleteNavigableRouteView!!.visibility = View.VISIBLE
                }

                selectedForNavigationView?.let {
                    it.setOnClickListener {
                        if (routeIsChecked) {
                            selectedForNavigationView!!.setImageResource(R.drawable.not_selected_icon_foreground)
                            routeIsChecked = false

                            if (onSearch) {
                                itemCheckedListener!!.onItemUnchecked(adapterPosition)
                            } else {
                                itemCheckedListener!!.onItemUnchecked(indexesList!![adapterPosition].toInt())
                            }

                        } else {
                            selectedForNavigationView!!.setImageResource(R.drawable.selected_icon_foreground)
                            routeIsChecked = true
                            if (onSearch) {
                                itemCheckedListener!!.onItemChecked(adapterPosition)
                            } else {
                                itemCheckedListener!!.onItemChecked(indexesList!![adapterPosition].toInt())
                            }
                        }
                    }
                }

//                deleteNavigableRouteView?.let {
//                    it.setOnClickListener {
//                        this.itemCheckedListener!!.onItemChecked(adapterPosition)
//                    }
//                }
            }
        }

        override fun onClick(v: View?) {
            val bundle = Bundle()
            bundle.putSerializable("class", Route::class.java.simpleName)
            if (indexesList.isNullOrEmpty()) {
                itemClickedListener.onItemClicked(adapterPosition, bundle)
            } else {
                itemClickedListener.onItemClicked(indexesList!![adapterPosition].toInt(), bundle)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View? = null
        if (isNavigationFragment()) {
            view =
                LayoutInflater.from(parent.context).inflate(R.layout.route_item_min, parent, false)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false)
        }

        return ViewHolder(
            context,
            view,
            indexesList,
            itemClickedListener,
            itemCheckedListener,
            onSearch,
            userLoggedIn = userLoggedIn,
            navigableRoutes,
            actionType,
            currentLocation
        )
    }

    private fun isNavigationFragment(): Boolean {
        return itemClickedListener is NavigationFragment
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        holder.imageView.setImageResource(routes[position].mainPhoto!!)
        holder.imageView.setImageDrawable(
            BitmapDrawable(
                context.resources,
                LocalDatabase.getMainImage(routes[position].routeId, Route::class.java.simpleName)
            )
        )
        holder.nameView.text = routes[position].routeName
        holder.stateView.text =
            context.getString(R.string.secondary_text, routes[position].stateName)
        holder.ratingView.rating = routes[position].routeInfo!!.rating!!

        if (!isNavigationFragment()) {


            val difficultyLevelText = routes[position].routeInfo!!.difficultyLevel!!.difficultyLevel


            holder.difficultyLevelView!!.text = difficultyLevelText
            when (routes[position].routeInfo!!.difficultyLevel!!) {
                DifficultyLevel.EASY -> holder.difficultyLevelView!!.background =
                    ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_green)
                DifficultyLevel.MODERATE -> holder.difficultyLevelView!!.background =
                    ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_yellow)
                DifficultyLevel.HARD -> holder.difficultyLevelView!!.background =
                    ContextCompat.getDrawable(holder.view.context, R.drawable.rounded_red)
            }


            holder.routeIsChecked = navigableRoutes.contains(routes[position].routeId.toString())

            if (holder.routeIsChecked) {
                holder.selectedForNavigationView!!.setImageResource(R.drawable.selected_icon_foreground)
            } else {
                holder.selectedForNavigationView!!.setImageResource(R.drawable.not_selected_icon_foreground)
            }
        } else {
//            FirebaseDatabase.getInstance().getReference("mapData")
//                    .child("route_${routes[position].routeId}").child("origin").addValueEventListener(object :
//                    ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            routes[position].mapInfo = MapInfo()
//                            val origin = snapshot.value as HashMap<String, Double>
//                            routes[position].mapInfo?.apply {
//                                this.origin = Point.fromLngLat(
//                                    origin["longitude"] as Double,
//                                    origin["latitude"] as Double
//                                )
                                holder.distanceFrom!!.text = "( ${GlobalUtils.getTwoDigitsDistance(TurfMeasurement.distance(currentLocation!!, routes[position].mapInfo!!.origin),DistanceUnitType.KILOMETERS, false)} )"
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
        }
    }

    override fun getItemCount() = routes.count()
}