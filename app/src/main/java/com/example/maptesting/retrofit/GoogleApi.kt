package com.example.maptesting.retrofit

import com.example.maptesting.data.MapData
import com.example.maptesting.data.building_route.GeocodedWaypointsData
import retrofit2.Call
import retrofit2.http.*


interface GoogleApi {

    //Получение маршрута
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin")  startLatitude :String,
        @Query("destination") endLatitude : String,
        @Query("sensor") sensor : Boolean,
        @Query("mode") mode : String,
        @Query("key") key : String,
    ): Call<GeocodedWaypointsData>
}