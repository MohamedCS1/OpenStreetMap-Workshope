package com.example.osm
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.example.osm.Interfaces.OnLocationChangeListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

var onLocationChangeListener:OnLocationChangeListener? = null
class LocationService : Service() {
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val latitude = locationResult.lastLocation.latitude
            val longitude = locationResult.lastLocation.longitude
            Toast.makeText(
                applicationContext,
                "Longitude -> $longitude Latitude -> $latitude", Toast.LENGTH_SHORT
            ).show()
            onLocationChangeListener?.onLocationChange(longitude.toString() ,latitude.toString())
//            locationResult.locations.last().bearing
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        val channelId = "location_notification_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()
        @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent = PendingIntent.getActivities(
            applicationContext, 0, arrayOf(resultIntent), PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Location Service")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setContentText("Running").priority = NotificationCompat.PRIORITY_MAX
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "this channel use by location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.setInterval(2000)
            .setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build())
    }

    fun stopLocationService() {
        stopSelf()
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet Implemented")
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            Toast.makeText(applicationContext, action, Toast.LENGTH_SHORT).show()
            if (action != null) {
                if (action == Constants.ACTION_START_LOCATION_SERVICE) {
                    startLocationService()
                } else if (action == Constants.ACTION_STOP_LOCATION_SERVICE) {
                    stopLocationService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun onLocationChange(onLocationChange: OnLocationChangeListener){
        onLocationChangeListener = onLocationChange
    }

}