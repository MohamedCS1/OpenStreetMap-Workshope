package com.example.osm


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.osm.Interfaces.OnLocationChangeListener
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.GroundOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem


open class MainActivity : AppCompatActivity(),MapEventsReceiver {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val overlayArray = ArrayList<OverlayItem>()

        val locationService = LocationService()

        startLocationService()


        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        val touchOverlay: Overlay = object : GroundOverlay() {
            var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
            override fun draw(arg0: Canvas?, arg1: MapView?, arg2: Boolean) {
                if (overlayArray.isNotEmpty()) {

                    //overlayItemArray have only ONE element only, so I hard code to get(0)
                    val `in`: GeoPoint = overlayArray[0].point as GeoPoint
                    val out = Point()
                    arg1!!.projection.toPixels(`in`, out)
                    val bm = BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_location
                    )
                    Canvas().drawBitmap(
                        bm,
                        (out.x - bm.width / 2).toFloat(),  //shift the bitmap center
                        (out.y - bm.height / 2).toFloat(),  //shift the bitmap center
                        null
                    )
                }
            }
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val marker = getDrawable(R.drawable.ic_location)


//                val mapItem = OverlayItem(
//                    "", "", GeoPoint(
//                        loc.latitudeE6.toDouble() / 1000000,
//                        loc.longitudeE6.toDouble() / 1000000
//                    )
//                )
//                mapItem.setMarker(marker)
//                overlayArray.add(mapItem)
//                if (anotherItemizedIconOverlay == null) {
//                    anotherItemizedIconOverlay =
//                        ItemizedIconOverlay(applicationContext, overlayArray, null)
//                    mapView.overlays.add(anotherItemizedIconOverlay)
//                    mapView.invalidate()
//                } else {
//                    mapView.invalidate()
//                    anotherItemizedIconOverlay =
//                        ItemizedIconOverlay(applicationContext, overlayArray, null)
//                    mapView.overlays.add(anotherItemizedIconOverlay)
//                }
                //      dlgThread();
                return true
            }
        }

        map.overlays.add(touchOverlay)
        locationService.onLocationChange(object :OnLocationChangeListener{
            override fun onLocationChange(long:String ,lat:String) {
                mapController.setCenter(GeoPoint(lat.toDouble(), long.toDouble()))

                mapController.setZoom(18.0)
                overlayArray.clear()
                overlayArray.add(OverlayItem("","",GeoPoint(lat.toDouble() ,long.toDouble())))
//                Toast.makeText(this@MainActivity ,geoLocation ,Toast.LENGTH_SHORT).show()
            }
        })


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