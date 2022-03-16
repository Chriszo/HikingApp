package com.example.hikingapp.ui.route.cultureInfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.SightsAdapter
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.ui.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser

class SightsListFragment : Fragment(), OnItemClickedListener {

    private var user: FirebaseUser? = null
    private val viewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var sightsAdapter: SightsAdapter

    private lateinit var cultureInfoView: View

    private var cultureInfo: CultureInfo? = null

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var itemClickedListener: OnItemClickedListener

    private lateinit var recyclerView: RecyclerView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemClickedListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        cultureInfoView = inflater.inflate(R.layout.fragment_culture_info, container, false)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView = cultureInfoView.findViewById(R.id.sights_listview)
        recyclerView.layoutManager = linearLayoutManager

        val progressBar = cultureInfoView.findViewById(R.id.progress_bar) as ProgressBar

        progressBar.visibility = View.VISIBLE

        viewModel.cultureInfo.observe(viewLifecycleOwner, { updatedCultureInfo ->
            cultureInfo = updatedCultureInfo

            sightsAdapter = SightsAdapter(context, cultureInfo?.sights!!, itemClickedListener)
            recyclerView.adapter = sightsAdapter
            progressBar.visibility = View.GONE
        })

        userViewModel.user.observe(viewLifecycleOwner, {
            user = it
        })

        return cultureInfoView
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context, SightDetailsActivity::class.java)
        val sight = cultureInfo?.sights?.get(position)
        intent.putExtra("sightInfo", sight)
        intent.putExtra("action", "discover")
        intent.putExtra("authInfo", user)
        startActivity(intent)
    }

}