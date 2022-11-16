package com.example.osm


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


class MainActivity : AppCompatActivity(),MapEventsReceiver {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(18.0)
        mapController.setCenter(GeoPoint(36.75299685157714, 3.0048393458673694))
        var touchOverlay: Overlay = object : Overlay(this) {
            var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
            override fun draw(arg0: Canvas?, arg1: MapView?, arg2: Boolean) {}
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val marker = applicationContext.resources.getDrawable(R.drawable.ic_launcher_background)
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
                    mapView.overlays.remove(anotherItemizedIconOverlay)
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

}