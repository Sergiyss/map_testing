package com.example.maptesting.retrofit

import android.provider.Settings.System.getString
import com.example.maptesting.R
import com.example.maptesting.data.building_route.Bounds
import com.example.maptesting.data.building_route.GeocodedWaypointsData
import com.example.maptesting.data.building_route.Route
import com.google.android.gms.maps.model.LatLng
import retrofit2.Response
import java.io.IOException



class ApiClient {

    // Coonect terminal as per documentation
    private val service: GoogleApi = RetrofitClient.getClient("https://maps.googleapis.com/").create(GoogleApi::class.java)



    private var arrNull : List<Route>? = null

    internal fun getCaptureError(startLanLon : LatLng, endLatLng: LatLng, mKey : String) :
            List<Route> {


        val result = service.getDirections(
            "${startLanLon.latitude}, ${startLanLon.longitude}",
            "${endLatLng.latitude}, ${endLatLng.longitude}",
            false,
            "driver",
            mKey).execute()

            if (result.code() == 200 && result.isSuccessful && result.body() != null){
                return result.body()!!.routes
            }else{
                return arrNull!!
            }


    }



}