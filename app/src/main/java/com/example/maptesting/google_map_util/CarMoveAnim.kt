package com.example.maptesting.google_map_util

import android.animation.ValueAnimator
import android.location.Location
import android.view.animation.LinearInterpolator
import com.example.maptesting.google_map_util.CarMoveAnim.LatLngInterpolatorNew.LinearFixed
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


class CarMoveAnim  {
    interface LatLngInterpolatorNew {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class LinearFixed : LatLngInterpolatorNew {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun convertLatLngToLocation(latLng: LatLng): Location {
        val location = Location("someLoc")
        location.latitude = latLng.latitude
        location.longitude = latLng.longitude
        return location
    }

    fun bearingBetweenLatLngs(beginLatLng: LatLng, endLatLng: LatLng): Float {
        val beginLocation = convertLatLngToLocation(beginLatLng)
        val endLocation = convertLatLngToLocation(endLatLng)
        return beginLocation.bearingTo(endLocation)
    }


        fun startcarAnimation(
            carMarker: Marker, googleMap: GoogleMap, startPosition: LatLng,
            endPosition: LatLng, duration: Int, callback: CancelableCallback?
        ) {
            var duration = duration
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            if (duration == 0 || duration < 3000) {
                duration = 300
            }
            valueAnimator.duration = duration.toLong()
            val latLngInterpolator: LatLngInterpolatorNew = LinearFixed()
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { valueAnimator ->
                val v = valueAnimator.animatedFraction
                val lng = v * endPosition.longitude + (1 - v) * startPosition.longitude
                val lat = v * endPosition.latitude + (1 - v) * startPosition.latitude
                val newPos = latLngInterpolator.interpolate(v, startPosition, endPosition)
                carMarker.position = newPos
                carMarker.setAnchor(0.5f, 0.5f)
                val rotation = MapAnimator().getRotation(endPosition, startPosition)
                if (!rotation.isNaN()) {
                    carMarker.rotation  = rotation
                }

                println("carMarker.rotation ${bearingBetweenLatLngs(startPosition, endPosition)}")


                if (callback != null) {

                    googleMap.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(
                                CameraPosition.Builder()
                                    .target(newPos)
                                    .bearing(
                                        bearingBetweenLatLngs(startPosition, endPosition)
                                    )
                                    .zoom(40f)
                                    .build()
                            ), callback
                    )
                } else {
                    googleMap.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(
                                CameraPosition.Builder()
                                    .target(newPos)
                                    .bearing(
                                        bearingBetweenLatLngs(startPosition, endPosition)
                                    )
                                    .zoom(40f)
                                    .build()
                            )
                    )
                }
            }
            valueAnimator.start()
        }


}