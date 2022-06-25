package com.example.maptesting.retrofit

import java.io.IOException



class ApiClient {

    // Coonect terminal as per documentation
    private val service: GoogleApi = RetrofitClient.getClient("https://maps.googleapis.com/").create(GoogleApi::class.java)


    // Coonect terminal as per documentation
    internal fun createConnectionToken(): String {
        try {
            val result = service.getDirections(mapOf(1.0 to 0.0, 2.0 to 0.0)
                , mapOf(1.0 to 0.0, 2.0 to 0.0),
            false,
            "driver",
                "AIzaSyDw6XisD9272BBEFergQ8SCqxxr-XkYWLE")

            if (result.isSuccessful && result.body() != null) {
                return result.body()
            } else {
                throw ConnectionTokenException("Creating connection token failed")
            }
        } catch (e: IOException) {
            throw ConnectionTokenException("Creating connection token failed", e)
        }
    }
}