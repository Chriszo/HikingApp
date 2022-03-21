package com.example.hikingapp.ui.route.photos

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.R

class PhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        val photoView: ImageView = this.findViewById(R.id.photo_id)

        intent.extras?.get("photo_item")?.let {
            photoView.setImageDrawable(BitmapDrawable(resources, it as Bitmap) )
        }
    }
}