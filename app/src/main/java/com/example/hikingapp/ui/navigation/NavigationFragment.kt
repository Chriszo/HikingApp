package com.example.hikingapp.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hikingapp.databinding.FragmentNavigationBinding

class NavigationFragment : Fragment() {

    private lateinit var navigationViewModel: NavigationViewModel
    private var _binding: FragmentNavigationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigationViewModel =
            ViewModelProvider(this).get(NavigationViewModel::class.java)

        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNavigation
        navigationViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}