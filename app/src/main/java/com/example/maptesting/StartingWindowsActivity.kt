package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.maptesting.activites.OnboardingFinishActivity
import com.example.maptesting.adapters.StartingWindowAdapter
import com.example.maptesting.utils.AnimatooStartingWindow
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import settings.PreferencesManager


class StartingWindowsActivity : AppCompatActivity() {

    private lateinit var mViewPager: ViewPager2
    private lateinit var textSkip: TextView
    private lateinit var tableLayout : TabLayout

    //для сохранения настроек показа этого окна
    private lateinit var loadSetting: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_window)

        //инициализация настроек
        loadSetting = PreferencesManager.getInstance(applicationContext)!!

        mViewPager = findViewById(R.id.viewPager)
        tableLayout = findViewById(R.id.pageIndicator)
        mViewPager.adapter = StartingWindowAdapter(this@StartingWindowsActivity, baseContext)
        TabLayoutMediator(tableLayout, mViewPager) { _, _ -> }.attach()
        textSkip = findViewById(R.id.text_skip)
        textSkip.setOnClickListener {
            finish()
            setLoadSetting()
            val intent =
                Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            AnimatooStartingWindow.animateSlideLeft(this)
        }

        val btnNextStep: Button = findViewById(R.id.btn_next_step)

        btnNextStep.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                finish()
                setLoadSetting()
                val intent =
                    Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                AnimatooStartingWindow.animateSlideLeft(this)
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }

    }

    private fun getItem(): Int {
        return mViewPager.currentItem
    }

    //сохранить то, что пользователь просмотрел это окно
    private fun setLoadSetting(){
        loadSetting.saveBrowsing(true)
    }

}
