package com.example.maptesting.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.*

private var toast: Toast? = null


inline fun <reified T : Activity> Activity.startActivityArg(name : String, value : String){
    startActivity(Intent(this, T::class.java).putExtra(name, value))
}

object Coroutines {
    fun io(work: suspend (() -> Unit)): Job =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }
    fun <T: Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit)): Job =
        CoroutineScope(Dispatchers.Main).launch {
            val data = CoroutineScope(Dispatchers.IO).async rt@{
                return@rt work()
            }.await()
            callback(data)
        }
}

internal fun Activity.toast(message: CharSequence) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .apply { show() }
}


fun View.slideUp(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideDown(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat())
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun hideSoftKeyBoard(context: Context, view: View) {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    } catch (e: Exception) {
        // TODO: handle exception
        e.printStackTrace()
    }
}
