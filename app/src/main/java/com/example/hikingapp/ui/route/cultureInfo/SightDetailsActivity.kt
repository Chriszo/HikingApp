package com.example.hikingapp.ui.route.cultureInfo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivitySightDetailsBinding
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.PhotoAdapter
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.example.hikingapp.ui.utils.PhotoItemDecorator
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import java.util.*

class SightDetailsActivity : AppCompatActivity(), OnItemClickedListener {

    private lateinit var binding: ActivitySightDetailsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var itemClickedListener: OnItemClickedListener
    private lateinit var photosAdapter: PhotoAdapter
    private lateinit var routeViewModel: RouteViewModel

    private lateinit var sightInfo: Sight

    private lateinit var photos: MutableList<Bitmap?>

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySightDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        itemClickedListener = this


        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView = binding.sightPhotosRecyclerView
        recyclerView.layoutManager = layoutManager

        val photoItemSpacing = PhotoItemDecorator(5)
        recyclerView.addItemDecoration(photoItemSpacing)

        sightInfo = intent.extras!!.get("sightInfo") as Sight

        val nameView = binding.sightName
        val descriptionView = binding.sightState
        val mainPhotoView = binding.sightImage
        val ratingView = binding.sightRating

        initializeButtonListeners()


        nameView.text = sightInfo.name
        descriptionView.text = sightInfo.description

        sightInfo.mainPhoto =
            LocalDatabase.getMainImage(sightInfo.sightId, Sight::class.java.simpleName)
        if (sightInfo.mainPhoto == null) {
            FirebaseStorage.getInstance().reference.child("sights/mainPhotos/sight_${sightInfo.sightId}_main.jpg")
                .getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    sightInfo.mainPhoto = bitmap
                    sightInfo.mainPhoto?.let {
                        mainPhotoView.setImageDrawable(
                            BitmapDrawable(
                                this.resources,
                                it
                            )
                        )
                    }
                }.addOnFailureListener {
                    if (it is StorageException) {
                        when (it.httpResultCode) {
                            404 -> Log.i(
                                this.toString(),
                                "Main photo does not exist for sight with id: ${sightInfo.sightId}"
                            )
                        }
                    }
                }
        } else {
            sightInfo.mainPhoto?.let {
                mainPhotoView.setImageDrawable(
                    BitmapDrawable(
                        this.resources,
                        it
                    )
                )
            }
        }


        photos = LocalDatabase.getImages(sightInfo.sightId, Sight::class.java.simpleName)
            ?: mutableListOf()
        if (photos.isNullOrEmpty()) {
            FirebaseStorage.getInstance().reference.child("sights/${sightInfo.sightId}").listAll()
                .addOnSuccessListener { sightPhotos ->
                    sightPhotos.items.forEach { photoReference ->
                        photoReference.getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                            if (Objects.isNull(photos)) {
                                photos = mutableListOf()
                            }
                            photos.add(bitmap)
                            if (photos.size == sightPhotos.items.size) {
                                photosAdapter = PhotoAdapter(this, photos, itemClickedListener)
                                recyclerView.adapter = photosAdapter
                            }
                        }.addOnFailureListener {
                            if (it is StorageException) {
                                when (it.httpResultCode) {
                                    404 -> {
                                        Log.e(
                                            this.toString(),
                                            "No image found for sight with id: ${sightInfo.sightId} with name: ${
                                                photoReference.path.split("/").last()
                                            }"
                                        )
                                        photos.add(null)
                                        if (photos.size == sightPhotos.items.size) {
                                            photosAdapter =
                                                PhotoAdapter(this, photos, itemClickedListener)
                                            recyclerView.adapter = photosAdapter
                                        }
                                    }
                                }
                            } else {
                                Log.e(this.toString(), "Exception occured: ${it.message}", it)
                            }
                        }
                    }
                }
        }

        sightInfo.rating?.let { ratingView.rating = it }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeButtonListeners() {

        val bookmarkButton = binding.sightBookmark
        val mapButton = binding.showMap
        val navigationButton = binding.navigate

        val authInfo: FirebaseUser? = intent.extras?.get("authInfo") as FirebaseUser? ?: null

        if (Objects.nonNull(authInfo)) {
            database.getReference("savedSightAssociations").child(authInfo!!.uid)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                            val savedRouteIds = snapshot.value as MutableList<Long>
                            if (savedRouteIds.contains(sightInfo.sightId)) {
                                bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                            } else {
                                bookmarkButton.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            bookmarkButton.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
        }

        if (intent.extras?.containsKey("action") == true) {
            when (intent.extras!!["action"] as String) {
                "discover" -> {
                    bookmarkButton.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedSightAssociations").child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                                            val savedUserSights =
                                                snapshot.value as MutableList<Long>
                                            if (savedUserSights.contains(sightInfo.sightId)) {
                                                savedUserSights.remove(sightInfo.sightId)
                                                bookmarkButton.setImageResource(R.drawable.bookmark_outlined_icon_foreground)
                                            } else {
                                                savedUserSights.add(sightInfo.sightId)
                                                bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                                            }
                                            database.getReference("savedSightAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserSights)
                                        } else {
                                            database.getReference("savedSightAssociations")
                                                .child(authInfo!!.uid).setValue(
                                                    mutableListOf(sightInfo.sightId)
                                                )
                                            bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                        } else {
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    }
                }
                "saved" -> {
                    bookmarkButton.setImageResource(R.drawable.remove_bookmark_icon_foreground)
                    bookmarkButton.setOnClickListener {
                        if (Objects.nonNull(authInfo)) {
                            database.getReference("savedSightAssociations").child(authInfo!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (Objects.nonNull(snapshot) && Objects.nonNull(snapshot.value)) {
                                            val savedUserSights =
                                                snapshot.value as MutableList<Long>
                                            savedUserSights.remove(sightInfo.sightId)
                                            database.getReference("savedSightAssociations")
                                                .child(authInfo!!.uid).setValue(savedUserSights)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                        } else {
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    }
                }
            }
        }

    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra("photo_item", photos[position])
        startActivity(intent)
    }
}