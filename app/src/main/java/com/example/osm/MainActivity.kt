package com.example.osm

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.osm.Interfaces.OnLocationChangeListener
import com.example.osm.databinding.ActivityMainBinding
import com.example.osm.pojo.Road
import com.example.osm.pojo.Station
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


open class MainActivity : AppCompatActivity()  {

    lateinit var map:MapView
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var startPoint = GeoPoint(36.712051 ,3.113417)

        map = findViewById<MapView>(R.id.mapView)

        map.minZoomLevel = 6.5
        map.controller.setCenter(GeoPoint(36.712051 ,3.113417))

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

        val myLocationNewOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this) ,map)
        myLocationNewOverlay.enableMyLocation()

        binding.myLocation.setOnClickListener {
            checkGpsPermission()

            val personalIcon = ResourcesCompat.getDrawable(resources ,
                R.drawable.ic_my_location,null)?.toBitmap(100,100)
            myLocationNewOverlay.setDirectionIcon(personalIcon)
            myLocationNewOverlay.setPersonIcon(personalIcon)
            map.controller.zoomTo(10.0)
            map.controller.animateTo(GeoPoint(myLocationNewOverlay.mMyLocationProvider.lastKnownLocation.latitude , myLocationNewOverlay.mMyLocationProvider.lastKnownLocation.longitude.toDouble()))
            map.overlays.add(myLocationNewOverlay)
            map.invalidate()
        }

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setBuiltInZoomControls(true)


        val locationService = LocationService()

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        val overlayArray = ArrayList<OverlayItem>()
        var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null

        locationService.onLocationChange(object :OnLocationChangeListener{
            override fun onLocationChange(long:String ,lat:String ,bearing:Float) {

//                overlayArray.clear()
//
//                startPoint = GeoPoint(lat.toDouble() ,long.toDouble())
//
//                val marker = getDrawable(R.drawable.ic_location)
//                anotherItemizedIconOverlay?.removeAllItems()
//                val mapItem = OverlayItem("", "", GeoPoint(lat.toDouble() , long.toDouble()))
//                mapItem.setMarker(marker)
//                overlayArray.add(mapItem)
//                if (anotherItemizedIconOverlay == null) {
//                    anotherItemizedIconOverlay = ItemizedIconOverlay(applicationContext, overlayArray, null)
//                    map.overlays.add(anotherItemizedIconOverlay)
//                    map.invalidate()
//                } else {
//                    anotherItemizedIconOverlay = ItemizedIconOverlay(applicationContext, overlayArray, null)
//                    map.overlays.add(anotherItemizedIconOverlay)
//                    map.invalidate()
//                }

            }
        })

        initBusStations()
        initRoads()

    }



    val overlayArray = ArrayList<OverlayItem>()
    var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
    fun initBusStations()
    {
        val arrayStations = arrayListOf<Station>()
        val arrayStops = arrayListOf<Station>()

        arrayStops.add(Station("belcourt" , GeoPoint(36.75653238774928, 3.066262627281341)))
        arrayStops.add(Station("tafourah" , GeoPoint(36.76910861533059, 3.0597641216494975)))

        arrayStations.add(Station("5 Juillet" ,GeoPoint(36.72115900873567, 3.201872910092401)))
        arrayStations.add(Station("passerelle" ,GeoPoint(36.72376543158721, 3.198553628987393)))
        arrayStations.add(Station("soumam" ,GeoPoint(36.727063840783146, 3.1905424256315666)))
        arrayStations.add(Station("bab ezouar" ,GeoPoint(36.726308665008155, 3.1839130255019095)))
        arrayStations.add(Station("souk el fellah" ,GeoPoint(36.7260686198122, 3.17713211092541)))
        arrayStations.add(Station("tamaris" ,GeoPoint(36.73394318066509, 3.1717060943293536)))
        arrayStations.add(Station("diar djemaa" ,GeoPoint(36.72974897319716, 3.1280050553013887)))
        arrayStations.add(Station("bachjerrah" ,GeoPoint(36.72286943815615, 3.1158171471916623)))
        arrayStations.add(Station("bourouba" ,GeoPoint(36.71709061901567, 3.1070629911177794)))
        arrayStations.add(Station("fleuriste" ,GeoPoint(36.712888887113834, 3.093758976047992)))
        arrayStations.add(Station("l'poumpa" ,GeoPoint(36.708485085130405, 3.082088242933017)))
        arrayStations.add(Station("720" ,GeoPoint(36.707521501156435, 3.078999069697306)))
        arrayStations.add(Station("2eme arret" ,GeoPoint(36.70471926039495, 3.0750052568927986)))
        arrayStations.add(Station("ben oumar" ,GeoPoint(36.72637981350614, 3.0888088890657484)))
        arrayStations.add(Station("garidi" ,GeoPoint(36.72637981350614, 3.0888088890657484)))
        arrayStations.add(Station("la cote" ,GeoPoint(36.73317673170082, 3.050180684915832)))
        arrayStations.add(Station("ruisseau" ,GeoPoint(36.74283630833801, 3.0862451756195375)))
        arrayStations.add(Station("el annaser" ,GeoPoint(36.74386758797359, 3.0821157235362593)))
        arrayStations.add(Station("said hamdine" ,GeoPoint(36.73882350665709, 3.0364589235278525)))
        arrayStations.add(Station("ben aknon" ,GeoPoint(36.75800981395116, 3.002152015904051)))
        arrayStations.add(Station("chevally" ,GeoPoint(36.77286145134548, 3.0079912609359285)))
        arrayStations.add(Station("alger center" ,GeoPoint(36.77834394146806, 3.057442610879451)))

        for (station in arrayStations)
        {

            val marker = Marker(map)
            marker.title = station.title
            marker.snippet = "This is the snippet"
            marker.subDescription = "This is sub description"
            marker.icon = getDrawable(R.drawable.ic_bus_station)
            marker.position = GeoPoint(station.location.latitude , station.location.longitude)
            map.overlays.add(marker)
            map.invalidate()
            

        }

        for (stop in arrayStops)
        {
            val marker = Marker(map)
            marker.title = stop.title
            marker.snippet = "This is the snippet"
            marker.subDescription = "This is sub description"
            marker.icon = getDrawable(R.drawable.ic_bus_stop)
            marker.position = GeoPoint(stop.location.latitude , stop.location.longitude)
            map.overlays.add(marker)
            map.invalidate()

        }


    }

    fun initRoads()
    {
        val arrayRoads = arrayListOf<Road>()

        arrayRoads.add(Road(GeoPoint(36.74283630833801, 3.0862451756195375) ,GeoPoint(36.77834394146806, 3.057442610879451)))

        val roadManager: RoadManager = OSRMRoadManager(baseContext ,"userAgent")
        for (rod in arrayRoads)
        {
            val thread = Thread{
                val road = roadManager.getRoad(arrayListOf(rod.startPoint ,rod.endPoint))

                val roadOverlay = RoadManager.buildRoadOverlay(road ,Color.RED ,10f)
//                val rnd = Random()
//                val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
//                roadOverlay.color = color


                map.overlays.add(roadOverlay)

                runOnUiThread {
                    map.invalidate()
                }
            }
            thread.start()

        }

//        val projection = mapView?.projection




//        val loc = projection?.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint

//        val waypoints = ArrayList<GeoPoint>()
//        waypoints.add(startPoint)
//
//        val endPoint = GeoPoint(loc.latitude ,loc.longitude)
//        waypoints.add(endPoint)

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

    override fun onPause() {
        super<AppCompatActivity>.onPause()
    }

    override fun onResume() {
        super<AppCompatActivity>.onResume()
    }


}