package com.example.maptesting

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maptesting.databinding.ActivityMapsBinding
import com.example.maptesting.network.Parser
import com.example.maptesting.utils.Coroutines
import com.example.maptesting.utils.PermissionUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.widget.Autocomplete
import settings.LocaleSettings
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnCameraMoveCanceledListener,
    LocationSource.OnLocationChangedListener {

    val DEFAULT_ZOOM = 15

    val locationPermissionGranted = true


    private lateinit var original: MarkerOptions

    private val TAG = MapsActivity::class.java.name

    private  val LOCATION_PERMISSION_REQUEST_CODE = 999


    private var currentLatLng: LatLng? = null

    private  var DEMO_LATITUDE = 48.430644
    private  var DEMO_LONGITUDE = 151.211
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private var currentLocation : Location? = null
    private var currentMarker : Marker? = null

    private lateinit var loc: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Получить местоположение клиента
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //onLaunchMapPickerClicked(context = this)

        //Get locale
        loc = LocaleSettings(this).getLocale()
    }


    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                       val lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            DEMO_LATITUDE = lastKnownLocation.latitude
                            DEMO_LONGITUDE = lastKnownLocation.longitude
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation.latitude,
                                    lastKnownLocation.longitude), DEFAULT_ZOOM.toFloat()))

                            circleMap()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom( LatLng(48.428694, 35.018050), DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }







    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    /*
    * Когда карта готова вызвается этот  метод
    * */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)


        val latLong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarket(latLong)

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(p0: Marker) {

            }

            //Перерисовать маркер
            override fun onMarkerDragEnd(p0: Marker) {
                if (currentMarker != null){
                   currentMarker?.remove()
                }
                val newLating = LatLng(p0.position.latitude, p0.position.longitude)
                drawMarket(newLating)
            }

            override fun onMarkerDragStart(p0: Marker) {

            }


        })

        //Нажатие на текущий маркер
        mMap.setOnMarkerClickListener(object : OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {

                println("marker p0" + p0.title)
                println("position "+ p0.position.latitude + " "+p0.position.longitude)
                p0.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker))
                return true
            }
        })
        

        getDeviceLocation()
        addNewMarker()
    }

    private fun gettingOriginalCoordinate(){
        original = MarkerOptions().position(LatLng(DEMO_LATITUDE, DEMO_LONGITUDE))
    }


