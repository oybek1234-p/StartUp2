package com.example.market.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.lang.Exception

class LocationController(private val context: Context,val locationProvider: LocationProvider,val locationRequest: LocationRequest) : GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var googleApi: GoogleApiClient

    private var started: Boolean = false
    private var lastKnownLocation: Location?=null
    private var isGooglePlayServicesAvailable:Boolean?=null

     private var networkLocationListener: GpsCallbackListener = GpsCallbackListener()
     private var gpsLocationListener: GpsCallbackListener = GpsCallbackListener()
     private var passiveLocationListener: GpsCallbackListener = GpsCallbackListener()


    init {

        googleApi = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()


    }

    private inner class GpsCallbackListener : LocationListener{
        override fun onLocationChanged(location: Location) {

            if (lastKnownLocation!=null){
                if (!started && location.distanceTo(lastKnownLocation)>20){
                    setLastKnownLocation(location)
                }else{
                    setLastKnownLocation(location)
                }
            }
        }
    }

     private fun setLastKnownLocation(location: Location?){
         lastKnownLocation = location
         if(lastKnownLocation!=null){
             locationProvider.onLocationChanged(location!!)
         }
     }

    override fun onConnected(p0: Bundle?) {
        startFusedLocationRequest()
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
         started = false
    }
    @SuppressLint("MissingPermission")
    private fun startFusedLocationRequest(){
        try {
            setLastKnownLocation(LocationServices.FusedLocationApi.getLastLocation(googleApi))
      //      LocationServices.FusedLocationApi.requestLocationUpdates(googleApi,locationRequest,fusedLocationListener)
        }catch (e : Exception){
            if (e.message == null){
                return
            }
            Log.i("Exception",e.message!!)
        }
    }
    private fun isGooglePlayServicesAvailable(): Boolean{
        if (isGooglePlayServicesAvailable==null){
            val googlePlayServicesAvailability= GoogleApiAvailability()
            val result = googlePlayServicesAvailability.isGooglePlayServicesAvailable(context)
            isGooglePlayServicesAvailable = result == ConnectionResult.SUCCESS
        }
        return isGooglePlayServicesAvailable!!
    }

    fun start(){
        if (started){
            return
        }
        var ok = false
        started = true
        if (isGooglePlayServicesAvailable()){
            try {
                googleApi.connect()
                ok = true
            }catch (e : Exception){
                Log.i("Exception",e.message!!)
            }
        }
        if(!ok){
            try {
             locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 1, 0F,  gpsLocationListener)
            }catch (e: Exception){
                logException(e)
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 1, 0F,  networkLocationListener)
            }catch (e: Exception){
                logException(e)
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER , 1, 0F,  passiveLocationListener)
            }catch (e: Exception){
                logException(e)
            }
            if (lastKnownLocation==null){
                try {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation==null){
                        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastKnownLocation==null){
                            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                        }
                    }
                }catch (e: Exception){
                   logException(e)
                }
            }
        }

    }
    private fun logException(e: Exception){
        if (e.message!=null){
            Log.i("Exception",e.message!!)
        }
    }
    fun clear(){
        started = false
        if (isGooglePlayServicesAvailable()){
            if (googleApi.isConnected){

                googleApi.disconnect()
            }
        }
        locationManager.removeUpdates(gpsLocationListener)
        locationManager.removeUpdates(networkLocationListener)
            locationManager.removeUpdates(passiveLocationListener)
    }
    fun getLastKnownLocation(): Location?{
        return lastKnownLocation
    }
}
interface LocationProvider{
    fun onLocationChanged(location: Location)
}