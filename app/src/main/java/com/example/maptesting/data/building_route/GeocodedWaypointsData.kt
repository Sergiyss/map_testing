package com.example.maptesting.data.building_route

data class GeocodedWaypointsData(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)