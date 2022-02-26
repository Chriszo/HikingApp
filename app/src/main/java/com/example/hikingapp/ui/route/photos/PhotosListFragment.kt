package com.example.hikingapp.ui.route.photos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.viewModels.RouteViewModel

class PhotosListFragment : Fragment(), OnItemClickedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var itemClickedListener: OnItemClickedListener
    private lateinit var photosAdapter: PhotoAdapter
    private val routeViewModel: RouteViewModel by activityViewModels()

    private lateinit var photos: List<Int>

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
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_photos_list, container, false)
        
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.sight_photos_recycler_view)
        recyclerView.layoutManager = linearLayoutManager
        

        routeViewModel.photos.observe(viewLifecycleOwner, Observer {
            photos = it

            photosAdapter = PhotoAdapter(photos,itemClickedListener)
            recyclerView.adapter = photosAdapter
        })

        return view
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context,PhotoActivity::class.java)
        startActivity(intent)
    }

}