package com.example.maptesting.google_map_util

import android.animation.*
import com.google.android.gms.maps.GoogleMap
import com.example.maptesting.google_map_util.MapAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.animation.AccelerateInterpolator
import android.graphics.Color
import com.example.maptesting.google_map_util.RouteEvaluator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*

/**
 * Created by amal.chandran on 22/12/16.
 */
class MapAnimator {
    private var backgroundPolyline: Polyline? = null
    private var foregroundPolyline: Polyline? = null
    private var optionsForeground: PolylineOptions? = null
    private var firstRunAnimSet: AnimatorSet? = null
    private var secondLoopRunAnimSet: AnimatorSet? = null


    fun animateRoute(googleMap: GoogleMap, bangaloreRoute: List<LatLng?>) {
        firstRunAnimSet = if (firstRunAnimSet == null) {
            AnimatorSet()
        } else {
            firstRunAnimSet!!.removeAllListeners()
            firstRunAnimSet!!.end()
            firstRunAnimSet!!.cancel()
            AnimatorSet()
        }
        secondLoopRunAnimSet = if (secondLoopRunAnimSet == null) {
            AnimatorSet()
        } else {
            secondLoopRunAnimSet!!.removeAllListeners()
            secondLoopRunAnimSet!!.end()
            secondLoopRunAnimSet!!.cancel()
            AnimatorSet()
        }
        //Reset the polylines
        if (foregroundPolyline != null) foregroundPolyline!!.remove()
        if (backgroundPolyline != null) backgroundPolyline!!.remove()
        val optionsBackground = PolylineOptions().add(bangaloreRoute[0]).color(GREY).width(5f)
        backgroundPolyline = googleMap.addPolyline(optionsBackground)
        optionsForeground = PolylineOptions().add(bangaloreRoute[0]).color(Color.BLACK).width(5f)
        foregroundPolyline = googleMap.addPolyline(optionsForeground!!)
        val percentageCompletion = ValueAnimator.ofInt(0, 100)
        percentageCompletion.duration = 2000
        percentageCompletion.interpolator = DecelerateInterpolator()
        percentageCompletion.addUpdateListener { animation ->
            val foregroundPoints = backgroundPolyline!!.points
            val percentageValue = animation.animatedValue as Int
            val pointcount = foregroundPoints.size
            val countTobeRemoved = (pointcount * (percentageValue / 100.0f)).toInt()
            val subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved)
            subListTobeRemoved.clear()
            foregroundPolyline!!.points = foregroundPoints
        }
        percentageCompletion.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                foregroundPolyline!!.color = GREY
                foregroundPolyline!!.points = backgroundPolyline!!.points
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), GREY, Color.BLACK)
        colorAnimation.interpolator = AccelerateInterpolator()
        colorAnimation.duration = 1200 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            foregroundPolyline!!.color = animator.animatedValue as Int
        }
        val foregroundRouteAnimator = ObjectAnimator.ofObject(
            this,
            "routeIncreaseForward",
            RouteEvaluator(),
            *bangaloreRoute.toTypedArray()
        )
        foregroundRouteAnimator.interpolator = AccelerateDecelerateInterpolator()
        foregroundRouteAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                backgroundPolyline!!.points = foregroundPolyline!!.points
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        foregroundRouteAnimator.duration = 1600
        //        foregroundRouteAnimator.start();
        firstRunAnimSet!!.playSequentially(
            foregroundRouteAnimator,
            percentageCompletion
        )
        firstRunAnimSet!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                secondLoopRunAnimSet!!.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        secondLoopRunAnimSet!!.playSequentially(
            colorAnimation,
            percentageCompletion
        )
        secondLoopRunAnimSet!!.startDelay = 200
        secondLoopRunAnimSet!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                secondLoopRunAnimSet!!.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        firstRunAnimSet!!.start()
    }




    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = Math.abs(start.latitude - end.latitude)
        val lngDifference: Double = Math.abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                println("1")
                rotation = Math.toDegrees(Math.atan(lngDifference / latDifference)).toFloat() -80
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                println("2")
                rotation = (90 - Math.toDegrees(Math.atan(lngDifference / latDifference)) + 90).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                println("3")
                rotation = (Math.toDegrees(Math.atan(lngDifference / latDifference)) + 180).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                println("4")
                rotation =
                    (90 - Math.toDegrees(Math.atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }




    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }



    companion object {
        private var mapAnimator: MapAnimator? = null
        val GREY = Color.parseColor("#FFA7A6A6")
        val instance: MapAnimator?
            get() {
                if (mapAnimator == null) mapAnimator = MapAnimator()
                return mapAnimator
            }
    }
}