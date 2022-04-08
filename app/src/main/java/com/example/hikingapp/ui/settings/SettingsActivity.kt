package com.example.hikingapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.BackButtonListener
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.MainActivity
import com.example.hikingapp.databinding.ActivitySettingsBinding
import com.example.hikingapp.domain.users.settings.UserSettings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


private lateinit var binding: ActivitySettingsBinding
private var authInfo: FirebaseUser? = null

class SettingsActivity : AppCompatActivity(), BackButtonListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setBackButtonListener()

        if (intent.extras?.containsKey("authInfo") == true && intent.extras!!["authInfo"] != null) {

            setContentView(binding.root)

            authInfo = intent.extras!!["authInfo"] as FirebaseUser?

            FirebaseDatabase.getInstance().getReference("user_settings")
                .child("user${authInfo!!.uid}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var userSettings: UserSettings? = null
                    if (snapshot.exists()) {
                        val settingsData = snapshot.value as HashMap<String, *>

                        userSettings = UserSettings(
                            settingsData["distanceUnit"]!! as String,
                            settingsData["heightUnit"]!! as String,
                            settingsData["timeUnit"]!! as String,
                            settingsData["showTips"] as Boolean
                        )

                        binding.distanceValue.text = userSettings.distanceUnit
                        binding.heightValue.text = userSettings.heightUnit
                        binding.timeValue.text = userSettings.timeUnit
                        binding.tipsSwitch.isChecked = userSettings.showTips

                    } else {
                        binding.distanceValue.text = "m"
                        binding.heightValue.text = "m"
                        binding.timeValue.text = "min"
                        binding.tipsSwitch.isChecked = false
                    }
                    configureSliders(userSettings = userSettings)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


            configureButtons()

            val selectedSetting = intent.extras?.get("selectedSetting") as String

            val title = binding.toolbarContainer.actionBarTitle as TextView
            title.text = "Settings"


        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }


    private fun configureButtons() {
        binding.submitButton.setOnClickListener {
            FirebaseDatabase.getInstance()
                .getReference("user_settings")
                .child("user${authInfo!!.uid}")
                .setValue(
                    UserSettings(
                        binding.distanceValue.text.toString(),
                        binding.heightValue.text.toString(),
                        binding.timeValue.text.toString(),
                        binding.tipsSwitch.isChecked
                    )
                )

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            if (authInfo != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("authInfo", authInfo)
                startActivity(intent)
            }
        }
    }

    private fun configureSliders(userSettings: UserSettings?) {
        val distanceSlider = binding.distanceSlider

        when (userSettings?.distanceUnit) {
            "cm" -> distanceSlider.value = 0f
            "m" -> distanceSlider.value = 5f
            "km" -> distanceSlider.value = 10f
        }

        distanceSlider.setLabelFormatter {
            when (it) {
                0f -> return@setLabelFormatter "cm"
                5f -> return@setLabelFormatter "m"
                10f -> return@setLabelFormatter "km"
                else -> {
                    return@setLabelFormatter ""
                }
            }
        }

        distanceSlider.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> binding.distanceValue.text = "cm"
                5f -> binding.distanceValue.text = "m"
                10f -> binding.distanceValue.text = "km"
            }
        }

        val heightSlider = binding.heightSlider

        when (userSettings?.heightUnit) {
            "cm" -> heightSlider.value = 0f
            "m" -> heightSlider.value = 5f
            "km" -> heightSlider.value = 10f
        }

        heightSlider.setLabelFormatter {
            when (it) {
                0f -> return@setLabelFormatter "cm"
                5f -> return@setLabelFormatter "m"
                10f -> return@setLabelFormatter "km"
                else -> {
                    return@setLabelFormatter ""
                }
            }
        }

        heightSlider.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> binding.heightValue.text = "cm"
                5f -> binding.heightValue.text = "m"
                10f -> binding.heightValue.text = "km"
            }
        }

        val timeSlider = binding.timeSlider

        when (userSettings?.timeUnit) {
            "sec" -> timeSlider.value = 0f
            "min" -> timeSlider.value = 5f
            "hrs" -> timeSlider.value = 10f
        }


        timeSlider.setLabelFormatter {
            when (it) {
                0f -> return@setLabelFormatter "sec"
                5f -> return@setLabelFormatter "min"
                10f -> return@setLabelFormatter "hrs"
                else -> {
                    return@setLabelFormatter ""
                }
            }
        }

        timeSlider.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> binding.timeValue.text = "sec"
                5f -> binding.timeValue.text = "min"
                10f -> binding.timeValue.text = "hrs"
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