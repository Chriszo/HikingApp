package com.example.hikingapp.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivityDeleteContactBinding
import com.example.hikingapp.databinding.ActivityEditContactBinding
import com.example.hikingapp.domain.users.Contact
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class DeleteContactActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityDeleteContactBinding
    private var authInfo: FirebaseUser? = null
    private var contactsWrapper: ContactsWrapper? = null
    private var position: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeleteContactBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        (binding.toolbarContainer.actionBarTitle as TextView).text = "Delete Contact"

        if (intent.extras?.containsKey("authInfo") == true) {

            authInfo = intent.extras?.get("authInfo") as FirebaseUser?
            contactsWrapper = intent.extras?.get("contactsWrapper") as ContactsWrapper?
            position = intent.extras?.get("position") as Int?

            if (authInfo != null) {

                binding.toolbarContainer.actionBarUser.visibility = View.GONE
                binding.toolbarContainer.accountIcon.visibility = View.VISIBLE

                binding.contactNameText.text = contactsWrapper!!.contacts[position!!].name
                binding.emailText.text = contactsWrapper!!.contacts[position!!].email
                binding.phoneText.text = contactsWrapper!!.contacts[position!!].phoneNumber

                binding.backButton.setOnClickListener {
                    if (authInfo != null) {
                        val intent = Intent(this, ContactsActivity::class.java)
                        intent.putExtra("authInfo", authInfo)
                        startActivity(intent)
                    }
                }

                binding.submitDeleteButton.setOnClickListener {

                    contactsWrapper!!.contacts.removeAt(position!!)

                    FirebaseDatabase.getInstance().getReference("contacts")
                        .child("user${authInfo!!.uid}").setValue(contactsWrapper!!.contacts)


                    Toast.makeText(this, "Contact deleted successfully!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, ContactsActivity::class.java)
                    intent.putExtra("authInfo", authInfo)
                    startActivity(intent)
                }
            }
        }
    }

    override fun setBackButtonListener() {
        binding.toolbarContainer.backBtn.setOnClickListener {
            if (authInfo != null) {
                val intent = Intent(this, ContactsActivity::class.java)
                intent.putExtra("authInfo", authInfo)
                startActivity(intent)
            }
        }
    }
}