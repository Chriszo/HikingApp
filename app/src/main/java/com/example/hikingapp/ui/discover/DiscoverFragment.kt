package com.example.hikingapp.ui.discover

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hikingapp.SampleMapActivity
import com.example.hikingapp.databinding.FragmentDiscoverBinding
import com.example.hikingapp.SampleNavigationActivity

class DiscoverFragment : Fragment() {

    private lateinit var discoverViewModel: DiscoverViewModel
    private var _binding: FragmentDiscoverBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        discoverViewModel =
            ViewModelProvider(this).get(DiscoverViewModel::class.java)

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDiscover
        discoverViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val showMapButton: Button = binding.showMap
        showMapButton.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleMapActivity::class.java)
                it.startActivity(intent)
            }
        }

        val showNavButton: Button = binding.navigate
        showNavButton.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleNavigationActivity::class.java)
                it.startActivity(intent)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}