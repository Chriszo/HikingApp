package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityAddContactBinding
import com.example.hikingapp.domain.users.Contact
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class AddContactActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityAddContactBinding
    private var authInfo: FirebaseUser? = null
    private var contactsWrapper: ContactsWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        (binding.toolbarContainer.actionBarTitle as TextView).text = "Add Contact"

        if (intent.extras?.containsKey("authInfo") == true) {

            authInfo = intent.extras?.get("authInfo") as FirebaseUser?

            contactsWrapper =
                if (intent.extras!!.containsKey("contacts") && (intent.extras!!["contacts"] as ContactsWrapper).contacts.isNotEmpty())
                    intent.extras!!["contacts"] as ContactsWrapper
                else
                    null

            if (authInfo != null) {

                binding.toolbarContainer.actionBarUser.visibility = View.GONE
                binding.toolbarContainer.accountIcon.visibility = View.VISIBLE

                binding.submitAddButton.setOnClickListener {
                    if (authInfo != null) {

                        var newContact = Contact(
                            binding.contactNameText.text.toString(),
                            binding.phoneText.text.toString(),
                            binding.emailText.text.toString()
                        )

                        if (contactsWrapper == null) {
                            contactsWrapper = ContactsWrapper(contacts = mutableListOf())
                        }
                        contactsWrapper!!.contacts.add(newContact)
                        FirebaseDatabase.getInstance().getReference("contacts")
                            .child("user${authInfo!!.uid}").setValue(contactsWrapper!!.contacts)

                        Toast.makeText(this, "New contact added successfully!", Toast.LENGTH_LONG).show()

                        val intent = Intent(this, ContactsActivity::class.java)
                        intent.putExtra("authInfo", authInfo)
                        startActivity(intent)
                    }
                }

                binding.backButton.setOnClickListener {
                    if (authInfo != null) {
                        val intent = Intent(this, ContactsActivity::class.java)
                        intent.putExtra("authInfo", authInfo)
                        startActivity(intent)
                    }
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