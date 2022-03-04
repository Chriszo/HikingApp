package com.example.hikingapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var optionsMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar!!)

        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        optionsMenu = binding.drawerNavMenu.menu

        setMenuItemListeners(optionsMenu)

        val navView: BottomNavigationView = binding.drawerLayout.nav_view

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
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

    private fun setMenuItemListeners(optionsMenu: Menu) {

        val accountSettingsItem = optionsMenu.findItem(R.id.accountSettingsFragment)
        val appSettingsItem = optionsMenu.findItem(R.id.appSettingsFragment)

        accountSettingsItem.setOnMenuItemClickListener {
            val intent = Intent(MainActivity@this,SettingsActivity::class.java)
            intent.putExtra("selectedSetting", "Account Setting clicked")
            startActivity(intent)
            true
        }

        appSettingsItem.setOnMenuItemClickListener {
            val intent = Intent(MainActivity@this,SettingsActivity::class.java)
            intent.putExtra("selectedSetting", "App Setting clicked")
            startActivity(intent)
            true
        }    }
}