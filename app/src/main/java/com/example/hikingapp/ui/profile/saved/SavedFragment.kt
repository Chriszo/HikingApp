package com.example.hikingapp.ui.profile.saved

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.example.hikingapp.ui.profile.ProfileViewModel
import com.example.hikingapp.ui.route.cultureInfo.SightDetailsActivity
import java.util.stream.Collectors

class SavedFragment : Fragment(), OnItemClickedListener, OnItemLongClickedListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var routes: MutableList<Route>
    private lateinit var sights: MutableList<Sight>


    private lateinit var routesLayoutManager: LinearLayoutManager
    private lateinit var sightsLayoutManager: LinearLayoutManager


    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var sightsRecyclerView: RecyclerView
    private lateinit var routesAdapter: RoutesProfileAdapter
    private lateinit var sightsAdapter: SightsProfileAdapter

    private var allRouteItemsSelected = false
    private var allSightItemsSelected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        val selectAllRoutesTextView = view.findViewById(R.id.select_all_route_items) as TextView
        val selectAllSightsTextView = view.findViewById(R.id.select_all_sight_items) as TextView


        profileViewModel.isRoutesLongClickPressed.observe(viewLifecycleOwner, {

            if (it == true) {
                selectAllRoutesTextView.visibility = View.VISIBLE
                selectAllRoutesTextView.text = getString(R.string.select_all)
            } else {
                selectAllRoutesTextView.visibility = View.GONE
            }
        })

        profileViewModel.isSightsLongClickPressed.observe(viewLifecycleOwner, {

            if (it == true) {
                selectAllSightsTextView.visibility = View.VISIBLE
                selectAllSightsTextView.text = getString(R.string.select_all)
            } else {
                selectAllSightsTextView.visibility = View.GONE
            }
        })


        selectAllRoutesTextView.setOnClickListener {
            if (!allRouteItemsSelected) {
                routesAdapter.selectAllItems()
                selectAllRoutesTextView.text = getString(R.string.un_select_all)
                allRouteItemsSelected = true
            } else {
                routesAdapter.clearAllItems()
                selectAllRoutesTextView.text = getString(R.string.select_all)
                selectAllRoutesTextView.visibility = View.GONE
                allRouteItemsSelected = false
            }

        }

        selectAllSightsTextView.setOnClickListener {
            if (!allSightItemsSelected) {
                sightsAdapter.selectAllItems()
                selectAllSightsTextView.text = getString(R.string.un_select_all)
                allSightItemsSelected = true
            } else {
                sightsAdapter.clearAllItems()
                selectAllSightsTextView.text = getString(R.string.select_all)
                selectAllSightsTextView.visibility = View.GONE
                allSightItemsSelected = false
            }

        }

        initializeSavedRoutes(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializeSavedSights(view)
        }
        return view
    }


    override fun onResume() {
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeSavedSights(view: View) {
        sightsRecyclerView = view.findViewById(R.id.favoritesSightsRView)
        sightsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sightsRecyclerView.layoutManager = sightsLayoutManager

        // TODO initialize sights list with sights

        profileViewModel.savedSights.observe(viewLifecycleOwner, { sightsList ->

            sights = sightsList
            profileViewModel.user.value?.profileInfo?.savedSights = sights.stream().map { it.sightId }.collect(Collectors.toList())
            sightsAdapter = SightsProfileAdapter(sights, this, this)
            sightsRecyclerView.adapter = sightsAdapter
            profileViewModel.savedSightsEnabled.observe(viewLifecycleOwner, {
                sightsAdapter.setEnabled(it)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeSavedRoutes(view: View) {
        routesRecyclerView = view.findViewById(R.id.favoritesRoutesRView)
        routesLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        routesRecyclerView.layoutManager = routesLayoutManager

        profileViewModel.savedRoutes.observe(viewLifecycleOwner, {
            routes = it as MutableList<Route>

            profileViewModel.user.value?.profileInfo?.savedRoutes = routes.stream().map { it.routeId }.collect(Collectors.toList())
            routesAdapter = RoutesProfileAdapter(routes, this, this)
            routesRecyclerView.adapter = routesAdapter

            profileViewModel.savedRoutesEnabled.observe(viewLifecycleOwner, {
                routesAdapter.setEnabled(it)
            })
        })
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        if (bundle.containsKey("class")) {
            when (bundle.get("class")) {
                Route::class.java.simpleName -> {
                    val intent = Intent(context, RouteActivity::class.java)
                    // TODO replace with Global Utils ID
                    intent.putExtra("route", routes[position])
                    intent.putExtra("action", "saved")
                    startActivity(intent)
                }
                Sight::class.java.simpleName -> {
                    val intent = Intent(context, SightDetailsActivity::class.java)
                    // TODO replace with Global Utils ID
                    intent.putExtra("sightInfo", sights[position])
                    intent.putExtra("action", "saved")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onItemLongClicked(position: Int, bundle: Bundle) {
        if (bundle.containsKey("routeLongClick")) {
            if (bundle.getBoolean("routeLongClick")) {
                profileViewModel.isRoutesLongClickPressed.postValue(true)
            } else {
                profileViewModel.isRoutesLongClickPressed.postValue(false)
            }
        }

        if (bundle.containsKey("sightLongClick")) {

            if (bundle.getBoolean("sightLongClick")) {
                profileViewModel.isSightsLongClickPressed.postValue(true)
            } else {
                profileViewModel.isSightsLongClickPressed.postValue(false)
            }
        }

        if (bundle.containsKey("sightSelectedItems")) {
            val selectedItemsWrapper =
                bundle.getSerializable("sightSelectedItems") as SelectedItemsWrapper
            profileViewModel.selectedSightItems.postValue(selectedItemsWrapper.selectedItems)
        }

        if (bundle.containsKey("routeSelectedItems")) {
            val selectedItemsWrapper =
                bundle.getSerializable("routeSelectedItems") as SelectedItemsWrapper
            profileViewModel.selectedRouteItems.postValue(selectedItemsWrapper.selectedItems)
        }
    }

}