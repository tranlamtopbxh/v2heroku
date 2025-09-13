package com.example.mapboxnavigation

import android.content.Context
import android.location.Location
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.route.RouteArrow
import com.mapbox.navigation.ui.maps.route.RouteLine
import com.mapbox.navigation.ui.voice.NavigationVoiceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NavigationHelper(
    private val context: Context,
    private val mapboxNavigation: MapboxNavigation,
    private val navigationCamera: NavigationCamera,
    private val routeLine: RouteLine,
    private val routeArrow: RouteArrow,
    private val voiceController: NavigationVoiceController
) {
    
    private var currentRoute: DirectionsRoute? = null
    private var isNavigating = false
    
    fun setupNavigationObservers(
        onLocationUpdate: (Location) -> Unit,
        onRouteUpdate: (DirectionsRoute) -> Unit,
        onProgressUpdate: (com.mapbox.navigation.core.trip.session.RouteProgress) -> Unit,
        onVoiceInstruction: (com.mapbox.navigation.core.trip.session.VoiceInstructions) -> Unit
    ) {
        // Location observer
        mapboxNavigation.registerLocationObserver(object : com.mapbox.navigation.core.location.LocationObserver {
            override fun onNewRawLocation(location: Location) {
                onLocationUpdate(location)
            }
            
            override fun onNewLocationMatcherResult(locationMatcherResult: com.mapbox.navigation.core.location.LocationMatcherResult) {
                // Handle location matching
            }
        })
        
        // Routes observer
        mapboxNavigation.registerRoutesObserver(object : RoutesObserver {
            override fun onRoutesChanged(routes: List<DirectionsRoute>) {
                if (routes.isNotEmpty()) {
                    currentRoute = routes.first()
                    onRouteUpdate(routes.first())
                }
            }
        })
        
        // Route progress observer
        mapboxNavigation.registerRouteProgressObserver(object : RouteProgressObserver {
            override fun onRouteProgressChanged(routeProgress: com.mapbox.navigation.core.trip.session.RouteProgress) {
                onProgressUpdate(routeProgress)
            }
        })
        
        // Voice instructions observer
        mapboxNavigation.registerVoiceInstructionsObserver(object : VoiceInstructionsObserver {
            override fun onNewVoiceInstructions(voiceInstructions: com.mapbox.navigation.core.trip.session.VoiceInstructions) {
                onVoiceInstruction(voiceInstructions)
            }
        })
    }
    
    fun calculateRoute(origin: Point, destination: Point, callback: (DirectionsRoute?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = com.mapbox.api.directions.v5.MapboxDirections.builder()
                    .accessToken(context.getString(R.string.mapbox_access_token))
                    .origin(origin)
                    .destination(destination)
                    .profile(com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING)
                    .build()
                    .executeCall()
                
                if (response.isSuccessful && response.body()?.routes()?.isNotEmpty() == true) {
                    val route = response.body()!!.routes()!![0]
                    callback(route)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    
    fun startNavigation(route: DirectionsRoute) {
        mapboxNavigation.setRoutes(listOf(route))
        mapboxNavigation.startTripSession()
        isNavigating = true
    }
    
    fun stopNavigation() {
        mapboxNavigation.stopTripSession()
        mapboxNavigation.setRoutes(emptyList())
        isNavigating = false
    }
    
    fun toggleVoiceInstructions(enabled: Boolean) {
        voiceController.mute(!enabled)
    }
    
    fun isCurrentlyNavigating(): Boolean = isNavigating
    
    fun getCurrentRoute(): DirectionsRoute? = currentRoute
}