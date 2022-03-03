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
import com.example.hikingapp.ui.adapters.RouteListAdapter

class FavoritesFragment : Fragment(), OnItemClickedListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var favoriteRoutes: MutableList<Route>

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var favoriteRoutesRecyclerView: RecyclerView
    private lateinit var favoriteSightsRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: RouteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        initializeFavoriteRoutes(view!!)

        initializeFavoriteSights(view!!)

        return view
    }

    private fun initializeFavoriteSights(view: View) {

    }

    private fun initializeFavoriteRoutes(view: View) {
        favoriteRoutesRecyclerView = view.findViewById(R.id.favoritesRoutesRView)
        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        favoriteRoutesRecyclerView.layoutManager = layoutManager

        profileViewModel.favoriteRoutes.observe(viewLifecycleOwner, {
            favoriteRoutes = it as MutableList<Route>
            favoritesAdapter = RouteAdapter(favoriteRoutes,FavoritesFragment@this)
            favoriteRoutesRecyclerView.adapter = favoritesAdapter
        })
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context,RouteActivity::class.java)
        // TODO replace with Global Utils ID
        intent.putExtra("route", favoriteRoutes[position])
        startActivity(intent)
    }

}