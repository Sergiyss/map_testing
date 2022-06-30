package com.example.maptesting

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.example.maptesting.room_database.DBViewModel
import settings.PreferencesManager

class App: Application() {

    init {
        init = this
    }
    companion object {
        private var init: App? = null
        fun applicationContext(): Context{
            return init!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        val main = Intent(applicationContext(), MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val hint = Intent(applicationContext(), StartingWindowsActivity::class.java)
        hint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val load = PreferencesManager.getInstance(applicationContext())!!
        print("is viewed ${load.isViewed}")
        if (load.isViewed)
            startActivity(main)
        else startActivity(hint)
    }

}