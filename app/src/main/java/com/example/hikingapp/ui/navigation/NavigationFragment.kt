package com.example.hikingapp.ui.navigation

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.*
import com.example.hikingapp.databinding.FragmentNavigationBinding
import com.example.hikingapp.domain.enums.ActionType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemCheckedListener
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.AppViewModel
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.stream.Collectors

class NavigationFragment : Fragment(), LocalDBExecutor, OnItemClickedListener, OnItemCheckedListener {

    private lateinit var binding: FragmentNavigationBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var routes: MutableList<Route>

    private var routesForNavigation = mutableListOf<Route>()

    private lateinit var appViewModel: AppViewModel

    private val storageRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().applicationContext.getSharedPreferences("mainPhotoPrefs", 0)
    }

    private val checkedRoutesPrefs: SharedPreferences by lazy {
        requireActivity().applicationContext.getSharedPreferences("checkedRoutePrefs", 0)
    }

    private val routeViewModel: RouteViewModel by activityViewModels()
    private val userViewModel:UserViewModel by activityViewModels()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = FragmentNavigationBinding.inflate(inflater, container, false)

        if (userViewModel.user.value == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        } else {


            val progressBar = binding.progressBar


            checkedRoutesPrefs.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                resolveRoutesForNavigation()
            }

            resolveRoutesForNavigation()

            appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

            layoutManager = LinearLayoutManager(context)
            recyclerView = binding.searchResultsRecyclerview
            recyclerView.layoutManager = layoutManager

            val noResultsView = binding.noResultsText

            if (!routes.isNullOrEmpty()) {
                noResultsView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
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

            appViewModel.mainPhotos.observe(viewLifecycleOwner, { photoBitmaps ->
                if (photoBitmaps.size == routes.size) {
                    routesAdapter = RouteAdapter(
                        requireContext(),
                        null,
                        routes,
                        itemClickedListener = this,
                        itemCheckedListener = this,
                        userLoggedIn = userViewModel.user.value != null,
                        actionType = ActionType.NAVIGATION
                    )
                    recyclerView.adapter = routesAdapter

                    progressBar.visibility = View.GONE
                }
            })

            routeViewModel.routesSelectedForNavigation.observe(viewLifecycleOwner,{
                resolveRoutesForNavigation()
            })
        }


        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroyView() {
        super.onDestroyView()

        checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())!!
            .toList().apply {
                FirebaseDatabase.getInstance()
                    .getReference("selected_routes_nav")
                    .child(userViewModel.user.value!!.uid)
                    .setValue(this)
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resolveRoutesForNavigation() {
        checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation, null)?.let { routeIds ->

            routeViewModel.routesSelectedForNavigation.value?.apply {
                routeIds.addAll(
                    this.stream()
                        .map { it.routeId.toString() }
                        .collect(Collectors.toList())
                )
            } ?: mutableListOf()

            routes = routeViewModel.currentRoutes.value!!
                .stream()
                .filter { routeIds.contains(it.routeId.toString()) }
                .collect(Collectors.toList())
        }
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(requireContext(), NavigationActivity::class.java)

        intent.putExtra("authInfo", userViewModel.user.value)
        intent.putExtra("route", routes[position])
        storeSelectedRoutesForNavigation()

        startActivity(intent)
    }

    private fun storeSelectedRoutesForNavigation() {
        val routeIdsSet = mutableSetOf<String>()
        routesForNavigation.forEach {
            routeIdsSet.add(it.routeId.toString())
        }
        routeViewModel.routesSelectedForNavigation.postValue(routesForNavigation)
        checkedRoutesPrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, routeIdsSet).apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemChecked(position: Int) {
        val routeToRemove = routes[position]

        routes.remove(routeToRemove).also {
            routesAdapter = RouteAdapter(
                requireContext(),
                null,
                routes,
                itemClickedListener = this,
                itemCheckedListener = this,
                userLoggedIn = userViewModel.user.value != null,
                actionType = ActionType.NAVIGATION
            )
            recyclerView.adapter = routesAdapter
        }
        routesForNavigation.removeIf {
            it.routeId == routeToRemove.routeId
        }

        routeViewModel.routesSelectedForNavigation.postValue(routesForNavigation)
        val routeIdsSet = checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation,mutableSetOf())
        routeIdsSet!!.removeIf { it == routeToRemove.routeId.toString() }
        checkedRoutesPrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, routeIdsSet).apply()
    }

    override fun onItemUnchecked(position: Int) {
        TODO("Not yet implemented")
    }
}