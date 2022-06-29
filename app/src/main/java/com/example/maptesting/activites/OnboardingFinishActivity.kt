package com.example.maptesting.activites

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.maptesting.R

class OnboardingFinishActivity  : AppCompatActivity() {
    private lateinit var btnStart: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_finish)
        btnStart = findViewById(R.id.btn_next_step)
        btnStart.setOnClickListener {
            finish()
        }
    }
}