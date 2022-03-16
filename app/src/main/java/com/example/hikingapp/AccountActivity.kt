package com.example.hikingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.hikingapp.databinding.ActivityAccountBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_account.view.*
import kotlinx.android.synthetic.main.activity_logout.view.*
import kotlinx.android.synthetic.main.custom_toolbar.view.*
import kotlinx.android.synthetic.main.custom_toolbar.view.toolbar

class AccountActivity : AppCompatActivity() {

    private var authInfo: FirebaseUser? = null


    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val emailView = findViewById<EditText>(R.id.emailText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        val toolbarContainer = findViewById<ConstraintLayout>(R.id.toolbarContainer)

        if (intent.extras?.containsKey("authInfo") == true) {
            (intent.extras!!["authInfo"] as FirebaseUser).apply {
                authInfo = this

                toolbarContainer.toolbar.account_icon.visibility = View.VISIBLE
                toolbarContainer.toolbar.action_bar_user.visibility = View.GONE

                emailView.setText(authInfo!!.email)
                logoutButton.setOnClickListener {
                    val logoutIntent = Intent(this@AccountActivity, LogoutActivity::class.java)
                    logoutIntent.putExtra("authInfo", authInfo)
                    startActivity(logoutIntent)
                }
            }
        }
    }
}