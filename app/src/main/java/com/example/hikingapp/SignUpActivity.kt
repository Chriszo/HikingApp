package com.example.hikingapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

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
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Sign Up failed due to: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }
}