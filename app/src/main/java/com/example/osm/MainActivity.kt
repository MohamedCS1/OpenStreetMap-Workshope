package com.example.osm

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.osm.Interfaces.OnLocationChangeListener
import com.example.osm.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.dynamic.IObjectWrapper
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.GroundOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.lang.reflect.Method


open class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var startPoint = GeoPoint(36.712051 ,3.113417)

        val map = findViewById<MapView>(R.id.mapView)

        val overlayArray = ArrayList<OverlayItem>()

        val overlay = object : GroundOverlay() {
            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                Toast.makeText(this@MainActivity ,"Single Click" ,Toast.LENGTH_SHORT).show()
                val thread = Thread {
                    try {
                        val roadManager: RoadManager = OSRMRoadManager(baseContext ,"userAgent")

                        val projection = mapView?.projection
                        val loc = projection?.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint

                        val waypoints = ArrayList<GeoPoint>()
                        waypoints.add(startPoint)

                        val endPoint = GeoPoint(loc.latitude ,loc.longitude)
                        waypoints.add(endPoint)



                        val road = roadManager.getRoad(waypoints)

                        val roadOverlay = RoadManager.buildRoadOverlay(road)


                        map.overlays.add(roadOverlay)


                        map.invalidate()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start();

                return super.onSingleTapConfirmed(e, mapView)
            }
        }

        map.overlays.add(overlay)
        map.invalidate()

        binding.myLocation.setOnClickListener {
            checkGpsPermission()
        }


        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setBuiltInZoomControls(true)


        val locationService = LocationService()

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))



        var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null

        locationService.onLocationChange(object :OnLocationChangeListener{
            override fun onLocationChange(long:String ,lat:String) {

                overlayArray.clear()

                startPoint = GeoPoint(lat.toDouble() ,long.toDouble())

                val marker = getDrawable(R.drawable.ic_location)
                anotherItemizedIconOverlay?.removeAllItems()
                val mapItem = OverlayItem("", "", GeoPoint(lat.toDouble() , long.toDouble()))
                mapItem.setMarker(marker)
                overlayArray.add(mapItem)
                if (anotherItemizedIconOverlay == null) {
                    anotherItemizedIconOverlay = ItemizedIconOverlay(applicationContext, overlayArray, null)
                    map.overlays.add(anotherItemizedIconOverlay)
                    map.invalidate()
                } else {
                    anotherItemizedIconOverlay = ItemizedIconOverlay(applicationContext, overlayArray, null)
                    map.overlays.add(anotherItemizedIconOverlay)
                    map.invalidate()
                }

            }
        })


    }


    fun dialogGps()
    {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)

        val locationSettingsResponse = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        locationSettingsResponse.addOnCompleteListener(object :OnCompleteListener<LocationSettingsResponse>{
            override fun onComplete(task: Task<LocationSettingsResponse>) {
                try {
                    task.getResult(ApiException::class.java)
                    Toast.makeText(this@MainActivity ,"Gps is already enable" ,Toast.LENGTH_SHORT).show()
                    startLocationService()
                }catch (e:ApiException){
                    if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                    {
                        val resolveApiException = e as ResolvableApiException

                        try {
                            resolveApiException.startResolutionForResult(this@MainActivity ,101)
                        }catch (ex:Exception)
                        {}
                    }
                    else if (e.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE)
                    {
                        Toast.makeText(this@MainActivity ,"Settings not available" ,Toast.LENGTH_SHORT).show()
                    }
                }


            }
        })
    }

     fun isLocationServiceRunning(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
         for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
             if (LocationService::class.java.name == service.service.className) {
                 if (service.foreground) {
                     return true
                 }
             }
         }
         return false
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

    private fun checkGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION).toString()) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }

    fun intentToSettings()
    {

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 101)
        {
            if (resultCode == RESULT_OK)
            {
                startLocationService()
                Toast.makeText(this@MainActivity ,"Now GPS is enable" ,Toast.LENGTH_SHORT).show()
            }
            if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this@MainActivity ,"Denied GPS enable" ,Toast.LENGTH_SHORT).show()
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                dialogGps()
            }
            else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                intentToSettings()
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}