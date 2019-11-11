package com.wifiheatmap.wifiheatmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navController : NavController = Navigation.findNavController(this, R.id.mainNavHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController)
    }
}
