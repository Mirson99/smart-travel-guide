package com.example.smarttravelguide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttravelguide.ui.auth.AuthViewModel

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authViewModel: AuthViewModel by viewModels()

        val logoutButton = findViewById<Button>(R.id.logout_button)

        logoutButton.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }
}