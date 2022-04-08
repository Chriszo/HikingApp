package com.example.hikingapp.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteAccountActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityDeleteAccountBinding
    private var authInfo: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        (binding.toolbarContainer.actionBarTitle as TextView).text = "Delete Account"

        if (intent.extras?.containsKey("authInfo") == true) {


            authInfo = intent.extras?.get("authInfo") as FirebaseUser?

            if (authInfo != null) {

                binding.toolbarContainer.actionBarUser.visibility = View.GONE
                binding.toolbarContainer.accountIcon.visibility = View.VISIBLE

                FirebaseDatabase.getInstance().getReference("users").child("user${authInfo!!.uid}").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            val userName = (snapshot.value as HashMap<String,*>)["userName"] as String?
                            binding.userName.text = userName
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

            binding.submitDeleteButton.setOnClickListener {
                if (authInfo != null) {
                    FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener {

                        FirebaseDatabase.getInstance().getReference("users").child("user${authInfo!!.uid}").removeValue()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }

            binding.backButton.setOnClickListener {
                if (authInfo != null) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("authInfo", authInfo)
                    startActivity(intent)
                }
            }
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