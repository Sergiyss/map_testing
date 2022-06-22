package com.example.maptesting.network

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.widget.Toast
import com.example.maptesting.R
import com.example.maptesting.data.MapData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class Parser {

    fun getDirectionUrl(original : LatLng, dest : LatLng ) : String?{

        println("------------------- getDirectionUrl")

        val str_original = "original "+ original.latitude + " , "+ original.longitude
        val str_dest = "destination "+ dest.latitude +" , "+ dest.longitude

        val mode = "mode=drver"

        val parameters = "$str_original&$str_dest&$mode"

        val output = "json"

        val a = "https://maps.googleapis.com/maps/api/directions/json?origin=${original.latitude},${original.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=AIzaSyDw6XisD9272BBEFergQ8SCqxxr-XkYWLE"

        println(a)


        return "https://maps.googleapis.com/maps/api/directions/json?origin=${original.latitude},${original.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=AIzaSyDw6XisD9272BBEFergQ8SCqxxr-XkYWLE"


    }


    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }



    fun doInBackground(url : String): List<List<LatLng>> {

        println("------------------- doInBackground")

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
        return result
    }

     fun onPostExecute(result: List<List<LatLng>>) : PolylineOptions {
         val lineoption = PolylineOptions()
         for (i in result.indices) {
             lineoption.addAll(result[i])
             lineoption.width(10f)
             lineoption.color(Color.GREEN)
             lineoption.geodesic(true)
         }
         return lineoption
     }
}
