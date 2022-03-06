package com.example.hikingapp.ui.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentProfileBinding
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.mock.db.MockDatabase
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.stream.Collectors

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var _binding: FragmentProfileBinding? = null

    private lateinit var routes: MutableList<Route>
    private lateinit var sights: MutableList<Sight>


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val navHost =
            childFragmentManager.findFragmentById(R.id.profileFragmentContainer) as NavHostFragment
        val navController = navHost.navController

        val navView = root.info_nav_view
        navView.setupWithNavController(navController)

        val deleteFrame = root.findViewById(R.id.delete_routes_layout) as LinearLayout

        profileViewModel.isRoutesLongClickPressed.observe(viewLifecycleOwner, {

            val deleteText = root.findViewById(R.id.removeText) as TextView
            val sightsView = root.findViewById(R.id.favoritesSightsRView) as RecyclerView
            if (it == true) {
                deleteFrame.visibility = View.VISIBLE
                sightsView.alpha = 0.5f
                profileViewModel.savedSightsEnabled.postValue(false)
                deleteText.text = getString(R.string.remove_routes)

            } else {

                deleteFrame.visibility = View.GONE
                sightsView.alpha = 1f
                profileViewModel.savedSightsEnabled.postValue(true)
            }
        })

        profileViewModel.isSightsLongClickPressed.observe(viewLifecycleOwner, {

            val routesView = root.findViewById(R.id.favoritesRoutesRView) as RecyclerView
            val deleteText = root.findViewById(R.id.removeText) as TextView

            if (it == true) {

                deleteFrame.visibility = View.VISIBLE
                routesView.alpha = 0.5f
                profileViewModel.savedRoutesEnabled.postValue(false)
                deleteText.text = getString(R.string.remove_sights)

            } else {

                deleteFrame.visibility = View.GONE
                routesView.alpha = 1f
                profileViewModel.savedRoutesEnabled.postValue(true)

            }
        })

        val removeImage = root.findViewById(R.id.removeImage) as ImageView

        removeImage.setOnClickListener {

            val selectedRouteItemsList = profileViewModel.selectedRouteItems.value
            val selectedSightItemsList = profileViewModel.selectedSightItems.value


            if (!selectedRouteItemsList.isNullOrEmpty()) {
                handleRouteSelectedItems(selectedRouteItemsList)
            }

            if (!selectedSightItemsList.isNullOrEmpty()) {
                handleSightSelectedItems(selectedSightItemsList)
            }
        }


        // TODO Populate with data from DB and will be related to User
        routes =
            MockDatabase.mockSearchResults.stream().map { it.third }.collect(Collectors.toList())
        profileViewModel.savedRoutes.postValue(routes)
        // TODO Populate Completed routes (and with data from db. Associated to user!!)
        profileViewModel.completedRoutes.postValue(routes)


        sights = routes.stream().flatMap { it.cultureInfo?.sights?.stream() }
            .collect(Collectors.toList())

        profileViewModel.savedSights.postValue(sights)
        profileViewModel.completedSights.postValue(sights)

        return root
    }

    private fun handleSightSelectedItems(selectedSightItemsList: MutableList<Int>) {

        val sightsCopy = mutableListOf<Sight>().apply { addAll(sights) }
        sights.clear()

        sightsCopy.withIndex().forEach {
            if (!selectedSightItemsList.contains(it.index)) {
                sights.add(it.value)
            }
        }

        selectedSightItemsList.clear()
        profileViewModel.selectedSightItems.postValue(selectedSightItemsList)
        profileViewModel.savedSights.postValue(sights)
        profileViewModel.isSightsLongClickPressed.postValue(false)
    }

    private fun handleRouteSelectedItems(selectedRouteItemsList: MutableList<Int>) {
        val routesCopy = mutableListOf<Route>().apply { addAll(routes) }
        routes.clear()

        routesCopy.withIndex().forEach {
            if (!selectedRouteItemsList.contains(it.index)) {
                routes.add(it.value)
            }
        }

        selectedRouteItemsList.clear()
        profileViewModel.selectedRouteItems.postValue(selectedRouteItemsList)
        profileViewModel.savedRoutes.postValue(routes)
        profileViewModel.isRoutesLongClickPressed.postValue(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}