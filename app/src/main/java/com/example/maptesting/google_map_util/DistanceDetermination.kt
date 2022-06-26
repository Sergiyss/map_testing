package com.example.maptesting.google_map_util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class DistanceDetermination {

    fun getDistanceInKilometers(startlatLong: LatLng, endlatLong: LatLng) : Float{
        val results = FloatArray(10)

        Location.distanceBetween(
            startlatLong.latitude,
            startlatLong.longitude,
            endlatLong.latitude,
            endlatLong.longitude,
            results
        )

        return results[0]
    }

}