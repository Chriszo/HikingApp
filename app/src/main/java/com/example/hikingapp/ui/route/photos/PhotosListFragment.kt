package com.example.hikingapp.ui.route.photos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.viewModels.RouteViewModel

class PhotosListFragment : Fragment(), OnItemClickedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: GridLayoutManager
    private lateinit var itemClickedListener: OnItemClickedListener
    private lateinit var photosAdapter: PhotoAdapter
    private val routeViewModel: RouteViewModel by activityViewModels()

    private lateinit var photos: List<Bitmap>

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
        val view = inflater.inflate(R.layout.fragment_photos, container, false)

        linearLayoutManager = GridLayoutManager(context, 5)
        recyclerView = view.findViewById(R.id.sight_photos_recycler_view)
        recyclerView.layoutManager = linearLayoutManager


        routeViewModel.photos.observe(viewLifecycleOwner, {
            photos = it

            photosAdapter = PhotoAdapter(context, photos, itemClickedListener)
            recyclerView.adapter = photosAdapter
        })

        return view
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context, PhotoActivity::class.java)
        startActivity(intent)
    }

}