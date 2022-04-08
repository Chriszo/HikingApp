package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityEditAccountBinding
import com.example.hikingapp.domain.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditAccountActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityEditAccountBinding
    private var authInfo: FirebaseUser? = null

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
                FirebaseDatabase.getInstance().getReference("users").child("user${updatedAuthInfo!!.uid}")
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