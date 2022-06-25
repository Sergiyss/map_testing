package com.example.maptesting.google_map_util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


const val TASK_AWAIT = 120L

//Рисую радиус круга
//    private fun circleMap(){
//        mMap.addCircle(
//            CircleOptions()
//                .center(LatLng(DEMO_LATITUDE, DEMO_LONGITUDE))
//                .radius(500.0)
//                .fillColor(Color.parseColor("#9cc0f94d"))
//                .strokeWidth(0f)
//                .zIndex(0.2f)
//        )
//
//
//
//
//    }


    /*
    * Управление камерой
    **/
//    fun newCamera(){
//        val cameraPosition = CameraPosition.Builder()
//            .target(LatLng(0.0, 0.0))
//            .bearing(45f)
//            .tilt(90f)
//            .zoom(mMap.getCameraPosition().zoom)
//            .build()
//    }


    //Находит ближайщие достопремичательности

//    var likelyPlaceNames : Array<String?> = arrayOf()
//    var likelyPlaceAddresses : Array<String?> = arrayOf()
//    var likelyPlaceAttributions : Array<List<*>?> = arrayOf()
//    var likelyPlaceLatLngs : Array<LatLng?> = arrayOf()
//
//    @SuppressLint("MissingPermission")
//    private fun showCurrentPlace() {
//        val M_MAX_ENTRIES = 1
//
//        if (mMap == null) {
//            return
//        }
//        if (locationPermissionGranted) {
//
//            // Construct a PlacesClient
//            Places.initialize(applicationContext, getString(R.string.google_key))
//            val placesClient = Places.createClient(this)
//
//            // Construct a FusedLocationProviderClient.
//            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//
//            // Use fields to define the data types to return.
//            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
//
//            // Use the builder to create a FindCurrentPlaceRequest.
//            val request = FindCurrentPlaceRequest.newInstance(placeFields)
//
//            // Get the likely places - that is, the businesses and other points of interest that
//            // are the best match for the device's current location.
//            val placeResult = placesClient.findCurrentPlace(request)
//            placeResult.addOnCompleteListener { task ->
//                if (task.isSuccessful && task.result != null) {
//                    val likelyPlaces = task.result
//
//                    // Set the count, handling cases where less than 5 entries are returned.
//                    val count = if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
//                        likelyPlaces.placeLikelihoods.size
//                    } else {
//                        M_MAX_ENTRIES
//                    }
//                    var i = 0
//                    likelyPlaceNames = arrayOfNulls(count)
//                    likelyPlaceAddresses = arrayOfNulls(count)
//                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
//                    likelyPlaceLatLngs = arrayOfNulls(count)
//                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
//                        // Build a list of likely places to show the user.
//                        likelyPlaceNames[i] = placeLikelihood.place.name
//                        likelyPlaceAddresses[i] = placeLikelihood.place.address
//                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
//                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
//                        i++
//                        if (i > count - 1) {
//                            break
//                        }
//                    }
//
//                    // Show a dialog offering the user the list of likely places, and add a
//                    // marker at the selected place.
//                    openPlacesDialog()
//                } else {
//                    Log.e(TAG, "Exception: %s", task.exception)
//                }
//            }
//        }
//    }
//
//
//
//    private fun openPlacesDialog() {
//        // Ask the user to choose the place where they are now.
//        val listener = DialogInterface.OnClickListener { dialog, which -> // The "which" argument contains the position of the selected item.
//            val markerLatLng = likelyPlaceLatLngs[which]
//            var markerSnippet = likelyPlaceAddresses[which]
//            if (likelyPlaceAttributions[which] != null) {
//                markerSnippet = """
//                $markerSnippet
//                ${likelyPlaceAttributions[which]}
//                """.trimIndent()
//            }
//
//            if (markerLatLng == null) {
//                return@OnClickListener
//            }
//
//            // Add a marker for the selected place, with an info window
//            // showing information about that place.
//            mMap?.addMarker(
//                MarkerOptions()
//                .title(likelyPlaceNames[which])
//                .position(markerLatLng)
//                .snippet(markerSnippet))
//
//            // Position the map's camera at the location of the marker.
//            mMap?.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(markerLatLng,
//                MapsActivity.DEFAULT_ZOOM.toFloat()))
//        }
//
//        // Display the dialog.
//        AlertDialog.Builder(this)
//            .setTitle("title")
//            .setItems(likelyPlaceNames, listener)
//            .show()
//    }



fun getAutocomplete(mPlacesClient: PlacesClient, constraint: CharSequence): List<AutocompletePrediction> {
    //48.4624412,34.8602745 //Dnipro

    //https://stackoverflow.com/questions/61617070/android-location-bias-in-autocomplete-rectangular-bounds
    //Как задать радицс ^

    val bounds = RectangularBounds.newInstance(
        LatLng(-33.880490, 151.184363),
        LatLng(-33.858754, 151.229596)
    )


    var list = listOf<AutocompletePrediction>()
    val token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder()
        .setTypeFilter(TypeFilter.ADDRESS)
        .setCountries("UA")
        .setSessionToken(token)
        .setQuery(constraint.toString())
        .build()
    val prediction = mPlacesClient.findAutocompletePredictions(request)
    try {
        Tasks.await(prediction, TASK_AWAIT, TimeUnit.SECONDS)
    } catch (e: ExecutionException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } catch (e: TimeoutException) {
        e.printStackTrace()
    }

    if (prediction.isSuccessful) {
        val findAutocompletePredictionsResponse = prediction.result
        findAutocompletePredictionsResponse?.let {
            list = findAutocompletePredictionsResponse.autocompletePredictions
        }
        return list
    }
    return list
}

//Получить координаты по адресу
//Получить широту и долготу адреса
//https://stackoverflow.com/questions/3574644/how-can-i-find-the-latitude-and-longitude-from-address
fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
    val coder = Geocoder(context)
    val address: List<Address>?
    var p1: LatLng? = null
    try {
        // May throw an IOException
        address = coder.getFromLocationName(strAddress, 5)
        if (address == null) {
            return null
        }
        val location: Address = address[0]
        p1 = LatLng(location.getLatitude(), location.getLongitude())
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return p1
}