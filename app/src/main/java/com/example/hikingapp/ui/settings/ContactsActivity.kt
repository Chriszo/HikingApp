package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityContactsBinding
import com.google.firebase.auth.FirebaseUser

class ContactsActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityContactsBinding
    private var authInfo: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactsBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        authInfo = intent.extras?.get("authInfo") as FirebaseUser?

        val title = binding.toolbarContainer.actionBarTitle as TextView
        title.text = "Contacts"
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