package com.example.mapboxnavigation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mapbox.android.core.location.*
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.NavigationView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.route.RouteArrow
import com.mapbox.navigation.ui.maps.route.RouteLine
import com.mapbox.navigation.ui.voice.NavigationVoiceController
import com.mapbox.navigation.ui.voice.VoiceInstructionPlayer

class MainActivity : AppCompatActivity(), MultiplePermissionsListener {
    
    private lateinit var mapView: MapView
    private lateinit var navigationView: NavigationView
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var routeLine: RouteLine
    private lateinit var routeArrow: RouteArrow
    private lateinit var voiceController: NavigationVoiceController
    private lateinit var locationEngine: LocationEngine
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // Helper classes
    private lateinit var navigationHelper: NavigationHelper
    private lateinit var uiComponentsManager: UIComponentsManager
    
    private var currentLocation: Location? = null
    
    companion object {
        private const val TAG = "MapboxNavigation"
        private const val REQUEST_CODE_LOCATION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        requestPermissions()
        setupMapbox()
    }
    
    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        navigationView = findViewById(R.id.navigationView)
        
        // Setup control buttons
        findViewById<View>(R.id.btnStartNavigation).setOnClickListener {
            startNavigation()
        }
        
        findViewById<View>(R.id.btnStopNavigation).setOnClickListener {
            stopNavigation()
        }
        
        // Setup feature toggles
        findViewById<View>(R.id.cbVoiceInstructions).setOnCheckedChangeListener { _, isChecked ->
            toggleVoiceInstructions(isChecked)
        }
    }
    
    private fun requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
            .withListener(this)
            .check()
    }
    
    private fun setupMapbox() {
        // Initialize Mapbox Navigation
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        
        // Setup location engine
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        
        // Setup FusedLocationProviderClient
        fusedLocationClient = LocationProviderClient.getFusedLocationProviderClient(this)
        
        // Setup navigation camera
        navigationCamera = navigationView.retrieveNavigationCamera()
        
        // Setup route line
        routeLine = navigationView.retrieveRouteLine()
        
        // Setup route arrow
        routeArrow = navigationView.retrieveRouteArrow()
        
        // Setup voice controller
        voiceController = NavigationVoiceController(
            VoiceInstructionPlayer(this),
            mapboxNavigation
        )
        
        // Setup helper classes
        navigationHelper = NavigationHelper(
            this,
            mapboxNavigation,
            navigationCamera,
            routeLine,
            routeArrow,
            voiceController
        )
        
        uiComponentsManager = UIComponentsManager(
            this,
            findViewById(R.id.navigationInfoPanel),
            findViewById(R.id.maneuverInstruction),
            findViewById(R.id.tripDistance),
            findViewById(R.id.tripDuration),
            findViewById(R.id.speedLimit)
        )
        
        // Setup observers
        setupObservers()
        
        // Setup map
        setupMap()
    }
    
    private fun setupMap() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            Log.d(TAG, "Map style loaded")
            startLocationUpdates()
        }
    }
    
    private fun setupObservers() {
        navigationHelper.setupNavigationObservers(
            onLocationUpdate = { location ->
                currentLocation = location
                updateLocationStatus("Vị trí: ${location.latitude}, ${location.longitude}")
            },
            onRouteUpdate = { route ->
                updateRouteInfo(route)
            },
            onProgressUpdate = { routeProgress ->
                uiComponentsManager.updateTripProgress(routeProgress)
            },
            onVoiceInstruction = { voiceInstructions ->
                voiceController.play(voiceInstructions)
            }
        )
    }
    
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .build()
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            currentLocation = location
                            updateLocationStatus("Vị trí: ${location.latitude}, ${location.longitude}")
                            
                            if (!isNavigating) {
                                // Move camera to current location
                                navigationCamera.requestCameraLocationUpdate(location)
                            }
                        }
                    }
                },
                null
            )
        }
    }
    
    private fun startNavigation() {
        if (currentLocation == null) {
            Toast.makeText(this, "Chưa có vị trí hiện tại", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Example destination (Ho Chi Minh City)
        val destination = Point.fromLngLat(106.6297, 10.8231)
        val origin = Point.fromLngLat(currentLocation!!.longitude, currentLocation!!.latitude)
        
        // Calculate route using helper
        navigationHelper.calculateRoute(origin, destination) { route ->
            if (route != null) {
                navigationHelper.startNavigation(route)
                
                // Update UI
                findViewById<View>(R.id.btnStartNavigation).isEnabled = false
                findViewById<View>(R.id.btnStopNavigation).isEnabled = true
                uiComponentsManager.showNavigationInfo()
                
                Toast.makeText(this@MainActivity, "Điều hướng đã bắt đầu", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Lỗi tính toán tuyến đường", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    
    private fun stopNavigation() {
        navigationHelper.stopNavigation()
        
        // Update UI
        findViewById<View>(R.id.btnStartNavigation).isEnabled = true
        findViewById<View>(R.id.btnStopNavigation).isEnabled = false
        uiComponentsManager.hideNavigationInfo()
        
        Toast.makeText(this, "Điều hướng đã dừng", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleVoiceInstructions(enabled: Boolean) {
        navigationHelper.toggleVoiceInstructions(enabled)
        val message = if (enabled) "Hướng dẫn bằng giọng nói đã bật" else "Hướng dẫn bằng giọng nói đã tắt"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateLocationStatus(status: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.locationStatus).text = status
        }
    }
    
    private fun updateRouteInfo(route: DirectionsRoute) {
        val distance = route.distance()?.toInt() ?: 0
        val duration = route.duration()?.toInt() ?: 0
        
        runOnUiThread {
            uiComponentsManager.updateRouteInfo(distance.toDouble(), duration.toDouble())
        }
    }
    
    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        if (report.areAllPermissionsGranted()) {
            Log.d(TAG, "All permissions granted")
            setupMapbox()
        } else {
            Toast.makeText(this, "Cần cấp quyền để sử dụng ứng dụng", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
        token?.continuePermissionRequest()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation.onDestroy()
        locationEngine.removeLocationUpdates()
    }
}