package com.shejan.pdfbox_pdfeditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.shejan.pdfbox_pdfeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize PDFBox
        com.shejan.pdfbox_pdfeditor.core.PdfProcessor.init(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        
        navHostFragment?.let {
            val navController = it.navController
            NavigationUI.setupWithNavController(binding.bottomNav, navController)
        }
    }
}