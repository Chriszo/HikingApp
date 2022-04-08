package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityEditContactBinding
import com.example.hikingapp.domain.users.Contact
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class EditContactActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityEditContactBinding
    private var authInfo: FirebaseUser? = null
    private var contactsWrapper: ContactsWrapper? = null
    private var position: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        (binding.toolbarContainer.actionBarTitle as TextView).text = "Edit Contact"

        if (intent.extras?.containsKey("authInfo") == true) {

            authInfo = intent.extras?.get("authInfo") as FirebaseUser?
            contactsWrapper = intent.extras?.get("contactsWrapper") as ContactsWrapper?
            position = intent.extras?.get("position") as Int?

            if (authInfo != null) {

                binding.toolbarContainer.actionBarUser.visibility = View.GONE
                binding.toolbarContainer.accountIcon.visibility = View.VISIBLE

                binding.contactNameText.setText(contactsWrapper!!.contacts[position!!].name)
                binding.emailText.setText(contactsWrapper!!.contacts[position!!].email)
                binding.phoneText.setText(contactsWrapper!!.contacts[position!!].phoneNumber)

                binding.backButton.setOnClickListener {
                    if (authInfo != null) {
                        val intent = Intent(this, ContactsActivity::class.java)
                        intent.putExtra("authInfo", authInfo)
                        startActivity(intent)
                    }
                }

                binding.submitEditButton.setOnClickListener {

                    val updatedContact = Contact(
                        binding.contactNameText.text.toString(),
                        binding.phoneText.text.toString(),
                        binding.emailText.text.toString()
                    )
                    contactsWrapper!!.contacts[position!!] = updatedContact

                    FirebaseDatabase.getInstance().getReference("contacts")
                        .child("user${authInfo!!.uid}").setValue(contactsWrapper!!.contacts)


                    Toast.makeText(this, "Contact updated successfully!", Toast.LENGTH_LONG).show()

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