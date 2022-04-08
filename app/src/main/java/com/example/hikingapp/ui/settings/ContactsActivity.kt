package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivityContactsBinding
import com.example.hikingapp.domain.users.Contact
import com.example.hikingapp.ui.adapters.ContactAdapter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactsActivity : AppCompatActivity(), BackButtonListener {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactAdapter

    private var authInfo: FirebaseUser? = null
    private var contacts: MutableList<Contact> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactsBinding.inflate(layoutInflater)
        setBackButtonListener()

        setContentView(binding.root)

        authInfo = intent.extras?.get("authInfo") as FirebaseUser?

        val title = binding.toolbarContainer.actionBarTitle as TextView
        title.text = "Contacts"

        recyclerView = binding.contactsRecyclerView

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        FirebaseDatabase.getInstance().getReference("contacts").child("user${authInfo!!.uid}").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var contactsCounted = 0

                if (snapshot.exists()) {
                    val retrievedContacts = snapshot.value as MutableList<HashMap<String,String>>
                    contactsCounted = retrievedContacts.size
                    retrievedContacts.forEach {
                        val contact = Contact(it["name"]!!,it["phoneNumber"]!!,it["email"]!!)
                        contacts.add(contact)

                        if (contactsCounted == contacts.size) {
                            contactsAdapter = ContactAdapter(this@ContactsActivity, authInfo,contacts)
                            recyclerView.adapter = contactsAdapter
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.addContact.setOnClickListener {
            val addContactIntent = Intent(this, AddContactActivity::class.java)
            addContactIntent.putExtra("contacts", ContactsWrapper(contacts))
            addContactIntent.putExtra("authInfo", authInfo)
            startActivity(addContactIntent)
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