//    private fun gettingEndCoordinate() : MarkerOptions {
//        return MarkerOptions().position(LatLng(end_latitude, end_longitude))
//    }

    private fun setDistance(latLong: LatLng) : String{
        val results = FloatArray(10)

        Location.distanceBetween(
            DEMO_LATITUDE,
            DEMO_LONGITUDE,
            latLong.latitude,
            latLong.longitude,
            results
        )

        return "Distance = ${String.format("%.1f", results[0] / 1000)} km"
    }


    private fun drawMarket(latLong : LatLng){
        val markerOption = MarkerOptions().position(latLong).title("I am heare")
            .snippet(getAddress(DEMO_LATITUDE, DEMO_LONGITUDE)).draggable(true) //Чтобы маркер двигался
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker))

        println("---------- ${markerOption.title}")
        println("---------- ${markerOption.snippet}")

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 75f))
        currentMarker = mMap.addMarker(markerOption)

        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)


        currentMarker?.showInfoWindow()

        getLocation()


    }


    //Рисую радиус круга
    private fun circleMap(){
       mMap.addCircle(
            CircleOptions()
                .center(LatLng(DEMO_LATITUDE, DEMO_LONGITUDE))
                .radius(500.0)
                .fillColor(Color.parseColor("#9cc0f94d"))
                .strokeWidth(0f)
                .zIndex(0.2f)
        )


    }

    private fun getAddress(lat : Double, lon : Double) : String?{
        val local = Locale(loc) // Список доступных городов https://stackoverflow.com/a/7989085/5722608
        val geoCoder = Geocoder(this, local)
        val addressess = geoCoder.getFromLocation(lat, lon, 1)
        return addressess[0].getAddressLine(0)
    }

    private fun fetchLocation(){
        println("init currentLocation")
        try {

            val task = fusedLocationProviderClient?.lastLocation
            task?.addOnSuccessListener { location ->
                if (location != null){
                    this.currentLocation = location
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)

                    getLocation()
                }
            }

        }catch (e: SecurityException){
            println(TAG + "${e.message}")
        }

    }




    //Определение ближайшего объекста
    private fun getLocation(){


        val sydney = LatLng(48.428694, 35.018050)
        val sydney2 = LatLng(48.4329635, 35.0193128)
        val sydney3 = LatLng( 48.446958, 35.000468)

        val sydney4 = LatLng( 48.4329149,35.0214726)
        val sydney5 = LatLng(48.4326938,35.0257699)
        val sydney6 = LatLng(48.4329985,35.0043483)

        val target1 = Location("target")
        target1.setLatitude(sydney.latitude)
        target1.setLongitude(sydney.longitude)

        val target2 = Location("target")
        target2.setLatitude(sydney2.latitude)
        target2.setLongitude(sydney2.longitude)

        val target3 = Location("target")
        target3.setLatitude(sydney3.latitude)
        target3.setLongitude(sydney3.longitude)

        val target4 = Location("target")
        target4.setLatitude(sydney4.latitude)
        target4.setLongitude(sydney4.longitude)

        val target5 = Location("target")
        target5.setLatitude(sydney4.latitude)
        target5.setLongitude(sydney4.longitude)

        val target6 = Location("target")
        target6.setLatitude(sydney5.latitude)
        target6.setLongitude(sydney5.longitude)

        val target7 = Location("target")
        target7.setLatitude(sydney6.latitude)
        target7.setLongitude(sydney6.longitude)

        println("-------- ${currentLocation?.distanceTo(target1)!! < 100F}")
//        Toast.makeText(this, " 1-------- ${currentLocation?.distanceTo(target1)!! < 100F}", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, " 2 -------- ${currentLocation?.distanceTo(target2)!! < 300F}", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, " 3 -------- ${currentLocation?.distanceTo(target3)!! < 500F}", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, " 4 -------- ${currentLocation?.distanceTo(target4)!! < 700F}", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, " 4,1000 -------- ${currentLocation?.distanceTo(target4)!! < 1000F}", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, " 4,100 -------- ${currentLocation?.distanceTo(target4)!! < 100F}", Toast.LENGTH_SHORT).show()

        Toast.makeText(this, " 5, остановка -------- ${currentLocation?.distanceTo(target4)!! < 200F}", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, " 6, Апполо -------- ${currentLocation?.distanceTo(target4)!! < 200F}", Toast.LENGTH_SHORT).show()

        println(currentLocation?.distanceTo(target1)!!) // парк //718.63293
        println(currentLocation?.distanceTo(target2)!!) // Стадион // 472.52606
        println(currentLocation?.distanceTo(target3)!!) // Раблчая // 2449.3254
        println(currentLocation?.distanceTo(target4)!!) // 312.72922

        val parser = Parser()
        Coroutines.ioThenMain({
            parser.doInBackground(parser.getDirectionUrl(LatLng(48.4316353,35.0268223),
                LatLng(48.4429017,34.9974127))!!)
        }){
            println("&&&&&&&&&&&&&&&&&&&&&&&&&&& " + it)
            mMap.addPolyline(parser.onPostExecute(it!!))
        }

    }







    override fun onStart() {
        super.onStart()
        if (currentLatLng == null) {
            when {
                PermissionUtils.isAccessFineLocationGranted(this) -> {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            fetchLocation()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                }
                else -> {
                    PermissionUtils.requestAccessFineLocationPermission(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1000 -> if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fetchLocation()
            }
        }

    }

    fun newCamera(){
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(0.0, 0.0))
            .bearing(45f)
            .tilt(90f)
            .zoom(mMap.getCameraPosition().zoom)
            .build()

//
//        mMap.animateCamera(
//            CameraUpdateFactory.newCameraPosition(cameraPosition),
//            ANIMATE_SPEEED_TURN,
//            object : CancelableCallback {
//                override fun onFinish() {
//                    if (++currentPt < markers.size()) {
//                        val cameraPosition = CameraPosition.Builder()
//                            .target(targetLatLng)
//                            .tilt(if (currentPt < markers.size() - 1) 90 else 0.toFloat()) //.bearing((float)heading)
//                            .zoom(mMap.getCameraPosition().zoom)
//                            .build()
//                        mMap.animateCamera(
//                            CameraUpdateFactory.newCameraPosition(cameraPosition),
//                            3000,
//                            simpleAnimationCancelableCallback
//                        )
//                        highLightMarker(currentPt)
//                    }
//                }
//                override fun onCancel() {}
//            }
//        )
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    private fun addNewMarker(){
        val sydney = LatLng(48.428694, 35.018050)
        val sydney2 = LatLng(48.4329635, 35.0193128)
        val sydney3 = LatLng( 48.446958, 35.000468)
        val sydney5 = LatLng(48.4281306,35.0216886)

        val sydney6 = LatLng(48.4280261,35.016418)

        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Парк")
                .snippet(setDistance(sydney))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(sydney2)
                .snippet(setDistance(sydney2))
                .title("Стадион")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(sydney3)
                .snippet(setDistance(sydney3))
                .title("Рабочая")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(sydney5)
                .snippet(setDistance(sydney5))
                .title("Остановка")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(sydney6)
                .snippet(setDistance(sydney6))
                .title("Аполло")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )


    }

    override fun onCameraMoveCanceled() {
        // [START_EXCLUDE silent]
        // When the camera stops moving, add its target to the current path, and draw it on the map.
        Log.d(TAG, "onCameraMoveCancelled")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val place = Autocomplete.getPlaceFromIntent(data!!)
        Toast.makeText(this, TAG + "Place: " + place.name + ", " + place.id + ", " + place.latLng, Toast.LENGTH_SHORT).show()

    }

    /*
    * Вызывается, когда известно новое местоположение пользователя.
    * */
    override fun onLocationChanged(p0: Location) {
        Toast.makeText(this, "new locale "+ p0.longitude, Toast.LENGTH_SHORT).show()
    }

}