package com.example.smishx

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_page)

        val getStartedButton: Button = findViewById(R.id.button_id_Login) // Replace with your button's ID
        getStartedButton.setOnClickListener {
            // Navigate to SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish() // Finish OnboardingActivity
        }
    }
}
