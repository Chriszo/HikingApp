package com.example.hikingapp.ui.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentProfileBinding
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.mock.db.MockDatabase
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.stream.Collectors

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var _binding: FragmentProfileBinding? = null

    private lateinit var routes: MutableList<Route>

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

        val navHost = childFragmentManager.findFragmentById(R.id.profileFragmentContainer) as NavHostFragment
        val navController = navHost.navController

        val navView = root.info_nav_view
        navView.setupWithNavController(navController)

        // TODO Populate with data from DB
        routes = MockDatabase.mockSearchResults.stream().map { it.third }.collect(Collectors.toList())
        profileViewModel.favoriteRoutes.postValue(routes)
        // TODO Populate Completed routes (and with data from db. Associated to user!!)
        profileViewModel.completedRoutes.postValue(routes)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}