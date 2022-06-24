package com.example.maptesting

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maptesting.data.CarMarker
import com.example.maptesting.databinding.ActivityMapsBinding
import com.example.maptesting.google_map_util.CreateMarker
import com.example.maptesting.google_map_util.DistanceDetermination
import com.example.maptesting.google_map_util.MapAnimator
import com.example.maptesting.network.Parser
import com.example.maptesting.utils.Coroutines
import com.example.maptesting.utils.PermissionUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.widget.Autocomplete
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnCameraMoveCanceledListener,
    LocationSource.OnLocationChangedListener,
    android.location.LocationListener  {

    private lateinit var binding: ActivityMapsBinding

    companion object{
        const val LOCATION_PERMISSION_REQUEST_CODE = 999
        const val DEFAULT_ZOOM = 15
    }



    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var currentLocation : Location? = null
    private var currentMarker : Marker? = null
    private var currentLatLng: LatLng? = null
    private var mMap: GoogleMap? = null


    private lateinit var locationCallback: LocationCallback




    val locationPermissionGranted = true



    private lateinit var locationRequest  : LocationRequest

    private  var DEMO_LATITUDE = 48.430644
    private  var DEMO_LONGITUDE = 151.211



    private val TAG = MapsActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Получить местоположение клиента
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setUpdateGoogleMap()

    }

    private fun setUpdateGoogleMap(){
        locationManager = applicationContext
            .getSystemService(LOCATION_SERVICE) as LocationManager

        //Настройки для автообновления карты
        locationRequest  = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation.latitude,
                                    lastKnownLocation.longitude), DEFAULT_ZOOM.toFloat()))

                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /*
    * Когда карта готова вызвается этот  метод
    * */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.isMyLocationEnabled = true
        mMap?.setOnMyLocationButtonClickListener(this)
        mMap?.setOnMyLocationClickListener(this)


        val latLong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarket(latLong)

        mMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
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
        mMap?.setOnMarkerClickListener(object : OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {

                Log.i("click", "id - ${p0.id}, title - ${p0.title}, snippet - ${p0.snippet}, position - ${p0.position}")
                println("marker "+p0.position)
                return true
            }
        })
        

        if (mMap != null){
            getDeviceLocation()
        }
        addNewMarker()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    DEMO_LATITUDE = location.latitude
                    DEMO_LONGITUDE = location.longitude

                    currentMarker!!.setPosition(LatLng(location.latitude,  location.longitude))


                    Toast.makeText(baseContext, "--- "+location.longitude, Toast.LENGTH_SHORT).show()
                }
            }
        }

        startLocationUpdates()
    }


    lateinit var markerOption : MarkerOptions
    private fun drawMarket(latLong : LatLng){
         markerOption = MarkerOptions().position(latLong).title("I am heare")
            .snippet(getAddress(DEMO_LATITUDE, DEMO_LONGITUDE)).draggable(true) //Чтобы маркер двигался
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker))


        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 75f))
        currentMarker = mMap?.addMarker(markerOption)

        mMap?.uiSettings!!.isMapToolbarEnabled = false
        mMap?.setOnMyLocationButtonClickListener(this)
        mMap?.setOnMyLocationClickListener(this)


        currentMarker?.showInfoWindow()

        getLocation()

    }


    private fun getAddress(lat : Double, lon : Double) : String?{
        val local = Locale("uk_UA") // Список доступных городов https://stackoverflow.com/a/7989085/5722608
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

        val parser = Parser()
        Coroutines.ioThenMain({
            parser.doInBackground(parser.getDirectionUrl(LatLng(48.4316353,35.0268223),
                LatLng(48.4429017,34.9974127))!!)
        }){
            it?.forEach {
                println("----")
                if(mMap != null) {
                    MapAnimator.instance?.animateRoute(mMap!!, it);
                }
                println(it)
                println("----")
            }
            //mMap?.addPolyline(parser.onPostExecute(it!!))
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
            .zoom(mMap?.getCameraPosition()!!.zoom)
            .build()
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()

        return false
    }

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

    private fun createMarker() : ArrayList<CarMarker> {
        val carMarker = arrayListOf<CarMarker>()

        for (i in 0..4) {

            val latLngRnd = LatLng(
                Random.nextDouble(48.4280261, 48.446958),
                Random.nextDouble(35.000468, 35.0216886)
            )

            carMarker.add(
                CarMarker(
                    latLngRnd,
                    "car$i",
                    setDistance(latLngRnd),
                    R.drawable.ic_car
                )

            )
        }
        return carMarker
    }

    private fun addNewMarker() {

        createMarker().forEach {
            Log.i("car", "${it.title} ${it.latLng} ${it.snippet}")
            mMap?.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.title)
                    .snippet(it.snippet)
                    .icon(BitmapDescriptorFactory.fromResource(it.icon))
            )
        }
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
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }





    /*
    * Вызывается, когда известно новое местоположение пользователя.
    * */
    override fun onLocationChanged(p0: Location) {
        Toast.makeText(this, "new locale "+ p0.longitude, Toast.LENGTH_SHORT).show()
    }

}