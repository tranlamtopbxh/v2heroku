package com.gpsdev.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.maps.NavigationView
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), PermissionsListener {

    // Mapbox components
    private lateinit var mapView: MapView
    private lateinit var navigationView: NavigationView
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var navigationLocationProvider: NavigationLocationProvider
    
    // Route components
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    
    // Voice components
    private lateinit var speechApi: MapboxSpeechApi
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer
    
    // UI components
    private lateinit var btnSearch: MaterialButton
    private lateinit var btnVoiceToggle: MaterialButton
    private lateinit var btnStartNavigation: MaterialButton
    private lateinit var btnCenterLocation: MaterialButton
    private lateinit var tripProgressCard: View
    private lateinit var progressBar: View
    
    // Trip info views
    private lateinit var tvDistance: android.widget.TextView
    private lateinit var tvDuration: android.widget.TextView
    private lateinit var tvETA: android.widget.TextView
    private lateinit var tvSpeed: android.widget.TextView
    private lateinit var tvTripStatus: android.widget.TextView
    
    // Location and navigation state
    private var currentLocation: Location? = null
    private var isNavigationActive = false
    private var isVoiceEnabled = true
    private var destination: Point? = null
    
    // Permissions
    private lateinit var permissionsManager: PermissionsManager
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                initializeLocationServices()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                initializeLocationServices()
            }
            else -> {
                Toast.makeText(this, "Cần quyền truy cập vị trí để sử dụng ứng dụng", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        checkPermissions()
    }
    
    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        navigationView = findViewById(R.id.navigationView)
        btnSearch = findViewById(R.id.btnSearch)
        btnVoiceToggle = findViewById(R.id.btnVoiceToggle)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        btnCenterLocation = findViewById(R.id.btnCenterLocation)
        tripProgressCard = findViewById(R.id.tripProgressCard)
        progressBar = findViewById(R.id.progressBar)
        
        tvDistance = findViewById(R.id.tvDistance)
        tvDuration = findViewById(R.id.tvDuration)
        tvETA = findViewById(R.id.tvETA)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvTripStatus = findViewById(R.id.tvTripStatus)
    }
    
    private fun setupClickListeners() {
        btnSearch.setOnClickListener {
            showSearchDialog()
        }
        
        btnVoiceToggle.setOnClickListener {
            toggleVoiceGuidance()
        }
        
        btnStartNavigation.setOnClickListener {
            if (isNavigationActive) {
                stopNavigation()
            } else {
                startNavigation()
            }
        }
        
        btnCenterLocation.setOnClickListener {
            centerOnCurrentLocation()
        }
    }
    
    private fun checkPermissions() {
        permissionsManager = PermissionsManager(this)
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationServices()
        } else {
            permissionsManager.requestLocationPermissions(this)
        }
    }
    
    private fun initializeLocationServices() {
        // Initialize Mapbox Navigation
        val navigationOptions = NavigationOptions.Builder(this)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()
        
        MapboxNavigationApp.setup(navigationOptions)
        mapboxNavigation = MapboxNavigationApp.current()
        
        // Initialize location provider
        navigationLocationProvider = NavigationLocationProvider()
        
        // Initialize route line components
        val routeLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(
                RouteLineResources.Builder()
                    .routeLineColorResources(
                        RouteLineColorResources.Builder()
                            .routeLowCongestionColor(ContextCompat.getColor(this, R.color.route_traffic_light))
                            .routeModerateCongestionColor(ContextCompat.getColor(this, R.color.route_traffic_medium))
                            .routeHeavyCongestionColor(ContextCompat.getColor(this, R.color.route_traffic_heavy))
                            .routeSevereCongestionColor(ContextCompat.getColor(this, R.color.route_traffic_heavy))
                            .build()
                    )
                    .build()
            )
            .build()
        
        routeLineApi = MapboxRouteLineApi(routeLineOptions)
        routeLineView = MapboxRouteLineView(routeLineOptions)
        
        // Initialize voice components
        speechApi = MapboxSpeechApi(this, getString(R.string.mapbox_access_token))
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(this, getString(R.string.mapbox_access_token))
        
        // Setup observers
        setupNavigationObservers()
        
        // Initialize map
        initializeMap()
        
        // Start location updates
        startLocationUpdates()
    }
    
    private fun initializeMap() {
        mapView.getMapboxMap().loadStyle(
            style(Style.MAPBOX_STREETS) {
                // Map style loaded
            }
        )
    }
    
    private fun setupNavigationObservers() {
        // Location observer
        mapboxNavigation.registerLocationObserver(object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {
                navigationLocationProvider.changePosition(rawLocation)
                currentLocation = rawLocation
                updateSpeedDisplay(rawLocation.speed)
            }
            
            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhancedLocation = locationMatcherResult.enhancedLocation
                navigationLocationProvider.changePosition(enhancedLocation)
                currentLocation = enhancedLocation
                updateSpeedDisplay(enhancedLocation.speed)
            }
        })
        
        // Route progress observer
        mapboxNavigation.registerRouteProgressObserver(object : RouteProgressObserver {
            override fun onRouteProgressChanged(routeProgress: com.mapbox.navigation.core.trip.session.RouteProgress) {
                updateTripProgress(routeProgress)
            }
        })
        
        // Voice instructions observer
        mapboxNavigation.registerVoiceInstructionsObserver(object : VoiceInstructionsObserver {
            override fun onNewVoiceInstructions(voiceInstructions: com.mapbox.navigation.core.trip.session.VoiceInstructions) {
                if (isVoiceEnabled) {
                    speechApi.generate(voiceInstructions, object : com.mapbox.navigation.ui.voice.api.SpeechCallback {
                        override fun onDone(speechValue: SpeechValue, isMuted: Boolean) {
                            if (!isMuted) {
                                voiceInstructionsPlayer.play(speechValue.announcement)
                            }
                        }
                        
                        override fun onError(speechError: SpeechError, speechValue: SpeechValue) {
                            // Handle voice error
                        }
                    })
                }
            }
        })
        
        // Routes observer
        mapboxNavigation.registerRoutesObserver(object : RoutesObserver {
            override fun onRoutesChanged(routes: List<com.mapbox.navigation.base.route.NavigationRoute>) {
                if (routes.isNotEmpty()) {
                    val route = routes.first()
                    drawRoute(route)
                }
            }
        })
    }
    
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapboxNavigation.startTripSession()
        }
    }
    
    private fun showSearchDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Tìm kiếm địa điểm")
            .setMessage("Nhập địa điểm bạn muốn đến:")
            .setView(android.widget.EditText(this).apply {
                hint = "Ví dụ: Hồ Gươm, Hà Nội"
            })
            .setPositiveButton("Tìm kiếm") { dialog, _ ->
                // TODO: Implement search functionality
                Toast.makeText(this, "Tính năng tìm kiếm đang được phát triển", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun toggleVoiceGuidance() {
        isVoiceEnabled = !isVoiceEnabled
        btnVoiceToggle.text = if (isVoiceEnabled) "Tắt giọng nói" else "Bật giọng nói"
        Toast.makeText(this, if (isVoiceEnabled) "Đã bật hướng dẫn bằng giọng nói" else "Đã tắt hướng dẫn bằng giọng nói", Toast.LENGTH_SHORT).show()
    }
    
    private fun startNavigation() {
        if (destination == null) {
            Toast.makeText(this, "Vui lòng chọn điểm đến trước", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (currentLocation == null) {
            Toast.makeText(this, "Đang tìm vị trí hiện tại...", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        calculateRoute(currentLocation!!, destination!!)
    }
    
    private fun stopNavigation() {
        isNavigationActive = false
        mapboxNavigation.stopTripSession()
        btnStartNavigation.text = getString(R.string.navigation_start)
        tripProgressCard.visibility = View.GONE
        Toast.makeText(this, "Đã dừng điều hướng", Toast.LENGTH_SHORT).show()
    }
    
    private fun centerOnCurrentLocation() {
        currentLocation?.let { location ->
            val point = Point.fromLngLat(location.longitude, location.latitude)
            val cameraOptions = CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
            mapView.getMapboxMap().setCamera(cameraOptions)
        } ?: run {
            Toast.makeText(this, "Không tìm thấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun calculateRoute(origin: Location, destination: Point) {
        val originPoint = Point.fromLngLat(origin.longitude, origin.latitude)
        
        val routeOptions = RouteOptions.builder()
            .coordinatesList(listOf(originPoint, destination))
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .steps(true)
            .voiceInstructions(true)
            .bannerInstructions(true)
            .build()
        
        val client = MapboxDirections.builder()
            .routeOptions(routeOptions)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()
        
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val routes = response.body()?.routes()
                    if (!routes.isNullOrEmpty()) {
                        val route = routes.first()
                        val navigationRoute = com.mapbox.navigation.base.route.NavigationRoute(route, routeOptions)
                        mapboxNavigation.setNavigationRoutes(listOf(navigationRoute))
                        isNavigationActive = true
                        btnStartNavigation.text = getString(R.string.navigation_stop)
                        tripProgressCard.visibility = View.VISIBLE
                        Toast.makeText(this@MainActivity, "Đã bắt đầu điều hướng", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Không tìm thấy tuyến đường", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Lỗi khi tính toán tuyến đường", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun drawRoute(navigationRoute: com.mapbox.navigation.base.route.NavigationRoute) {
        lifecycleScope.launch {
            val routeLine = RouteLine(navigationRoute.route, null)
            val routeDrawData = routeLineApi.setRouteLine(listOf(routeLine))
            routeLineView.renderRouteDrawData(mapView.getMapboxMap().getStyle()!!, routeDrawData)
        }
    }
    
    private fun updateTripProgress(routeProgress: com.mapbox.navigation.core.trip.session.RouteProgress) {
        val distanceRemaining = routeProgress.distanceRemaining
        val durationRemaining = routeProgress.durationRemaining
        val currentLegProgress = routeProgress.currentLegProgress
        
        // Update distance
        val distanceKm = distanceRemaining / 1000.0
        tvDistance.text = String.format("%.1f km", distanceKm)
        
        // Update duration
        val durationMinutes = durationRemaining / 60.0
        tvDuration.text = String.format("%.0f phút", durationMinutes)
        
        // Update ETA
        val eta = System.currentTimeMillis() + durationRemaining * 1000
        val etaTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(eta)
        tvETA.text = etaTime
        
        // Update status
        currentLegProgress?.currentStepProgress?.step?.maneuver()?.instruction()?.let { instruction ->
            tvTripStatus.text = instruction
        }
    }
    
    private fun updateSpeedDisplay(speed: Float) {
        val speedKmh = speed * 3.6f // Convert m/s to km/h
        tvSpeed.text = String.format("%.0f km/h", speedKmh)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Ứng dụng cần quyền truy cập vị trí để hoạt động", Toast.LENGTH_LONG).show()
    }
    
    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            initializeLocationServices()
        } else {
            Toast.makeText(this, "Không thể sử dụng ứng dụng mà không có quyền truy cập vị trí", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation.unregisterLocationObserver(this)
        mapboxNavigation.unregisterRouteProgressObserver(this)
        mapboxNavigation.unregisterVoiceInstructionsObserver(this)
        mapboxNavigation.unregisterRoutesObserver(this)
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }
}