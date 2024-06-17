package com.example.smishx

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_page) // Use your onboarding layout

        val finishOnboardingButton: Button = findViewById(R.id.button_id_Login) // Replace with your button's ID
        finishOnboardingButton.setOnClickListener {
            // Mark onboarding as complete
            val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("onboarding_complete", true).apply()

            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finish OnboardingActivity
        }
    }
}