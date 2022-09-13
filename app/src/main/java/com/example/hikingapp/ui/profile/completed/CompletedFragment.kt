package com.example.hikingapp.ui.profile.completed

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.RouteActivity
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.*
import com.example.hikingapp.viewModels.ProfileViewModel
import com.example.hikingapp.ui.profile.saved.CompletedViewModel
import com.example.hikingapp.ui.route.cultureInfo.SightDetailsActivity
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_completed.view.*
import kotlinx.android.synthetic.main.fragment_saved.view.*
import java.util.stream.Collectors

class CompletedFragment : Fragment(), OnItemClickedListener, OnItemLongClickedListener {

    private var authInfo: FirebaseUser? = null
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val completedViewModel: CompletedViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var routes: MutableList<Route>
    private lateinit var sights: MutableList<Sight>


    private lateinit var routesLayoutManager: LinearLayoutManager
    private lateinit var sightsLayoutManager: LinearLayoutManager


    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var sightsRecyclerView: RecyclerView
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var sightsAdapter: SightsProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_completed, container, false)
        
        userViewModel.user.observe(viewLifecycleOwner, {
            authInfo = it
        })

        initializeCompletedRoutes(view)
//        initializeCompletedSights(view)

        return view
    }

   /* @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeCompletedSights(view: View) {
        sightsRecyclerView = view.findViewById(R.id.completedSightsRView)
        sightsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sightsRecyclerView.layoutManager = sightsLayoutManager

        // TODO initialize sights list with sights
        profileViewModel.completedSights.observe(viewLifecycleOwner, {

            if (it.isNullOrEmpty()) {
                sightsRecyclerView.visibility = View.GONE
                view.no_completed_sights.visibility = View.VISIBLE
            } else {
                sightsRecyclerView.visibility = View.VISIBLE
                view.no_completed_sights.visibility = View.GONE
                sights = it as MutableList<Sight>
                profileViewModel.user.value?.profileInfo?.completedSights =
                    sights.stream().map { it.sightId }.collect(Collectors.toList())
                sightsAdapter = SightsProfileAdapter(context, sights, this,this)
                sightsRecyclerView.adapter = sightsAdapter
            }
        })
    }*/

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeCompletedRoutes(view: View) {
        routesRecyclerView = view.findViewById(R.id.completedRoutesRView)
        routesLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        routesRecyclerView.layoutManager = routesLayoutManager

        profileViewModel.completedRoutes.observe(viewLifecycleOwner, {

            if (it.isNullOrEmpty()) {
                routesRecyclerView.visibility = View.GONE
                view.no_completed_routes.visibility = View.VISIBLE
            } else {
                routesRecyclerView.visibility = View.VISIBLE
                view.no_completed_routes.visibility = View.GONE
                routes = it
                profileViewModel.user.value?.profileInfo?.completedRoutes =
                    routes.stream().map { it.routeId }.collect(Collectors.toList())
                routesAdapter = RouteAdapter(
                    requireContext(),
                    null,
                    routes,
                    this,
                    userLoggedIn = authInfo != null
                )
                routesRecyclerView.adapter = routesAdapter
            }
        })
    }


    override fun onItemClicked(position: Int, bundle: Bundle) {
        if (bundle.containsKey("class")) {

            when (bundle.get("class")) {

                Route::class.java.simpleName -> {
                    val intent = Intent(context, RouteActivity::class.java)
                    // TODO replace with Global Utils ID
                    routes[position].mainPhotoBitmap = null
                    intent.putExtra("route", routes[position])
                    intent.putExtra("action", "completed")
                    intent.putExtra("authInfo", authInfo)
                    intent.putExtra("userSettings", userViewModel.userSettings.value)
                    startActivity(intent)
                }

                Sight::class.java.simpleName -> {
                    val intent = Intent(context, SightDetailsActivity::class.java)
                    // TODO replace with Global Utils ID
                    intent.putExtra("sightInfo", sights[position])
                    intent.putExtra("action", "completed")
                    intent.putExtra("authInfo", authInfo)
                    startActivity(intent)
                }

            }

        }

    }

    override fun onItemLongClicked(position: Int, bundle: Bundle) {

    }

}