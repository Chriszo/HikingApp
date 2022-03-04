package com.example.hikingapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter

class FavoritesFragment : Fragment(), OnItemClickedListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var routes: MutableList<Route>
    private lateinit var sights: MutableList<Route>


    private lateinit var routesLayoutManager: LinearLayoutManager
    private lateinit var sightsLayoutManager: LinearLayoutManager


    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var sightsRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: RouteAdapter
    private lateinit var sightsAdapter: RouteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        initializeFavoriteRoutes(view)

        initializeFavoriteSights(view)

        return view
    }

    private fun initializeFavoriteSights(view: View) {
        sightsRecyclerView = view.findViewById(R.id.favoritesSightsRView)
        sightsLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        sightsRecyclerView.layoutManager = sightsLayoutManager

        // TODO initialize sights list with sights
        profileViewModel.favoriteRoutes.observe(viewLifecycleOwner, {
            sights = it as MutableList<Route>
            sightsAdapter = RouteAdapter(sights, this)
            sightsRecyclerView.adapter = sightsAdapter
        })
    }

    private fun initializeFavoriteRoutes(view: View) {
        routesRecyclerView = view.findViewById(R.id.favoritesRoutesRView)
        routesLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        routesRecyclerView.layoutManager = routesLayoutManager

        profileViewModel.favoriteRoutes.observe(viewLifecycleOwner, {
            routes = it as MutableList<Route>
            favoritesAdapter = RouteAdapter(routes, this)
            routesRecyclerView.adapter = favoritesAdapter
        })
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context,RouteActivity::class.java)
        // TODO replace with Global Utils ID
        intent.putExtra("route", routes[position])
        startActivity(intent)
    }

}