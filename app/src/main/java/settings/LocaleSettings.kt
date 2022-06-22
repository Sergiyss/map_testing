package settings

import android.content.Context
import java.util.*

open class LocaleSettings(c: Context) {

    private val context = c
    private val loc = context.resources.configuration.locales.get(0)

    //Auto change locale
    fun getLocale():String {
        return loc.toString()
    }

    //Selective change locale
    fun changeMapLocale(idLoc:Int):String{
        return when(idLoc){
            1 -> {
                changeAppLocale(context, "en")
                "en_US"
            }
            2 -> {
                changeAppLocale(context, "uk")
                "uk_UA"
            }
            3 -> {
                changeAppLocale(context, "ru")
                "ru_RU"
            }
            else -> {
                changeAppLocale(context, "en")
                "en_US"
            }
        }
    }

    //Does not completely replace locale
    private fun changeAppLocale(c: Context, loc: String){
        val locale = Locale(loc)
        Locale.setDefault(locale)
        val resources = c.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}