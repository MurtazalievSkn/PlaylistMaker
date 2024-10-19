package com.example.playlistmaker.presentation.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var separatorLine: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        separatorLine = findViewById<View>(R.id.separator_line)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bnv_bottom_panel)
        bottomNavigationView.setupWithNavController(navController)
    }

    fun showNavBar() {
        bottomNavigationView.isVisible = true
        separatorLine.isVisible = true

    }

    fun hideNavBar() {
        bottomNavigationView.isVisible = false
        separatorLine.isVisible = false
    }
}
