package com.example.hikingapp.ui.route.photos

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivityPhotoBinding
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoBinding
    private var authInfo: FirebaseUser? = null
    private var voted = false
    private lateinit var itemId: String
    private lateinit var imageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoBinding.inflate(layoutInflater)

        setContentView(binding.root)

        intent.extras?.get("photo_item")?.let {

            imageName = (it as String).split(".")[0]
            val bitmap = LocalDatabase.getBitmapForImageName(imageName)
            if (bitmap != null) {
                binding.photoId.setImageDrawable(BitmapDrawable(resources, bitmap))
            }

        }

        intent.extras?.get("itemId")?.let {
            itemId = it as String
        }

        intent.extras?.get("authInfo")?.let {
            authInfo = it as FirebaseUser
        }


        if (authInfo != null) {
            FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid)
                .child(itemId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val votedImage = snapshot.value as String
                            if (votedImage.trim() == imageName) {
                                binding.voteIcon.setImageResource(R.drawable.rate_icon_foreground_white)
                                this@PhotoActivity.voted = true
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

        }

        binding.voteIcon.setOnClickListener {
            if (authInfo != null) {
                if (voted) {
                    voted = false
                    FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid)
                        .child(itemId).removeValue().addOnSuccessListener {
                            binding.voteIcon.setImageResource(R.drawable.vote_icon_foreground)
                        }
                } else {
                    voted = true
                    FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid)
                        .child(itemId).setValue(imageName).addOnSuccessListener {
                            binding.voteIcon.setImageResource(R.drawable.rate_icon_foreground_white)
                        }
                }
            } else {
                val redirectIntent = Intent(this, LoginActivity::class.java)
                redirectIntent.putExtra(GlobalUtils.LAST_PAGE, "PhotoActivity")
                redirectIntent.putExtra("photo_item", imageName)
                redirectIntent.putExtra("itemId", itemId)
                redirectIntent.putExtra("authInfo", authInfo)
                startActivity(redirectIntent)
            }

        }

        binding.bookmarkPhotoIcon.setOnClickListener {
            if (authInfo != null) {
                FirebaseDatabase.getInstance().getReference("savedPhotosAssociations")
                    .child(authInfo!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val savedUserPhotos =
                                    snapshot.value as MutableList<String>
                                if (!savedUserPhotos.contains(imageName)) {
                                    savedUserPhotos.add(imageName)
                                    binding.bookmarkPhotoIcon.setImageResource(R.drawable.remove_bookmark_icon_foreground_white)
                                } else {
                                    savedUserPhotos.remove(imageName)
                                    binding.bookmarkPhotoIcon.setImageResource(R.drawable.bookmark_outlined_icon_foreground_white)
                                }
                                FirebaseDatabase.getInstance()
                                    .getReference("savedPhotosAssociations")
                                    .child(authInfo!!.uid).setValue(savedUserPhotos)
                            } else {
                                FirebaseDatabase.getInstance()
                                    .getReference("savedPhotosAssociations")
                                    .child(authInfo!!.uid).setValue(
                                        mutableListOf(imageName)
                                    )
                                binding.bookmarkPhotoIcon.setImageResource(R.drawable.remove_bookmark_icon_foreground_white)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            } else {
                val redirectIntent = Intent(this, LoginActivity::class.java)
                redirectIntent.putExtra(GlobalUtils.LAST_PAGE, "PhotoActivity")
                redirectIntent.putExtra("photo_item", imageName)
                redirectIntent.putExtra("itemId", itemId)
                redirectIntent.putExtra("authInfo", authInfo)
                startActivity(redirectIntent)
            }


        }

    }
}