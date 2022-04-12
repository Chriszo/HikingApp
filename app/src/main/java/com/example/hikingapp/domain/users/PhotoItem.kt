package com.example.hikingapp.domain.users

import android.graphics.Bitmap
import java.io.Serializable

class PhotoItem(var imageName: String?, @Transient var imageBitmap: Bitmap?): Serializable {
}