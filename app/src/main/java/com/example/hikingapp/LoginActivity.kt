package com.example.hikingapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivityLoginBinding
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.settings.UserSettings
import com.example.hikingapp.ui.profile.HistoryFragment
import com.example.hikingapp.ui.profile.statistics.StatisticsFragment
import com.example.hikingapp.ui.route.photos.PhotoActivity
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //    private lateinit var actionBar: ActionBar
    private lateinit var progressDialog: ProgressDialog

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
//
//        actionBar = supportActionBar
//        actionBar.title = "Login"

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Logging in...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUpHint.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        val userMail = binding.emailText.text.toString()
        val userPassword = binding.passwordText.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(userMail).matches()) {
            binding.emailText.error = "Invalid email provided"
        } else if (TextUtils.isEmpty(userPassword)) {
            binding.passwordText.error = "Please enter password"
        } else {
            firebaseLogin(userMail, userPassword)
        }

    }

    private fun firebaseLogin(userMail: String, userPassword: String) {
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(userMail, userPassword)
            .addOnSuccessListener {
                progressDialog.dismiss()

                val user = firebaseAuth.currentUser

                redirect(user)
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed. Reason: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun redirect(user: FirebaseUser?) {

        var redirectIntent: Intent? = null
        val lastPage = (intent.extras?.get(GlobalUtils.LAST_PAGE) as String?).apply {
            when (this) {
                "NavigationActivity" -> {
                    val currentRoute = intent.extras?.get("route") as Route?
                    redirectIntent = Intent(this@LoginActivity, NavigationActivity::class. java)
                    redirectIntent!!.putExtra("route", currentRoute)
                    redirectIntent!!.putExtra("authInfo", user)
                    startActivity(redirectIntent)
                }
                "StatisticsFragment" -> {
                    redirectIntent = Intent(this@LoginActivity, StatisticsFragment::class.java)
                    redirectIntent!!.putExtra("authInfo", user)
                    startActivity(redirectIntent)
                }
                "ProfileFragment" -> {
                    redirectIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    redirectIntent!!.putExtra(
                        GlobalUtils.LAST_PAGE,
                        HistoryFragment::class.java.simpleName
                    )
                    redirectIntent!!.putExtra("authInfo", user)
                    startActivity(redirectIntent)
                }
                "RouteFragment" -> {

                    var userSettings: UserSettings? = null

                    FirebaseDatabase.getInstance().getReference("user_settings")
                        .child("user" + user!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(snapshot: DataSnapshot) {

                                var userSettings: UserSettings? = null

                                if (snapshot.exists()) {
                                    val settingsData = snapshot.value as HashMap<String, *>

                                    userSettings = UserSettings(
                                        settingsData["distanceUnit"]!! as String,
                                        settingsData["heightUnit"]!! as String,
//                                        settingsData["timeUnit"]!! as String,
                                        settingsData["showTips"] as Boolean,
                                        settingsData["temperatureUnit"]!! as String,
                                    )
                                }

                                val currentRoute = intent.extras?.get("route") as Route?
                                val action = intent.extras?.get("action") as String?
                                redirectIntent =
                                    Intent(this@LoginActivity, RouteActivity::class.java)
                                redirectIntent!!.putExtra("userSettings", userSettings)
                                redirectIntent!!.putExtra("route", currentRoute)
                                redirectIntent!!.putExtra("action", action)
                                redirectIntent!!.putExtra("authInfo", user)
                                startActivity(redirectIntent)
                            }

                            override fun onCancelled(p0: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }
                "PhotoActivity" -> {
                    val imageName = intent.extras?.get("photo_item") as String?
                    val itemId = intent.extras?.get("itemId") as String?
                    redirectIntent = Intent(this@LoginActivity, PhotoActivity::class.java)
                    redirectIntent!!.putExtra(GlobalUtils.LAST_PAGE, "PhotoActivity")
                    redirectIntent!!.putExtra("photo_item", imageName)
                    redirectIntent!!.putExtra("itemId", itemId)
                    redirectIntent!!.putExtra("authInfo", user)
                    startActivity(redirectIntent)
                }
                else -> {
                    redirectIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    redirectIntent!!.putExtra("authInfo", user)
                    startActivity(redirectIntent)
                }
            }
        }
    }


}