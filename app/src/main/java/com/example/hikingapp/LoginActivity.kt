package com.example.hikingapp

import android.app.ActionBar
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

//    private lateinit var actionBar: ActionBar
    private lateinit var progressDialog: ProgressDialog

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
//
//        actionBar = supportActionBar
//        actionBar.title = "Login"

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Logging in...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUpHint.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
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
            firebaseLogin(userMail, userPassword)
        }

    }

    private fun firebaseLogin(userMail: String, userPassword: String) {
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(userMail, userPassword)
            .addOnSuccessListener {
                progressDialog.dismiss()

                val user = firebaseAuth.currentUser

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("authInfo", user)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed. Reason: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }


}