package com.example.hikingapp.ui.route.photos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.utils.PhotoItemDecorator
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_photos.view.*

class RoutePhotosFragment : Fragment(), OnItemClickedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var itemClickedListener: OnItemClickedListener
    private lateinit var photosAdapter: PhotoAdapter
    private val routeViewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var photos: List<PhotoItem?>
    private var routeId: Long = 0
    private var authInfo: FirebaseUser? = null

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

        val view = inflater.inflate(R.layout.fragment_photos, container, false)

        linearLayoutManager = GridLayoutManager(context,5)
        linearLayoutManager.orientation = GridLayoutManager.VERTICAL
        recyclerView = view.findViewById(R.id.sight_photos_recycler_view)
        recyclerView.layoutManager = linearLayoutManager

        val photoItemSpacing = PhotoItemDecorator(5)
        recyclerView.addItemDecoration(photoItemSpacing)

        val progressBar = view.findViewById(R.id.progress_bar) as ProgressBar
        progressBar.visibility = View.VISIBLE

        routeViewModel.photos.observe(viewLifecycleOwner, {

            if (it.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                view.no_photos_found.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                view.no_photos_found.visibility = View.GONE

                photos = it
                photosAdapter = PhotoAdapter(context, photos, itemClickedListener)
                recyclerView.adapter = photosAdapter
                progressBar.visibility = View.GONE
            }
        })

        routeViewModel.route.observe(viewLifecycleOwner,{
            routeId = it.routeId
        })

        userViewModel.user.observe(viewLifecycleOwner,{
            authInfo = it
        })

        return view
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(context, PhotoActivity::class.java)
        intent.putExtra("photo_item", photos[position]!!.imageName)
        intent.putExtra("itemId", "R$routeId")
        intent.putExtra("authInfo",authInfo)
        startActivity(intent)
    }

}