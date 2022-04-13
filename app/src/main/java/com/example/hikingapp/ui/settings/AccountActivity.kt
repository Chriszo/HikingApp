package com.example.hikingapp.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.MainActivity
import com.example.hikingapp.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_account.view.*
import kotlinx.android.synthetic.main.activity_logout.view.*
import kotlinx.android.synthetic.main.custom_toolbar.view.*
import kotlinx.android.synthetic.main.custom_toolbar.view.toolbar

class AccountActivity : AppCompatActivity(), BackButtonListener {

    private var toolbarContainer: ConstraintLayout? = null
    private var authInfo: FirebaseUser? = null


    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val userNameView = findViewById<EditText>(R.id.userNameText)
        val emailView = findViewById<EditText>(R.id.emailText)
        val passwordView = findViewById<EditText>(R.id.passwordText)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val okButton = findViewById<Button>(R.id.okButton)
        val editButton = findViewById<Button>(R.id.editButton)
        val deletButton = findViewById<Button>(R.id.deleteButton)

        toolbarContainer = findViewById<ConstraintLayout>(R.id.toolbarContainer)

        (toolbarContainer!!.action_bar_title as TextView).text = "Account"
        setBackButtonListener()

        if (intent.extras?.containsKey("authInfo") == true) {
            (intent.extras!!["authInfo"] as FirebaseUser).apply {
                authInfo = this

                FirebaseDatabase.getInstance().getReference("users").child("user${authInfo!!.uid}").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        toolbarContainer!!.toolbar.account_icon.visibility = View.VISIBLE
                        toolbarContainer!!.toolbar.action_bar_user.visibility = View.GONE

                        if (snapshot.exists()) {
                            val userData = snapshot.value as HashMap<String, *>
                            val userName = userData["userName"] as String?
                            val email = userData["mail"] as String?
                            val password = userData["password"] as String?

                            userNameView.setText(userName)
                            emailView.setText(email)
                            passwordView.setText(password)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

                logoutButton.setOnClickListener {
                    val logoutIntent = Intent(this@AccountActivity, LogoutActivity::class.java)
                    logoutIntent.putExtra("authInfo", authInfo)
                    startActivity(logoutIntent)
                }

                editButton.setOnClickListener {
                    val editIntent = Intent(this@AccountActivity, EditAccountActivity::class.java)
                    editIntent.putExtra("authInfo", authInfo)
                    startActivity(editIntent)
                }

                deletButton.setOnClickListener {
                    val deleteIntent = Intent(this@AccountActivity, DeleteAccountActivity::class.java)
                    deleteIntent.putExtra("authInfo", authInfo)
                    startActivity(deleteIntent)
                }

                okButton.setOnClickListener {
                    val intent = Intent(this@AccountActivity, MainActivity::class.java)
                    intent.putExtra("authInfo", authInfo)
                    startActivity(intent)
                }

            }
        }
    }

    override fun setBackButtonListener() {
        toolbarContainer!!.back_btn.setOnClickListener {
            if (authInfo != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("authInfo", authInfo)
                startActivity(intent)
            }
        }
    }
}