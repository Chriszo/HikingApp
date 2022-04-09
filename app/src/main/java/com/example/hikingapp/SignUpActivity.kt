package com.example.hikingapp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivitySignUpBinding
import com.example.hikingapp.domain.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.navigation.ui.utils.internal.extensions.getBitmap
import java.io.ByteArrayOutputStream

class SignUpActivity : AppCompatActivity() {

    private val SELECT_PICTURE = 200
    private lateinit var progressDialog: ProgressDialog

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Signing up...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.signUpButton.setOnClickListener {
            validateData()
        }

        binding.accountImage.setOnClickListener {
            binding.imageSelector.performClick()
        }

        binding.imageSelector.setOnClickListener {
            selectImage()
        }


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

    private fun validateData() {
        val userMail = binding.emailText.text.toString()
        val userPassword = binding.passwordText.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(userMail).matches()) {
            binding.emailText.error = "Invalid email provided"
        } else if (TextUtils.isEmpty(userPassword)) {
            binding.passwordText.error = "Please enter password"
        } else {
            firebaseSignUp(userMail, userPassword)
        }
    }

    private fun firebaseSignUp(userMail: String, userPassword: String) {
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(userMail, userPassword).addOnSuccessListener {
            progressDialog.dismiss()
            val user = firebaseAuth.currentUser

            val newUser = User(user?.uid!!,user?.email!!.split("@")[0],user?.email!!,"pass", null)
            FirebaseDatabase.getInstance().getReference("users").child("user${user.uid}").setValue(newUser).addOnSuccessListener {
                addAccountImage(user?.email!!)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
                finish()
            }

        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Sign Up failed due to: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addAccountImage(email: String) {

        val userName = email.split("@")[0]

        val bitmap = binding.accountImage.drawable.getBitmap()

        val os = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG,100,os)

        FirebaseStorage.getInstance()
            .getReference("users")
            .child("$userName/")
            .child(userName + "_icon.png")
            .putBytes(os.toByteArray())
    }
}