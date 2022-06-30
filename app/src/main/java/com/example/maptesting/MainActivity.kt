package com.example.maptesting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.SparseArray
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieCompositionFactory.fromJson
import com.example.maptesting.adapters.PlaceArrayAdapter
import com.example.maptesting.data.CarMarker
import com.example.maptesting.data.PlaceDataModel
import com.example.maptesting.data.building_route.Geometry
import com.example.maptesting.data.building_route.Polygons
import com.example.maptesting.databinding.ActivityMainBinding
import com.example.maptesting.fragments.DeliveryAddressSearchFragment
import com.example.maptesting.google_map_util.*
import com.example.maptesting.retrofit.ApiClient
import com.example.maptesting.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonLineString
import com.mindorks.example.ubercaranimation.util.AnimationUtils
import com.mindorks.example.ubercaranimation.util.AnimationUtils.zoomRoute
import com.mindorks.example.ubercaranimation.util.MapUtils
import com.mindorks.example.ubercaranimation.util.MapUtils.getAddress
import com.mindorks.example.ubercaranimation.util.MapUtils.getDistanceMeters
import com.mindorks.example.ubercaranimation.util.MapUtils.getLocation
import com.mindorks.example.ubercaranimation.util.MapUtils.getNearestMarker
import com.mindorks.example.ubercaranimation.util.MapUtils.getRegion
import kotlinx.coroutines.delay
import org.json.JSONException
import java.io.IOException


class MainActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnCameraMoveCanceledListener{

    /**Fragment**/
    private var savedStateSparseArray = SparseArray<Fragment.SavedState>()
    private var currentSelectItemId = 1

    companion object{
        const val LOCATION_PERMISSION_REQUEST_CODE = 999
        const val DEFAULT_ZOOM = 15

        //Сохраниение состояния Fragments
        const val SAVED_STATE_CONTAINER_KEY = "ContainerKey"
        const val SAVED_STATE_CURRENT_TAB_KEY = "CurrentTabKey"

    }


    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var currentLocation: LatLng? = null
    private lateinit var googleMap: GoogleMap
    private var currentMarker : Marker? = null // главный маркер
    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var locationRequest  : LocationRequest

    //Точки маршрута
    val path = java.util.ArrayList<LatLng>()
    //Сюда передаю растояние атомобиля до полтзователя
    val distanseCar = java.util.ArrayList<Float>()
    //Здесь храняться объекты на все автомобили
    private val arrMarker : java.util.ArrayList<Marker> = arrayListOf()
    //Индекс ближайший машины
    private var indexNearestCar = -1 //По умолчанию -1 (Не найден)
    /* Делаем поиск */
    private var placeAdapter: PlaceArrayAdapter? = null
    private lateinit var mPlacesClient: PlacesClient

    private lateinit var binding: ActivityMainBinding
    private lateinit var autoCompleteEditText : AppCompatAutoCompleteTextView
    private lateinit var addNewMarker : ImageButton


