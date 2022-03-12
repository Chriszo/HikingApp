package com.example.hikingapp.ui.route.cultureInfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.SightsAdapter
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.ui.viewModels.SightsViewModel

class SightsListFragment : Fragment(), OnItemClickedListener {

    private val viewModel: RouteViewModel by activityViewModels()
    private val sightsViewModel: SightsViewModel by activityViewModels()

    private lateinit var sightsAdapter: SightsAdapter

    private lateinit var cultureInfoView: View

    private lateinit var cultureInfo: CultureInfo

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


        viewModel.cultureInfo.observe(viewLifecycleOwner, { updatedCultureInfo ->
            cultureInfo = updatedCultureInfo

            sightsAdapter = SightsAdapter(cultureInfo.sights!!, itemClickedListener)
            recyclerView.adapter = sightsAdapter
        })

        return cultureInfoView
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context,SightDetailsActivity::class.java)
        val sight = cultureInfo.sights?.get(position)
        sight?.mainPhoto = R.drawable.sunny
        sight?.photos = viewModel.photos.value?.toMutableList()
        intent.putExtra("sightInfo",sight)
        startActivity(intent)
    }

}