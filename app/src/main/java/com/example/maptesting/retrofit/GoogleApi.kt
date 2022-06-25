package com.example.maptesting.retrofit

import com.example.maptesting.data.MapData
import retrofit2.Call
import retrofit2.http.*


interface GoogleApi {

    //Получение маршрута
    @Headers("Accept: application/json")
    @POST("maps/api/directions")
    fun getDirections(
        @Query("origin")  startLatitude : Map<Double, Double>,
        @Part("destination") endLatitude : Map<Double, Double>,
        @Part("sensor") sensor : Boolean,
        @Part("mode") mode : String,
        @Part("mode") key : String,
    ): Call<MapData>
}