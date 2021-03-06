package com.mindorks.example.ubercaranimation.util

import android.content.Context
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.example.maptesting.R
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.math.abs
import kotlin.math.atan


object MapUtils {

    fun getCarBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }

    fun getOriginDestinationMarkerBitmap(): Bitmap {
        val height = 20
        val width = 20
        val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun getRotation(start: LatLng, end: LatLng): Float {
        println("getRotation" + start + " , "+ end)
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }



    // ?????????????????? ???????????????? ??????????????
    fun getAddress(ctx : Context, lat : Double, lon : Double) : String?{
        val local = Locale("uk_UA") // ???????????? ?????????????????? ?????????????? https://stackoverflow.com/a/7989085/5722608
        val geoCoder = Geocoder(ctx, local)
        val addressess = geoCoder.getFromLocation(lat, lon, 1)
        //https://developer.android.com/reference/android/location/Address.html
        return "${addressess[0].getThoroughfare()}, ${addressess.get(0).getFeatureName()}"
    }

    // ?????????????????? ???????????????? ??????????????
    fun getRegion(ctx : Context, lat : Double, lng : Double) : String?{
        val local = Locale("uk_UA") // ???????????? ?????????????????? ?????????????? https://stackoverflow.com/a/7989085/5722608
        val geoCoder = Geocoder(ctx, local)
        val region = geoCoder.getFromLocation(lat, lng, 1)
//        println( region.get(0).describeContents())
//        println( region.get(0).	getAdminArea())
//        println( region.get(0).getCountryCode())
//        println( region.get(0).getCountryName())
//        println( region.get(0).getExtras())
//        println( region.get(0).getFeatureName())
//        println( region.get(0).getLatitude())
//        println( region.get(0).getLocale())
        println( region.get(0).getLocality())
//        println( region.get(0).getLongitude())
//        println( region.get(0).getMaxAddressLineIndex())
//        println( region.get(0).getPhone())
//        println( region.get(0).getPostalCode())
//        println( region.get(0).getPremises())
//        println( region.get(0).getSubAdminArea())
//        println( region.get(0).getSubLocality())
//        println( region.get(0).getSubThoroughfare())
//        println( region.get(0).getThoroughfare())
//        println( region.get(0).getUrl())




        //https://developer.android.com/reference/android/location/Address.html
        return region.get(0).subLocality
    }

    //?????????????????????? ?????????????????????? ???? ??????????????
    fun getDistanceMeters(startlatLong: LatLng, endlatLong: LatLng) : Float{
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

    //?????????????????????? ?????????????????? ??????????????
    fun getNearestMarker(distanceArrFloat : ArrayList<Float>) : Int{
        var index = 0
        val sortDistanse : ArrayList<Float> = arrayListOf()
        sortDistanse.addAll(distanceArrFloat)
        sortDistanse.sort()

        println("sortdistance "+ sortDistanse)
        println("distanceArrFloat "+ distanceArrFloat)

        for(i in 0 until distanceArrFloat.size){
            if(sortDistanse.get(0) == distanceArrFloat.get(i)){
                index = i
            }
        }

        return index
    }


    //??????
    fun getLocation(x0: Double, y0: Double, radius: Int) : LatLng {
        val random = Random()

        // Convert radius from meters to degrees
        val radiusInDegrees = (radius / 111000f).toDouble()
        val u = random.nextDouble()
        val v = random.nextDouble()
        val w = radiusInDegrees * Math.sqrt(u)
        val t = 2 * Math.PI * v
        val x = w * Math.cos(t)
        val y = w * Math.sin(t)

        // Adjust the x-coordinate for the shrinking of the east-west distances
        val new_x = x / Math.cos(Math.toRadians(y0))
        val foundLongitude = new_x + x0
        val foundLatitude = y + y0
        return  LatLng(foundLongitude , foundLatitude)
    }


}