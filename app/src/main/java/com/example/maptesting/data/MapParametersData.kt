package com.example.maptesting.data

import com.google.android.gms.maps.model.LatLng


//class MapData {
//        var routes = ArrayList<Routes>()
//    }
//
//    class Routes {
//        var legs = ArrayList<Legs>()
//    }
//
//    class Legs {
//        var distance = Distance()
//        var duration = Duration()
//        var end_address = ""
//        var start_address = ""
//        var end_location =Location()
//        var start_location = Location()
//        var steps = ArrayList<Steps>()
//    }
//
//    class Steps {
//        var distance = Distance()
//        var duration = Duration()
//        var end_address = ""
//        var start_address = ""
//        var end_location =Location()
//        var start_location = Location()
//        var polyline = PolyLine()
//        var travel_mode = ""
//        var maneuver = ""
//    }
//
//    class Duration {
//        var text = ""
//        var value = 0
//    }
//
//    class Distance {
//        var text = ""
//        var value = 0
//    }
//
//    class PolyLine {
//        var points = ""
//    }
//
//    class Location{
//        var lat =""
//        var lng =""
//    }

    //Data class for car markers
    data class CarMarker(var latLng : LatLng,
                         val title : String,
                         val snippet : String,
                         val icon : Int,
                         var distance: Float
    )

