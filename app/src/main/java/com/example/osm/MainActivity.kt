package com.example.osm


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem


open class MainActivity : AppCompatActivity(),MapEventsReceiver {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startLocationService()

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(18.0)
        mapController.setCenter(GeoPoint(36.75299685157714, 3.0048393458673694))
        val touchOverlay: Overlay = object : Overlay(this) {
            var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
            override fun draw(arg0: Canvas?, arg1: MapView?, arg2: Boolean) {}
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val marker = getDrawable(R.drawable.ic_location)
                val proj = mapView.projection
                val loc = proj.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                val longitude = java.lang.Double.toString(loc.longitudeE6.toDouble() / 1000000)
                val latitude = java.lang.Double.toString(loc.latitudeE6.toDouble() / 1000000)

                Toast.makeText(baseContext,longitude+""+latitude,Toast.LENGTH_SHORT).show()

                Log.d("CurrentLocation",longitude+""+latitude)
                val overlayArray = ArrayList<OverlayItem>()
                val mapItem = OverlayItem(
                    "", "", GeoPoint(
                        loc.latitudeE6.toDouble() / 1000000,
                        loc.longitudeE6.toDouble() / 1000000
                    )
                )
                mapItem.setMarker(marker)
                overlayArray.add(mapItem)
                if (anotherItemizedIconOverlay == null) {
                    anotherItemizedIconOverlay =
                        ItemizedIconOverlay(applicationContext, overlayArray, null)
                    mapView.overlays.add(anotherItemizedIconOverlay)
                    mapView.invalidate()
                } else {
                    mapView.invalidate()
                    anotherItemizedIconOverlay =
                        ItemizedIconOverlay(applicationContext, overlayArray, null)
                    mapView.overlays.add(anotherItemizedIconOverlay)
                }
                //      dlgThread();
                return true
            }
        }

        map.overlays.add(touchOverlay)

    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        Toast.makeText(this,p?.latitude.toString()+""+p?.longitude.toString(),Toast.LENGTH_SHORT).show()
        return false
    }
     fun isLocationServiceRunning(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager != null) {
            for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
                if (LocationService::class.java.name == service.service.className) {
                    if (service.foreground) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            Toast.makeText(this, "Start location service", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LocationService::class.java)
            intent.action = Constants.ACTION_START_LOCATION_SERVICE
            startService(intent)
        }
    }

    fun stopLocationService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        intent.action = Constants.ACTION_STOP_LOCATION_SERVICE
        applicationContext.startService(intent)
    }
}