    private val TAG = MapsActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Инициализация поиска */
        Places.initialize(this, getString(R.string.google_key))
        mPlacesClient = Places.createClient(this)
        placeAdapter = PlaceArrayAdapter(this, R.layout.layout_item_places, mPlacesClient)
        autoCompleteEditText = binding.autoCompleteEditText
        addNewMarker = binding.addNewMarker
        autoCompleteEditText.setAdapter(placeAdapter)
        /* end */
        /* Сам поиск */
        autoCompleteEditText.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as PlaceDataModel
            autoCompleteEditText.apply {
                println(getLocationFromAddress(this@MainActivity, place.fullText))
                setText(place.fullText.split(",")[0])
                setSelection(autoCompleteEditText.length())

                hideSoftKeyBoard(this@MainActivity,autoCompleteEditText)


                val lanlon = getLocationFromAddress(this@MainActivity, place.fullText)

                currentLocation = lanlon
                //Изменить позицию маркера
                currentMarker!!.position = lanlon!!
                //Передвижение карты сделом за меркером
                getDeviceLocation()


            }
        }

        addNewMarker.setOnClickListener {
            val showRouteSelection = binding.showRouteSelection
            showRouteSelection.root.visibility = it.visibility

            val slideUp: Animation = loadAnimation(this, R.anim.animate_slide_up_enter)
            showRouteSelection.root.startAnimation(slideUp);

            ///additionAdress()

            swapFragments(1, "key", "red")
        }



        binding.button3.setOnClickListener {
            //Запускаю анимацию движения автомобиля с точки B к A
            searchNearestCar()
        }



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setUpdateGoogleMap()
    }


    /**
     * Вызываю фрагмент
     * **/

    private fun swapFragments(actionId: Int, key: String, color: String) {
        if (supportFragmentManager.findFragmentByTag(key) == null) {
            savedFragmentState(actionId)
            createFragment(key, color, actionId)
        }
    }

    /**
     * Обновление карты
     * */

    private fun setUpdateGoogleMap(){
        locationManager = applicationContext
            .getSystemService(LOCATION_SERVICE) as LocationManager

        //Настройки для автообновления карты
        locationRequest  = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(35.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    /**
     * This function is used to update the location of the Cab while moving from Origin to Destination
     */
    private fun updateCarLocation(latLng: LatLng) {
        if (movingCabMarker == null) {
            movingCabMarker = arrMarker.get(indexNearestCar)
        }
        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng!!
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            //animateCamera(currentLatLng!!)
        } else {
            previousLatLng = currentLatLng
            currentLatLng = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLng != null && previousLatLng != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                        multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                    )
                    Coroutines.ioThenMain({
                        delay(250L)
                    }){
                        movingCabMarker?.position = nextLocation
                        val rotation = MapUtils.getRotation( nextLocation, currentLatLng!!)
                        if (!rotation.isNaN()) {
                            movingCabMarker?.rotation = rotation
                        }
                        movingCabMarker?.setAnchor(0.5f, 0.5f)
                    }

                   // animateCamera(arrMarker.get(indexNearestCar).position)
                }
            }
            valueAnimator.start()
        }
    }

    private fun showMovingCab(carLatLngList: ArrayList<LatLng>) {
        handler = Handler()
        var index = 0
        runnable = Runnable {
            run {
                if (index < path.size-1) {
                    println("---- size")
                    println(path.size)
                    updateCarLocation(carLatLngList[index])
//                    //Передвижение карты сделом за меркером
//                    googleMap.animateCamera(
//                        CameraUpdateFactory
//                            .newCameraPosition(
//                                CameraPosition.Builder()
//                                    .target(carLatLngList[index])
//                                    .zoom(DEFAULT_ZOOM.toFloat()-1f)
//                                    .build()
//                            )
//                    )

                    //animationCameraMovementBehindCar(carLatLngList[index])
                    handler.postDelayed(runnable, 3000)
                    ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(this@MainActivity, "Trip Ends", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    /**
     * Вызываеться когда карта загружена
     * **/
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        drawMarket(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!))

        println("${(currentLocation?.latitude)}  ${ currentLocation?.longitude}")

        //!!! ИЗМЕНИТЬ !!! РИСУЕТ МАРШРУТ

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(p0: Marker) {}
            //Перерисовать маркер
            override fun onMarkerDragEnd(p0: Marker) {
                if (currentMarker != null){
                    currentMarker?.remove()
                }

                /**
                 * Если клиент перетащий маркер, то нужно переписать кооридинаты
                 * currentLocation для поиска ближайшего автомобиля
                 * **/

                val newLating = LatLng(p0.position.latitude, p0.position.longitude)
                currentLocation = newLating
                drawMarket(newLating)
            }
            override fun onMarkerDragStart(p0: Marker) {}
        })

        //Передвижение карты сделом за меркером
        getDeviceLocation()

    }

    /**
     * Настройки гугл карты
     * рисуем марукер
     * получаем текущий адресс
     *
     *
     * !!! Нужно позделить на отделтные фунции !!!
     * */

    private fun drawMarket(latLng: LatLng) {


        val markerOption = MarkerOptions()
            .position(latLng)
            .title("I am heare")
            .draggable(true) //Чтобы маркер двигался
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_marker))

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        currentMarker = googleMap.addMarker(markerOption)
        currentMarker?.showInfoWindow()

        //Определяю текущий адресс по координатам
        autoCompleteEditText.setText(getAddress(this,latLng.latitude, latLng.longitude))

        //Добавляю бензовазы на карту
        if(arrMarker.size == 0){
            addNewMarker()
        }
        //Выделяю область
        bordersColorMap()
    }

    /*
    *   Добавить дополнительный маркер. Для того чтобы клиент мог указать дополнительный адрес доставки
    *   Маркер добавляю в центр окна, далее клиент перетаскивает нужный маркер на нужный адрес
    * */

    private fun additionAdress(){

        val markerOption = MarkerOptions()
            .position(googleMap.cameraPosition.target)
            .title("I am heare")
            .draggable(true) //Чтобы маркер двигался
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_add))
        currentMarker = googleMap.addMarker(markerOption)
        currentMarker!!.showInfoWindow()


        setUpdateGoogleMap()
    }

    /**
     * Рисую маршрут и анимирую его здесь
     * Для теста задаю дефолтные значения
     * */

    private fun drawRoute(){

        Coroutines.ioThenMain({
            delay(1000L)
            ApiClient().getCaptureError(
                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                getLocationFromAddress(this@MainActivity,
                    "проспект Пилипа Орлика, 20а, Дніпро, Дніпропетровська область, 49000")!!,
                getString(R.string.google_key))
        }){

            //Рисую линию маршрута
            for (k in 0 until it!![0].legs[0].steps.size){
                path.addAll(decodePolyline(it[0].legs[0].steps[k].polyline.points))
            }

            //Анимирую линию маршрута
            if(googleMap != null) {
                MapAnimator.instance?.animateRoute(googleMap, path);
            }

            //Запускаю внимацию автомобиля.
           // showMovingCab(path)

        }
    }

