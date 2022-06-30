package settings

import android.content.Context

class PreferencesManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("load", Context.MODE_PRIVATE)

    companion object {
        const val APP_DOWNLOAD_SETTINGS = "load"
        private var INSTANCE: PreferencesManager? = null
        fun getInstance(context: Context): PreferencesManager? {
            if (INSTANCE == null)
                synchronized(PreferencesManager::class.java) {
                    if (INSTANCE == null)
                        INSTANCE = PreferencesManager(context)
                }
            return INSTANCE
        }
    }

    fun saveBrowsing(token: Boolean) {
        sharedPreferences.edit().putBoolean(APP_DOWNLOAD_SETTINGS, token).apply()
    }

    val isViewed: Boolean
        get() = sharedPreferences.getBoolean(APP_DOWNLOAD_SETTINGS, false)
}