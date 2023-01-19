package com.example.smarttravelguide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttravelguide.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        val authViewModel: AuthViewModel by viewModels()
        val username = findViewById<TextView>(R.id.name)
        val email = findViewById<TextView>(R.id.email)
        username.setText(authViewModel.currentUser?.email)
        email.setText(authViewModel.currentUser?.email)
    }
}