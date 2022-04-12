package com.example.hikingapp.ui.route.photos

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivityPhotoBinding
import com.example.hikingapp.persistence.local.LocalDatabase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoBinding
    private var authInfo: FirebaseUser? = null
    private var voted = false
    private var routeId = 0L
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

        intent.extras?.get("routeId")?.let {
            routeId = it as Long
        }

        intent.extras?.get("authInfo")?.let {
            authInfo = it as FirebaseUser
        }


        if (authInfo != null) {
            FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid).child(routeId.toString()).addValueEventListener(object : ValueEventListener {
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


            binding.voteIcon.setOnClickListener {
                if (voted) {
                    voted = false
                    FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid).child(routeId.toString()).removeValue().addOnSuccessListener {
                        binding.voteIcon.setImageResource(R.drawable.vote_icon_foreground)
                    }

//                        .child(imageName.split(".")[0]).child(authInfo!!.uid).setValue(voted).addOnSuccessListener {
//                        binding.voteIcon.setImageResource(R.drawable.vote_icon_foreground)
                } else {
                    voted = true
                    FirebaseDatabase.getInstance().getReference("image_votes").child(authInfo!!.uid).child(routeId.toString()).setValue(imageName).addOnSuccessListener {
                        binding.voteIcon.setImageResource(R.drawable.rate_icon_foreground_white)
                    }
//                    child(imageName.split(".")[0]).child(authInfo!!.uid).setValue(voted).addOnSuccessListener {
//                        binding.voteIcon.setImageResource(R.drawable.rate_icon_foreground_white)
//                    }
                }
            }
        }

    }
}