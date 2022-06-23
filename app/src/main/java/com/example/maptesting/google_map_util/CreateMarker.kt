package com.example.maptesting.google_map_util

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CreateMarker {

    fun addNewMarker(latLng: LatLng,

                             snippet : String) : MarkerOptions {
        return MarkerOptions()
            .position(latLng)
            .snippet(snippet)
    }

    fun setIconMarker(mOptions : MarkerOptions,
                      resourceId : Int){
        mOptions.icon(BitmapDescriptorFactory.fromResource(resourceId))
    }

    fun setTitleMarker(mOptions : MarkerOptions,
                      title : String){
        mOptions.title(title)
    }



}