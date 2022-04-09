package com.example.hikingapp.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityEditAccountBinding
import com.example.hikingapp.domain.users.User
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import java.io.ByteArrayOutputStream


class EditAccountActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityEditAccountBinding
    private var authInfo: FirebaseUser? = null

    // the activity result code
    private var SELECT_PICTURE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditAccountBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        (binding.toolbarContainer.actionBarTitle as TextView).text = "Edit Account"

        if (intent.extras?.containsKey("authInfo") == true) {

            authInfo = intent.extras?.get("authInfo") as FirebaseUser?

            if (authInfo != null) {

                binding.toolbarContainer.actionBarUser.visibility = View.GONE
                binding.toolbarContainer.accountIcon.visibility = View.VISIBLE



                FirebaseDatabase.getInstance().getReference("users").child("user${authInfo!!.uid}")
                    .addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if (snapshot.exists()) {
                                val userData = snapshot.value as HashMap<String, *>
                                val userName = userData["userName"] as String?
                                val password = userData["password"] as String?

                                binding.userNameText.setText(userName)
                                binding.emailText.setText(authInfo!!.email)
                                binding.passwordText.setText(password)

                                FirebaseStorage.getInstance().getReference("users")
                                    .child(binding.userNameText.text.toString())
                                    .child(binding.userNameText.text.toString() + "_icon.png")
                                    .getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {

                                        binding.accountImage.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))

                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            } else {
                binding.toolbarContainer.actionBarUser.visibility = View.VISIBLE
                binding.toolbarContainer.accountIcon.visibility = View.GONE
            }

            binding.submitEditButton.setOnClickListener {
                if (authInfo != null) {

                    updateUser(authInfo!!)
                    updateUserImage()
                }
            }

            binding.backButton.setOnClickListener {
                if (authInfo != null) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("authInfo", authInfo)
                    startActivity(intent)
                }
            }

            binding.imageSelector.setOnClickListener {
                selectImage()
            }
        }
    }

    private fun updateUserImage() {

        val bitmap = binding.accountImage.drawable.getBitmap()

        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)

        FirebaseStorage.getInstance()
            .getReference("users")
            .child(binding.userNameText.text.toString() + "/" + binding.userNameText.text.toString() + "_icon.png")
            .putBytes(outStream.toByteArray())
    }

    private fun selectImage() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        // pass the constant to compare it
        // with the returned requestCode

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)

        if (resultCode === RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode === SELECT_PICTURE) {
                // Get the url of the image from data
                val selectedImageUri: Uri = dataIntent?.data!!
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    binding.accountImage.setImageURI(selectedImageUri)
                }
            }


        }
    }

    private fun updateUser(authInfo: FirebaseUser) {
        FirebaseAuth.getInstance().currentUser!!.updateEmail(binding.emailText.text.toString())
            .addOnSuccessListener {

                val updatedAuthInfo = FirebaseAuth.getInstance().currentUser
                val updatedUser = User(
                    authInfo.uid,
                    binding.userNameText.text.toString(),
                    binding.emailText.text.toString(),
                    binding.passwordText.text.toString(),
                    null
                )
                FirebaseDatabase.getInstance().getReference("users")
                    .child("user${updatedAuthInfo!!.uid}")
                    .setValue(updatedUser)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("authInfo", updatedAuthInfo)
                startActivity(intent)
            }
    }

    override fun setBackButtonListener() {
        binding.toolbarContainer.backBtn.setOnClickListener {
            if (authInfo != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("authInfo", authInfo)
                startActivity(intent)
            }
        }
    }
}