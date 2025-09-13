package com.example.mapboxnavigation

import android.content.Context
import android.view.View
import android.widget.TextView
import com.mapbox.navigation.core.trip.session.RouteProgress

class UIComponentsManager(
    private val context: Context,
    private val navigationInfoPanel: View,
    private val maneuverInstruction: TextView,
    private val tripDistance: TextView,
    private val tripDuration: TextView,
    private val speedLimit: TextView
) {
    
    fun showNavigationInfo() {
        navigationInfoPanel.visibility = View.VISIBLE
    }
    
    fun hideNavigationInfo() {
        navigationInfoPanel.visibility = View.GONE
    }
    
    fun updateManeuverInstruction(instruction: String) {
        maneuverInstruction.text = instruction
    }
    
    fun updateTripProgress(routeProgress: RouteProgress) {
        val distanceRemaining = routeProgress.distanceRemaining
        val durationRemaining = routeProgress.durationRemaining
        
        tripDistance.text = "${(distanceRemaining / 1000).toInt()} km"
        tripDuration.text = "${(durationRemaining / 60).toInt()} phút"
        
        // Update maneuver instruction
        val maneuver = routeProgress.currentLegProgress?.currentStepProgress?.step?.maneuver()
        maneuver?.instruction()?.let { instruction ->
            updateManeuverInstruction(instruction)
        }
    }
    
    fun updateSpeedLimit(speedLimitValue: Int?) {
        if (speedLimitValue != null && speedLimitValue > 0) {
            speedLimit.text = "${speedLimitValue} km/h"
            speedLimit.visibility = View.VISIBLE
        } else {
            speedLimit.visibility = View.GONE
        }
    }
    
    fun updateRouteInfo(distance: Double, duration: Double) {
        tripDistance.text = "${(distance / 1000).toInt()} km"
        tripDuration.text = "${(duration / 60).toInt()} phút"
    }
}