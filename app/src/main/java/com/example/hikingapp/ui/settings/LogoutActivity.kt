package com.example.hikingapp.ui.settings

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.hikingapp.MainActivity
import com.example.hikingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LogoutActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        auth = FirebaseAuth.getInstance()

        val mail = (intent?.extras?.get("authInfo") as FirebaseUser).email

        val mailView = findViewById<TextView>(R.id.userMail)
        mailView.text = mail ?: ""


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Logging out...")
        progressDialog.setCanceledOnTouchOutside(false)

        val logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            progressDialog.show()
            auth.signOut()
            progressDialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}