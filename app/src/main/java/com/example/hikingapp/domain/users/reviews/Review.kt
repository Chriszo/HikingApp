package com.example.hikingapp.domain.users.reviews

import android.graphics.Bitmap
import java.io.Serializable

class Review(@Transient var userImage: Bitmap?, var userName: String?, var review: String?, var rating: Number?): Serializable {

    constructor():this(null,null,null,null)

}