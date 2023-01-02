package com.example.smarttravelguide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.smarttravelguide.data.Resource
import com.example.smarttravelguide.ui.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        val authViewModel: AuthViewModel by viewModels()

        val signinButton = findViewById<Button>(R.id.signin_button)
        val signupButton = findViewById<Button>(R.id.signup_button)

        signinButton.setOnClickListener {
            val password = findViewById<TextInputEditText>(R.id.password).text.toString()
            val email = findViewById<TextInputEditText>(R.id.email).text.toString()

            authViewModel?.loginUser(email, password)

            authViewModel.loginFlow.observe(this, Observer { loginFlow ->
                if (loginFlow is Resource.Success) {
                    findViewById<TextInputEditText>(R.id.password).setText("")
                    findViewById<TextInputEditText>(R.id.email).setText("")
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                }
                else if (loginFlow is Resource.Failure) {
                    Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show()
                }
            })
        }

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}