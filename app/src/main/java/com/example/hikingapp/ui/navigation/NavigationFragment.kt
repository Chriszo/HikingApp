package com.example.hikingapp.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.databinding.FragmentNavigationBinding
import com.example.hikingapp.viewModels.UserViewModel

class NavigationFragment : Fragment() {

    private lateinit var navigationViewModel: NavigationViewModel
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentNavigationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().actionBar?.title = "Navigation"
        if (userViewModel.user.value == null) {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        } else {
            navigationViewModel =
                ViewModelProvider(this).get(NavigationViewModel::class.java)

            _binding = FragmentNavigationBinding.inflate(inflater, container, false)
            val root: View = binding.root

            val textView: TextView = binding.textNavigation
            navigationViewModel.text.observe(viewLifecycleOwner, {
                textView.text = it
            })
            return root
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}