package com.example.smarttravelguide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.smarttravelguide.data.Resource
import com.example.smarttravelguide.ui.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        val authViewModel: AuthViewModel by viewModels()

        val signupButton = findViewById<Button>(R.id.signup_button)
        val signinButton = findViewById<Button>(R.id.signin_button)

        signupButton.setOnClickListener {
            val password = findViewById<TextInputEditText>(R.id.passET).text.toString()
            val email = findViewById<TextInputEditText>(R.id.emailEt).text.toString()
            val name = findViewById<TextInputEditText>(R.id.name).text.toString()

            authViewModel?.signupUser(name, email, password)

            authViewModel.signupFlow.observe(this, Observer { signupFlow ->
                if (signupFlow is Resource.Success) {
                    findViewById<TextInputEditText>(R.id.passET).setText("")
                    findViewById<TextInputEditText>(R.id.emailEt).setText("")
                    findViewById<TextInputEditText>(R.id.name).setText("")

                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                }
                else if (signupFlow is Resource.Failure) {
                    Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }

        signinButton.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

    }
}