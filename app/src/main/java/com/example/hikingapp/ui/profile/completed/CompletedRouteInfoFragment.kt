package com.example.hikingapp.ui.profile.completed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.hikingapp.R
import com.example.hikingapp.ui.profile.ProfileViewModel
import com.example.hikingapp.ui.viewModels.RouteViewModel

class CompletedRouteInfoFragment : Fragment() {

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completed_route_info, container, false)
    }
}