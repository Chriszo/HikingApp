package com.example.hikingapp.ui.route.cultureInfo

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.databinding.ActivitySightDetailsBinding
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.example.hikingapp.ui.utils.PhotoItemDecorator
import com.example.hikingapp.ui.viewModels.RouteViewModel

class SightDetailsActivity : AppCompatActivity(), OnItemClickedListener {

    private lateinit var binding: ActivitySightDetailsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var itemClickedListener: OnItemClickedListener
    private lateinit var photosAdapter: PhotoAdapter
    private lateinit var routeViewModel: RouteViewModel

    private lateinit var photos:List<Int>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySightDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        itemClickedListener = this


        layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        recyclerView = binding.sightPhotosRecyclerView
        recyclerView.layoutManager = layoutManager

        val photoItemSpacing = PhotoItemDecorator(5)
        recyclerView.addItemDecoration(photoItemSpacing)

        val sightInfo = intent.extras!!.get("sightInfo") as Sight

        photos = sightInfo.photos!!

        photosAdapter = PhotoAdapter(photos,itemClickedListener)
        recyclerView.adapter = photosAdapter

        /*routeViewModel.photos.observe(this, Observer {
            photos = it

            photosAdapter = PhotoAdapter(photos,itemClickedListener)
            recyclerView.adapter = photosAdapter
        })*/


        val nameView = binding.sightName
        val descriptionView = binding.sightState
        val mainPhotoView = binding.sightImage


        nameView.text = sightInfo.name
        descriptionView.text = sightInfo.description
        sightInfo.mainPhoto?.let { mainPhotoView.setImageResource(it) }

    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra("photo_item", photos[position])
        startActivity(intent)
    }
}