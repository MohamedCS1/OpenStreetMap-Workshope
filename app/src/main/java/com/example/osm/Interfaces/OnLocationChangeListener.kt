package com.example.osm.Interfaces

import android.location.GnssAntennaInfo

interface OnLocationChangeListener {
    fun onLocationChange(long:String ,lat:String ,bearing:Float)
}