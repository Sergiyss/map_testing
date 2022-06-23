package com.example.maptesting.adapters

import android.R
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.maptesting.data.CarMarker
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


class MarkerAdapter(inflater: LayoutInflater): InfoWindowAdapter {
    private var inflater: LayoutInflater? = null

    init {
        this.inflater = inflater
    }

    override fun getInfoContents(p0: Marker): View? {
        /*
        *
        * TODO: Обработать передачу данных
        *
        val view = inflater.inflate(R.layout.activity_maps, null)

        val carMarker = CarMarker()
        carMarker.title = ""
        carMarker.latLng = LatLng(0.0, 0.0)
        carMarker.snippet = ""
        carMarker.icon = 0
        */
        return null
    }

    override fun getInfoWindow(p0: Marker): View? = null

}
