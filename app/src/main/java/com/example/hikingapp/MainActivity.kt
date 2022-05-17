package com.example.hikingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.databinding.ActivityMainBinding
import com.example.hikingapp.ui.settings.*
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.AppViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.custom_toolbar.view.*
import java.util.stream.Collectors

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private val userViewModel: UserViewModel by viewModels()
    private val applicationViewModel: AppViewModel by viewModels()

    private lateinit var optionsMenu: Menu

    private var authInfo: FirebaseUser? = null

    private lateinit var db:SQLiteDatabase
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val checkedRoutePrefs: SharedPreferences by lazy {
        this.applicationContext.getSharedPreferences("checkedRoutePrefs", 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = this.openOrCreateDatabase("images.db", Context.MODE_PRIVATE,null)
        db.execSQL("create table if not exists images (name String, data blob)")

        val toolbarContainer = findViewById<View>(R.id.toolbarContainer)

        val toolbar = toolbarContainer.findViewById(R.id.toolbar) as Toolbar

        val backButton = toolbarContainer.findViewById<ImageView>(R.id.back_btn)
        backButton.visibility = View.GONE

        val title = toolbarContainer.findViewById<TextView>(R.id.action_bar_title)
        title.visibility = View.GONE

        if (intent.extras?.containsKey("authInfo") == true) {
            (intent.extras!!["authInfo"] as FirebaseUser?)?.let { user ->
                userViewModel.user.postValue(user).also {
                    authInfo = user
                }

                if (authInfo != null) {

                    toolbar.action_bar_user.visibility = View.GONE
                    toolbar.account_icon.visibility = View.VISIBLE
                } else {

                    checkedRoutePrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation,mutableSetOf()).apply()

                    toolbar.action_bar_user.visibility = View.VISIBLE
                    toolbar.account_icon.visibility = View.GONE
                }
            }
        }

        applicationViewModel.storage.postValue(storage)


        setSupportActionBar(toolbar!!)

        toolbar.action_bar_user.setOnClickListener {

            val actionBarTextView = it as TextView
            if (actionBarTextView.text.toString() == "Login") {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        toolbar.account_icon.setOnClickListener {
            val accountIntent = Intent(this, AccountActivity::class.java)
            accountIntent.putExtra("authInfo", authInfo)
            startActivity(accountIntent)
        }


        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        optionsMenu = binding.drawerNavMenu.menu

        setMenuItemListeners(optionsMenu)

        val navView: BottomNavigationView = binding.drawerLayout.nav_view

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_discover, R.id.navigation_navigation, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun setRoutesSelectedForNavigation() {
        FirebaseDatabase.getInstance().getReference("selected_routes_nav").child(authInfo!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val routeIdsList = snapshot.value as MutableList<String>
                    val routeIdsSet  = routeIdsList.stream().map { it.toString() }.collect(Collectors.toSet())

                    checkedRoutePrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation,routeIdsSet).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setMenuItemListeners(optionsMenu: Menu) {

        val accountSettingsItem = optionsMenu.findItem(R.id.accountSettingsFragment)
        val logoutItem = optionsMenu.findItem(R.id.account_logout)
        val editItem = optionsMenu.findItem(R.id.account_edit)
        val deleteIem = optionsMenu.findItem(R.id.account_delete)
        val appSettingsItem = optionsMenu.findItem(R.id.appSettingsFragment)
        val settingsItem = optionsMenu.findItem(R.id.app_settings)
        val contactSettingsItem = optionsMenu.findItem(R.id.contact_settings)
        val loginItem = optionsMenu.findItem(R.id.loginActivity)

        setMenuItemsVisibility(loginItem, accountSettingsItem, appSettingsItem)

        logoutItem.setOnMenuItemClickListener {
            if (authInfo != null) {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
            }
            true
        }

        editItem.setOnMenuItemClickListener {
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            intent.putExtra("selectedSetting", "Account Setting clicked")
            startActivity(intent)
            true
        }

        deleteIem.setOnMenuItemClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            intent.putExtra("selectedSetting", "Account Setting clicked")
            startActivity(intent)
            true
        }

        settingsItem.setOnMenuItemClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            intent.putExtra("selectedSetting", "App Setting clicked")
            startActivity(intent)
            true
        }


        contactSettingsItem.setOnMenuItemClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            intent.putExtra("authInfo", authInfo)
            intent.putExtra("selectedSetting", "App Setting clicked")
            startActivity(intent)
            true
        }

        loginItem.setOnMenuItemClickListener {
            if (authInfo == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            true
        }
    }

    private fun setMenuItemsVisibility(
        loginItem: MenuItem?,
        accountSettingsItem: MenuItem,
        appSettingsItem: MenuItem
    ) {

        if (authInfo != null) {
            loginItem?.isVisible = false
            accountSettingsItem.isVisible = true
            appSettingsItem.isVisible = true
        } else {
            loginItem?.isVisible = true
            accountSettingsItem.isVisible = false
            appSettingsItem.isVisible = false
        }
    }


}