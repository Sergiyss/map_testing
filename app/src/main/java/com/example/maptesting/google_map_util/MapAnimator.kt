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

    /**
     * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
     */
    fun setRouteIncreaseForward(endLatLng: LatLng) {
        val foregroundPoints = foregroundPolyline!!.points
        foregroundPoints.add(endLatLng)
        foregroundPolyline!!.points = foregroundPoints
    }



    fun animateMarker(mMap : GoogleMap,
                      position : Int,
                      allPaths : List<LatLng>,
                      durationMs : Int,
                      marker : Marker) : GoogleMap.CancelableCallback{

        var currentPt = position

        val simpleAnimationCancelableCallback: GoogleMap.CancelableCallback = object :
            GoogleMap.CancelableCallback {
            override fun onCancel() {}
            override fun onFinish() {
                if (++currentPt < allPaths.size) {

                    val cameraPosition = CameraPosition.Builder()
                        .target(allPaths.get(currentPt))
                        .tilt(if (currentPt < allPaths.size - 1) 90f else 0f) //.bearing((float)heading)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build()
                    mMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(cameraPosition),
                        durationMs,
                        this
                    )

                    marker.position = LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)
                }
            }
        }

        return simpleAnimationCancelableCallback
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