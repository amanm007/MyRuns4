package com.example.manpreet_singh_sarna_myruns2

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.manpreet_singh_sarna_myruns2.room_database.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MapActivity : FragmentActivity(), OnMapReadyCallback{

    // Location permission
    private lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationSettingsIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var locationSourceSettingsIntentLauncher: ActivityResultLauncher<Intent>
    var mMap: GoogleMap? = null
    lateinit var permissionDialog: AlertDialog
    var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private lateinit var myLatLng: LatLng
    var latLng: LatLng? = null
    private var polyline: Polyline? = null
    private var marker: Marker? = null
    lateinit var onBackPressedCallback: OnBackPressedCallback

    private val androidLocationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    var longitude: Double? = null
    var latitude: Double? = null
    var destinationLatitude: Double? = null
    var destinationLongitude: Double? = null
    lateinit var mapKey: String

    private lateinit var llMapView: LinearLayout
    private lateinit var inputType: String
    private lateinit var activityType: String
    private lateinit var tvActivityType: TextView
    private lateinit var tvAverageSpeed: TextView
    private lateinit var tvCurrentSpeed: TextView
    private lateinit var tvClimb: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnAllowAccess: Button
    private lateinit var imgMap: ImageView
    private lateinit var imgMapDirection: ImageView
    private lateinit var llAllowLocation: LinearLayout

    private lateinit var context: Activity
    lateinit var currentLocation: Location

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var historyViewModel: ExerciseEntryViewModel

    companion object {
        private const val REQUEST_CODE = 101
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0
        private var REQUEST_CODE_CHECK_SETTINGS = 321
        private const val TAG = "SelectLocationActivity"

        // Assuming average walking speed for an average man is 5 km/h
        private const val AVERAGE_WALKING_SPEED_KMH = 5.0

        // Rough estimate for calories burned per meter for an average man
        private const val KCAL_PER_METER = 0.063
    }

    private var distanceCovered: Float = 0f
    private var totalDistance: Float = 0f
    private var caloriesBurned: Double = 0.0
    private var averageSpeed: Double = 0.0
    private var timeTaken: Long = 0L
    private lateinit var currentDate: String
    private lateinit var currentTime: String

    // Declaring notification variables
    var notificationManager: NotificationManager? = null
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: NotificationCompat.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"
    private val notificationId = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)

        initViews()
        initPermissionsResultLauncher()
        checkLocationPermissions()
        onBackPressCallback()

    }

    private fun sendNotification() {
        // it is a class to notify the user of events that happen.
        // This is how you tell the user that something has happened in the
        // background.
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        // pendingIntent is an intent for future use i.e after
        // the notification is clicked, this intent will come into action
        val parent = Intent(applicationContext, MainActivity::class.java)
        // FLAG_UPDATE_CURRENT specifies that if a previous
        // PendingIntent already exists, then the current one
        // will update it with the latest intent
        // 0 is the request code, using it later with the
        // same method again will get back the same pending
        // intent for future reference
        // intent passed here is to our MapActivity class
        // Adds the back stack for the Intent

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(parent)
        val pendingIntent: PendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager!!.createNotificationChannel(notificationChannel)

            builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("MyRuns").setContentText("Recording your path now")
                .setSmallIcon(R.mipmap.ic_launcher).setPriority(
                    Notification.PRIORITY_HIGH
                ).setContentIntent(pendingIntent).setAutoCancel(true)

        } else {

            builder = NotificationCompat.Builder(this)
                .setContentTitle("MyRuns").setContentText("Recording your path now")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentIntent(pendingIntent)
        }
        notificationManager!!.notify(notificationId, builder.build())
    }

    // Method to dismiss the notification
    private fun dismissNotification() {
        if (notificationManager != null){
            notificationManager!!.cancel(notificationId)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        context = this@MapActivity
        llMapView = findViewById(R.id.llMapView)
        tvActivityType = findViewById(R.id.tvActivityType)
        tvAverageSpeed = findViewById(R.id.tvAverageSpeed)
        tvCurrentSpeed = findViewById(R.id.tvCurrentSpeed)
        tvClimb = findViewById(R.id.tvClimb)
        tvCalories = findViewById(R.id.tvCalories)
        tvDistance = findViewById(R.id.tvDistance)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        imgMap = findViewById(R.id.imgMap)
        imgMapDirection = findViewById(R.id.imgMapDirection)
        btnCancel = findViewById(R.id.btnCancel)
        btnAllowAccess = findViewById(R.id.btnAllowAccess)
        llAllowLocation = findViewById(R.id.llAllowLocation)

        inputType = intent.getStringExtra("input_type")!!
        activityType = intent.getStringExtra("activity_type")!!

        tvActivityType.text = "Type: $activityType"
        tvAverageSpeed.text = "Avg speed: 0 m/h"
        tvCurrentSpeed.text = "Cur speed: 0 m/h"
        tvCalories.text = "Calorie: 0"
        tvDistance.text = "Distance: 0 Miles"
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // Init Repository & ViewModel
        database = ExerciseEntryDatabase.getInstance(this@MapActivity)
        databaseDao = database.exerciseEntryDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(
            this@MapActivity,
            viewModelFactory
        )[ExerciseEntryViewModel::class.java]


        isLocationEnabled()
        getLocationPermission()

        //Search Places Code
        mapKey = resources.getString(R.string.map_key)
        if (!Places.isInitialized()) {
            Places.initialize(this@MapActivity, mapKey)
        }

        btnSave.setOnClickListener {
            getCurrentDateTime()
            saveEntryToDatabase()
        }

        btnCancel.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
        imgMap.setOnClickListener {
            // Default google map
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)

/*            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:51.5, 0.125"))
            startActivity(intent)*/
        }
        imgMapDirection.setOnClickListener {
            // Directions
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=$latitude,$longitude&daddr=$destinationLatitude,$destinationLongitude")
            )
            startActivity(intent)

            /*val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=21.5, 0.15&daddr=-49.5, 0.15")
            )
            startActivity(intent)*/
        }
        btnAllowAccess.setOnClickListener {
            val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            locationSourceSettingsIntentLauncher.launch(locationSettingsIntent)
        }
    }


    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            if (!isDeviceLocationOn()) {
                llMapView.visibility = View.GONE
                llAllowLocation.visibility = View.VISIBLE
            } else {
                llMapView.visibility = View.VISIBLE
                llAllowLocation.visibility = View.GONE
                initData()
            }
        } else {
            requestLocationPermissionLauncher.launch(androidLocationPermissions)
        }

    }

    private fun initPermissionsResultLauncher() {
        requestLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { result: Map<String, Boolean> ->
                val permissions: Array<String> =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        androidLocationPermissions
                    } else {
                        androidLocationPermissions
                    }
                var permissionGranted = false
                for (i in 0 until result.size) {
                    if (java.lang.Boolean.TRUE == result[permissions[i]]) {
                        permissionGranted = true
                    } else {
                        permissionGranted = false
                        break
                    }
                }
                if (permissionGranted) {
                    locationPermissionGranted = true
                } else {
                    buildPermissionAlert()
                }
            }

        locationSettingsIntentLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { checkLocationPermissions() }

        locationSourceSettingsIntentLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { checkLocationPermissions() }
    }

    @SuppressLint("SetTextI18n")
    fun buildPermissionAlert() {
        val builder = AlertDialog.Builder(context, R.style.CustomDialog)
        val customLayout: View =
            LayoutInflater.from(context).inflate(R.layout.permissions_alert, null)
        builder.setView(customLayout)
        val label = customLayout.findViewById<TextView>(R.id.txtViewLabel)
        label.text = "Permission Required!"
        label.setTextColor(resources.getColor(R.color.white))
        val message = customLayout.findViewById<TextView>(R.id.txtViewMessage)
        message.text = "Go to setting and allow the location permission for this app."
        val btnSettings = customLayout.findViewById<TextView>(R.id.btnRight)
        btnSettings.text = "Settings"
        val btnCancel = customLayout.findViewById<TextView>(R.id.btnLeft)
        btnCancel.text = "Cancel"
        permissionDialog = builder.create()
        permissionDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        permissionDialog.setCancelable(false)
        permissionDialog.show()
        btnSettings.setOnClickListener {
            permissionDialog.dismiss()

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            locationSettingsIntentLauncher.launch(intent)
        }
        btnCancel.setOnClickListener { permissionDialog.dismiss() }
    }

    //Check if location enabled or not
    private fun isLocationEnabled() {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            showLocationPopup()
        } else {
            fetchLastLocation()
        }
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
            }
        }
        updateLocationUI()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //Show location popup
    private fun showLocationPopup() {
        val locationRequest =
            LocationRequest.create().setInterval(10000).setFastestInterval(1000).setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY
            )
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(builder.build())
            .addOnFailureListener { e ->
                try {
                    val resolvable = e as ResolvableApiException
                    //For Activity Use
                    resolvable.startResolutionForResult(context, REQUEST_CODE_CHECK_SETTINGS)
                    //For Fragment Use
                    /* startIntentSenderForResult(
                         resolvable.resolution.intentSender,
                         REQUEST_CODE_CHECK_SETTINGS,
                         null,
                         0,
                         0,
                         0,
                         null
                     )*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
    }

    private fun fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        val task = fusedLocationProviderClient!!.lastLocation.addOnCompleteListener(
            OnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        currentLocation = task.result
                        latitude = currentLocation.latitude
                        longitude = currentLocation.longitude
                    } else {
                        val locationRequest = LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1)
                        val locationCallback: LocationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                val location1 = locationResult.lastLocation
                                latitude = location1!!.latitude
                                longitude = location1.longitude
                                getDeviceLocation("GetMyLocation")
                            }
                        }
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@OnCompleteListener
                        }
                        fusedLocationProviderClient!!.requestLocationUpdates(
                            locationRequest, locationCallback, Looper.myLooper()!!
                        )
                    }
                }
            })
        task.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Send Notification for notify
                sendNotification()

                latitude = location.latitude
                longitude = location.longitude
                getDeviceLocation("GetMyLocation")
                if (latitude == null && longitude == null) {
                    Handler(Looper.myLooper()!!).postDelayed(
                        { getDeviceLocation("GetMyLocation") },
                        1500
                    )
                }
                currentLocation = location


                val supportMapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                supportMapFragment!!.getMapAsync(this)

            }
        }
    }

    private fun getDeviceLocation(callFrom: String) {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient!!.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            //This goes up to 21
                            val zoomLevel = 12.0f
                            myLatLng = if (callFrom == "GetMyLocation") {
                                //For Zoom MAp
                                mMap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ), zoomLevel
                                    )
                                )
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            } else {
                                //For Zoom MAp
                                mMap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ), zoomLevel
                                    )
                                )
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                )
                            }

                            val addresses: List<Address>?
                            val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
                            try {
                                addresses = geocoder.getFromLocation(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude,
                                    1
                                )
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            mMap!!.addMarker(
                                MarkerOptions()
                                    .position(myLatLng)
                                    .draggable(false)
                                    .title("Country")
                            )
                            mMap!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    myLatLng,
                                    zoomLevel
                                )
                            )
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(myLatLng))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                            mMap!!.uiSettings.isZoomControlsEnabled = false
                            mMap!!.uiSettings.isZoomGesturesEnabled = true
                            mMap!!.uiSettings.isMapToolbarEnabled = false
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        val zoomLevel = 12.0f //This goes up to 21
                        mMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(-33.852, 151.211),
                                zoomLevel
                            )
                        )
                        mMap!!.uiSettings.isMyLocationButtonEnabled = false
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng!!))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Get the current location of the device and set the position of the map.
        getDeviceLocation("IntentLatLngPosition")
        getMarkerPosition()

        mMap!!.uiSettings.isMapToolbarEnabled = true
        // Set up map click listener
        mMap!!.setOnMapClickListener { clickedLatLng ->
            if (inputType == "GPS") {
                // Draw polyline between current location and clicked position
                drawPolyline(clickedLatLng)
            } else {
                // Move the marker to the clicked position
                moveMapMarker(clickedLatLng)
            }
        }

        // Initialize the Polyline
        polyline = mMap!!.addPolyline(PolylineOptions().width(5f))
    }

    //Map Camera Move Code
    private fun getMarkerPosition() {
        mMap!!.setOnCameraIdleListener { /*LatLng mPosition = googleMap.getCameraPosition().target;
                Float mZoom = googleMap.getCameraPosition().zoom;*/
            latitude = mMap!!.cameraPosition.target.latitude
            longitude = mMap!!.cameraPosition.target.longitude

            var addresses: List<Address>?
            val geocoder: Geocoder = Geocoder(this@MapActivity, Locale.getDefault())
            try {
                addresses = ArrayList()
                if (addresses.isNotEmpty()) {
                    addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val latLng1 = LatLng(latitude!!, longitude!!)
            mMap!!.clear()
            mMap!!.addMarker(
                MarkerOptions()
                    .position(latLng1)
                    .title("Country")
            )
        }
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    //Check if location enabled or not
    private fun isDeviceLocationOn(): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return !(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    private fun moveMapMarker(newPosition: LatLng) {
        // Remove the previous marker if it exists
        marker?.remove()

        // Add a new marker at the clicked position
        marker = mMap?.addMarker(MarkerOptions().position(newPosition).title("New Location"))

        // Center the map camera on the clicked position
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(newPosition))
        // Get the latitude and longitude of the new position
        destinationLatitude = newPosition.latitude
        destinationLongitude = newPosition.longitude
    }

    @SuppressLint("SetTextI18n")
    private fun drawPolyline(destination: LatLng) {

        // Calculate distance and update distanceCovered variable
        val destinationLocation = Location("Destination")
        destinationLocation.latitude = destination.latitude
        destinationLocation.longitude = destination.longitude
        val distanceToDestination = currentLocation.distanceTo(destinationLocation)
        distanceCovered += distanceToDestination
        totalDistance += distanceToDestination

        // Convert distance to miles
        val distanceInMiles = distanceToDestination * 0.000621371

        // Format the distance string with only the integer part before the decimal
        val formattedDistance = String.format("%.0f", distanceInMiles)

        // Update the TextView with the formatted distance
        tvDistance.text = "Distance: $formattedDistance Miles"

        // Get current latitude and longitude
        latitude = currentLocation.latitude
        longitude = currentLocation.longitude

        // Get destination latitude and longitude
        destinationLatitude = destination.latitude
        destinationLongitude = destination.longitude

        // Calculate time taken, calories burned, and average speed
        calculateTimeTaken()
        calculateCaloriesBurned()
        calculateAverageSpeed()

        val polylineOptions = PolylineOptions()
            .add(LatLng(currentLocation.latitude, currentLocation.longitude))
            .add(destination)
            .width(5f)
            .color(ContextCompat.getColor(this, R.color.black)) // Use colorPrimary for the polyline

        // Clear existing markers and polylines
        mMap!!.clear()

        // Add markers for current location and clicked location
        mMap!!.addMarker(
            MarkerOptions().position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        ) // Green marker for current location
        mMap!!.addMarker(
            MarkerOptions().position(destination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        ) // Blue marker for clicked location

        // Add polyline to the map
        polyline = mMap!!.addPolyline(polylineOptions)
    }

    private fun calculateTimeTaken() {
        // Assuming constant walking speed
        val speedInMetersPerSecond = Companion.AVERAGE_WALKING_SPEED_KMH * 1000 / 3600.0
        val timeInSeconds = distanceCovered / speedInMetersPerSecond
        timeTaken = Math.round(timeInSeconds).toLong()
        Log.d(TAG, "Time Taken: $timeTaken seconds")
    }

    @SuppressLint("SetTextI18n")
    private fun calculateCaloriesBurned() {
        caloriesBurned = (distanceCovered * Companion.KCAL_PER_METER).toInt().toDouble()

        tvCalories.text = "Calorie: $caloriesBurned"
        Log.d(TAG, "Calories Burned: $caloriesBurned kcal")
    }

    @SuppressLint("SetTextI18n")
    private fun calculateAverageSpeed() {
        averageSpeed = distanceCovered / (timeTaken / 3600.0)

        // Format the average speed string with two decimal places
        val formattedSpeed = String.format("%.2f", averageSpeed)

        tvAverageSpeed.text = "Avg speed: $formattedSpeed m/h"
    }

    private fun getCurrentDateTime() {
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        currentDate = dateFormat.format(Date())
        currentTime = timeFormat.format(Date())
    }

    private fun saveEntryToDatabase() {
        val date = currentDate
        val time = currentTime
        val duration = timeTaken
        val distance = totalDistance
        val calories = caloriesBurned
        val heartRate = averageSpeed
        val comment = ""

        val newEntry = ExerciseEntry(
            inputType = inputType,
            activityType = activityType,
            date = date,
            time = time,
            duration = duration.toString(),
            distance = distance.toString(),
            calories = calories.toString(),
            heartRate = heartRate.toString(),
            comment = comment
        )

        // Insert the new entry into the database via the ViewModel and repository
        historyViewModel.insertEntry(newEntry)
        Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
        onBackPressedCallback.handleOnBackPressed()
    }

    private fun onBackPressCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dismissNotification()
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}