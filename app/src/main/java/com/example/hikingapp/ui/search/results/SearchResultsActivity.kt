package com.example.hikingapp.ui.search.results

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.MainActivity
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.databinding.ActivitySearchResultsBinding
import com.example.hikingapp.domain.enums.ActionType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemCheckedListener
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.example.hikingapp.ui.discover.DiscoverFragment
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.AppViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.stream.Collectors

class SearchResultsActivity : AppCompatActivity(), OnItemClickedListener, OnItemCheckedListener {

    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var routes: MutableList<Route>
    private lateinit var itemCheckedListener: OnItemCheckedListener
    private lateinit var itemClickedListener: OnItemClickedListener

    private var routesForNavigation = mutableListOf<Route>()

    private lateinit var appViewModel: AppViewModel

    private val storageRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("mainPhotoPrefs", 0)
    }

    private val checkedRoutePrefs: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("checkedRoutePrefs", 0)
    }

    private var authInfo: FirebaseUser? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemCheckedListener = this
        itemClickedListener = this
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressBar = binding.progressBar
        val confirmButton = binding.confirmButton

        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        routes = intent.extras?.get("routes") as MutableList<Route>
        authInfo = intent.extras?.get("authInfo") as FirebaseUser?

        layoutManager = LinearLayoutManager(this)
        recyclerView = binding.searchResultsRecyclerview
        recyclerView.layoutManager = layoutManager

        val noResultsView = binding.noResultsText

        if (!routes.isNullOrEmpty()) {
            noResultsView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            confirmButton.visibility = View.VISIBLE
        }

        initializeRoutesForNavigation()

        routes.forEach { route ->
            storageRef.child(
                "routes/mainPhotos/${
                    sharedPreferences.getString(
                        route.routeId.toString(),
                        null
                    )
                }"
            ).getBytes(1024 * 1024).addOnSuccessListener {
                route.mainPhotoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                val temp =
                    if (appViewModel.mainPhotos.value == null) mutableListOf() else appViewModel.mainPhotos.value
                temp?.add(route.mainPhotoBitmap!!)
                appViewModel.mainPhotos.postValue(temp)
            }
        }

        appViewModel.mainPhotos.observe(this, { photoBitmaps ->
            if (photoBitmaps.size == routes.size) {
                routesAdapter = RouteAdapter(
                    this,
                    null,
                    routes,
                    this,
                    this,
                    true,
                    userLoggedIn = authInfo != null,
                    navigableRoutes = checkedRoutePrefs.getStringSet(
                        GlobalUtils.routeIdsForNavigation,
                        mutableSetOf()
                    )!!,
                    ActionType.SEARCH
                )
                recyclerView.adapter = routesAdapter

                progressBar.visibility = View.GONE
                confirmButton.visibility = View.VISIBLE
            }
        })

        confirmButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            storeSelectedRoutesForNavigation()
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroy() {
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeRoutesForNavigation() {

        val routeIds =
            checkedRoutePrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())
        routesForNavigation = routes.stream().filter { routeIds!!.contains(it.routeId.toString()) }
            .collect(Collectors.toList())
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(this, RouteActivity::class.java)

        routes[position].mainPhotoBitmap = null

        intent.putExtra("route", routes[position])
        intent.putExtra("action", "discover")
        intent.putExtra("authInfo", authInfo)

        storeSelectedRoutesForNavigation()

        startActivity(intent)
    }

    private fun storeSelectedRoutesForNavigation() {
        val routeIdsSet = mutableSetOf<String>()
        routesForNavigation.forEach {
            routeIdsSet.add(it.routeId.toString())
        }

        val existingRouteIdsSet =
            checkedRoutePrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())
        routeIdsSet.addAll(existingRouteIdsSet!!)
        checkedRoutePrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, routeIdsSet)
            .apply()

        if (authInfo != null) {
            FirebaseDatabase.getInstance()
                .getReference("selected_routes_nav")
                .child(authInfo!!.uid)
                .setValue(routeIdsSet.toList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemChecked(position: Int) {
        if (routesForNavigation.isNullOrEmpty()) {
            routesForNavigation = mutableListOf()
        }
        routesForNavigation.add(routes[position])

        addSelectedRoutesForNavigation(routesForNavigation)

        logRouteIds(routesForNavigation)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addSelectedRoutesForNavigation(routesForNavigation: MutableList<Route>) {
        val currentRouteIds = routesForNavigation.stream().map { it.routeId.toString() }.collect(Collectors.toSet())

        val existingRouteIds =
            checkedRoutePrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())
        existingRouteIds!!.addAll(currentRouteIds)
        checkedRoutePrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, existingRouteIds)
            .apply()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemUnchecked(position: Int) {
        var indexToRemove = 0
        var routeId = ""
        if (!routesForNavigation.isNullOrEmpty()) {

            routesForNavigation.forEach { route ->
                if (routes.indexOf(route) == position) {
                    indexToRemove = routesForNavigation.indexOf(route)
                    routeId = route.routeId.toString()
                }
            }
            routesForNavigation.removeAt(indexToRemove)

            removeRouteFromSelected(routeId)

            logRouteIds(routesForNavigation)
        }
    }

    private fun removeRouteFromSelected(routeId: String) {
        val existingIds =
            checkedRoutePrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())

        if (!existingIds.isNullOrEmpty()) {
            existingIds.remove(routeId)
            checkedRoutePrefs.edit()
                .putStringSet(GlobalUtils.routeIdsForNavigation, existingIds)
                .apply()
        }
    }


    // TEST - DEBUG
    private fun logRouteIds(routeList: MutableList<Route>) {
        Log.i(
            DiscoverFragment::class.java.simpleName,
            "#### LOGGING ROUTE IDS SELECTED FOR NAVIGATION... ####"
        )
        routeList.forEach {
            Log.i(DiscoverFragment::class.java.simpleName, "logRouteId stored: ${it.routeId}")
        }
    }
}