//Рисую маршрут от автомобиля до клиента
    private fun drawRoute2(startRoute : LatLng){

        Coroutines.ioThenMain({
            delay(1000L)
            ApiClient().getCaptureError(
                startRoute,
                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                getString(R.string.google_key))
        }){

            //Перемешаю карту до ближайшего бенеовоза
            animateCamera(startRoute)

            //Рисую линию маршрута
            for (k in 0 until it!![0].legs[0].steps.size){
                path.addAll(decodePolyline(it[0].legs[0].steps[k].polyline.points))
            }

            //Маштабирую карту относительно длины маршрута
            zoomRoute(googleMap, path)
           // showFullRouteCamera(path.get(0), currentLocation!!)

            //Анимирую линию маршрута
            if(googleMap != null) {
                MapAnimator.instance?.animateRoute(googleMap, path);
            }



            //Запускаю анимацию автомобиля.
             showMovingCab(path)

        }

    }

    /**
     * Добавиляю на карту тестовые автомобили
     * **/

    private fun createMarker() : java.util.ArrayList<CarMarker> {
        val carMarker = arrayListOf<CarMarker>()

        for (i in 0..4) {

            /**
             * Чисто для теста. Ставим рандомно автомобиля в радиусе 12км
             * */
            val latLngRnd =  getLocation(currentLocation!!.latitude, currentLocation!!.longitude, 12000)

            carMarker.add(
                CarMarker(
                    latLngRnd,
                    "car$i",
                    "test snippet",
                    R.drawable.ic_car
                )

            )
        }
        return carMarker
    }

    private fun addNewMarker() {

        createMarker().forEach {
            Log.i("car", "${it.title} ${it.latLng} ${it.snippet}")
            arrMarker.add( googleMap.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.title)
                    .snippet(it.snippet)
                    .icon(BitmapDescriptorFactory.fromResource(it.icon))
            )!!)
        }
    }

    //Поиск бижайшего автомобиля
    fun searchNearestCar(){

        arrMarker.forEach {
            val distance = getDistanceMeters(
                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                it.position
            ) / 1000
            println("----- +++ " + distance)

            //Добавляю расстояния атомобилей от пользователя
            distanseCar.add(distance)
        }


        //Поиск ближайшего
        indexNearestCar = getNearestMarker(distanseCar)
        println("Ближайший автомобиль "+indexNearestCar)

        //Когда найден ближайший автомобиль
        // Рисуем маршрут от него до клиента
        arrMarker.get(indexNearestCar).setIcon(BitmapDescriptorFactory
            .fromResource(R.drawable.ic_car))

       // animateCamera(arrMarker.get(indexNearestCar).position)

        drawRoute2(arrMarker.get(indexNearestCar).position)

    }


    /**
     * Инициализация карты
     * **/

    private fun fetchLocation(){
        println("init currentLocation")
        try {
            val task = fusedLocationProviderClient?.lastLocation
            task?.addOnSuccessListener { location ->

                if (location != null){
                    this.currentLocation = LatLng(location.latitude, location.longitude)
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
               }
            }

        }catch (e: SecurityException){
            println(TAG + "${e.message}")
        }

    }


    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            val locationPermissionGranted = true
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(currentLocation!!.latitude,
                                    currentLocation!!.longitude), MapsActivity.DEFAULT_ZOOM.toFloat()))

                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun animationCameraMovementBehindCar(latlng : LatLng) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            val locationPermissionGranted = true
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(latlng.latitude,
                                    latlng.longitude), MapsActivity.DEFAULT_ZOOM.toFloat()))

                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    //Выделение  активной области на карте
    //https://stackoverflow.com/questions/41431384/highlight-whole-countries-in-google-maps-for-android
    //https://nominatim.openstreetmap.org/ui/search.html
    //convert json
    //http://polygons.openstreetmap.fr/index.py
    /**
     * Чтобы добавить новую область в Json, нужно сделать так
     *
     * ]
     *  ],
     *  [
     * [
     *  [30.1595414, 50.5507233],
     *  [30.179497, 50.5444823]
     *  .....
     * ]
     *  ]
     *
     *    ]
     *   }]
     *  }
     * */
    private fun bordersColorMap(){

        try {
            val layer = GeoJsonLayer(googleMap, R.raw.borders_kyiv_region, applicationContext)
            val style = layer.defaultPolygonStyle
            style.fillColor = 0x12f3b70f
            style.zIndex = 0.9f
            style.strokeColor = Color.MAGENTA
            style.strokeWidth = 1f
            layer.addLayerToMap()
        } catch (ex: IOException) {
            Log.e("IOException", ex.getLocalizedMessage())
        } catch (ex: JSONException) {
            Log.e("JSONException", ex.localizedMessage)
        }

        println("--------------- ")
        getUser(this)

    }

    private fun getUser(activity : Activity){

        val geometry : Geometry
      //  val json = mRead(getResources().openRawResource(R.raw.borders_kyiv_region));

        println("0000000000 ---------- 00000000000")
        val reg = getRegion(this@MainActivity,
            currentLocation!!.latitude,
            currentLocation!!.longitude)
        println(reg)
        println("0000000000 ---------- 00000000000")
//        try {
//            // convert json in an User object
//            geometry = Gson().fromJson("", Geometry::class.java)
//            println(geometry)
//        }
//        catch (e : Exception) {
//            // we never know :)
//            Log.e("error parsing", e.toString());
//        }


    }

    private fun location2(){



        //val isLocationOnPath = PolyUtil.isLocationOnPath(currentLocation, GeoJsonFeature(), true, tolerance)
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
                        MapsActivity.LOCATION_PERMISSION_REQUEST_CODE
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


    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()

        return false
    }

    override fun onCameraMoveCanceled() {
    }



    private fun createFragment(key: String, color: String, actionId: Int) {
        val fragment = DeliveryAddressSearchFragment()
        fragment.setInitialSavedState(savedStateSparseArray[actionId])
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, fragment, key)
            .commit()
    }

    private fun savedFragmentState(actionId: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container_fragment)
        if (currentFragment != null) {
            savedStateSparseArray.put(currentSelectItemId,
                supportFragmentManager.saveFragmentInstanceState(currentFragment)
            )
        }
        currentSelectItemId = actionId
    }

}
