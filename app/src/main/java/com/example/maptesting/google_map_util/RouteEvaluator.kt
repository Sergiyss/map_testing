package com.example.maptesting.google_map_util

import android.animation.TypeEvaluator
import com.google.android.gms.maps.model.LatLng

class RouteEvaluator : TypeEvaluator<LatLng> {
    override fun evaluate(fraction: Float, startValue: LatLng?, endValue: LatLng?): LatLng {
        val lat: Double = startValue!!.latitude + fraction * (endValue!!.latitude - startValue.latitude)
        val lng: Double = startValue.longitude + fraction * (endValue.longitude - startValue.longitude)
        return LatLng(lat, lng)
    }
}