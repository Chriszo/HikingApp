package com.example.hikingapp.ui.search.results

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.databinding.ActivitySearchResultsBinding
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.example.hikingapp.ui.adapters.RouteListAdapter

class SearchResultsActivity : AppCompatActivity(), OnItemClickedListener {

    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var recyclerView:RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var routes: MutableList<Route>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras?.get("routesBundle") as Bundle
        routes = bundle.get("routes") as MutableList<Route>

        layoutManager = LinearLayoutManager(this)
        recyclerView = binding.searchResultsRecyclerview
        recyclerView.layoutManager = layoutManager
        routesAdapter = RouteAdapter(routes,this)
        recyclerView.adapter = routesAdapter

    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(this, RouteActivity::class.java)
        intent.putExtra("route", routes[position])
        startActivity(intent)
    }
}