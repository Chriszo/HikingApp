package com.example.hikingapp.ui.search.results

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.app.viewModels.AppViewModel
import com.example.hikingapp.databinding.ActivitySearchResultsBinding
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SearchResultsActivity : AppCompatActivity(), OnItemClickedListener {

    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var routes: MutableList<Route>

    private lateinit var appViewModel: AppViewModel

    private val storageRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("mainPhotoPrefs", 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)

        val bundle = intent.extras?.get("routesBundle") as Bundle
        routes = bundle.get("routes") as MutableList<Route>

        layoutManager = LinearLayoutManager(this)
        recyclerView = binding.searchResultsRecyclerview
        recyclerView.layoutManager = layoutManager

        val noResultsView = binding.noResultsText

        if (!routes.isNullOrEmpty()) {
            noResultsView.visibility = View.GONE
        }

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
                routesAdapter = RouteAdapter(this, null, routes, this)
                recyclerView.adapter = routesAdapter
            }
        })


    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(this, RouteActivity::class.java)

        routes[position].mainPhotoBitmap = null

        intent.putExtra("route", routes[position])
        intent.putExtra("action", "normal")
        startActivity(intent)
    }
}