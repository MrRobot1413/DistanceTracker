package ru.mrrobot1413.distancetracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import ru.mrrobot1413.distancetracker.misc.Constants
import ru.mrrobot1413.distancetracker.misc.MapUtils
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->
                locations.forEach {
                    updateLocationList(it)
                    updateNotification()
                }
            }
        }
    }

    companion object {
        val started = MutableLiveData<Boolean>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()

        val locationList = MutableLiveData<MutableList<LatLng>>()
    }

    private fun setInitialValues() {
        started.value = false
        startTime.value = 0L
        stopTime.value = 0L

        locationList.value = mutableListOf()
    }

    private fun updateNotification() {
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText(locationList.value?.let { MapUtils.calculateDistance(it) } + "km")
        }
        notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
    }

    private fun updateLocationList(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.value = this
        }
    }

    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_SERVICE_START -> {
                    startService()
                    startLocationUpdates()
                    started.value = true
                }
                Constants.ACTION_SERVICE_STOP -> {
                    stopService()
                    started.value = false
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService() {
        createNotificationChannel()
        startForeground(Constants.NOTIFICATION_ID, notification.build())
    }

    private fun stopService() {
        removeLocationUpdates()
        stopForeground(Constants.NOTIFICATION_ID)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(Constants.NOTIFICATION_ID)
        stopSelf()
        stopTime.value = System.currentTimeMillis()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = Constants.LOCATION_UPDATE_INTERVAL
            fastestInterval = Constants.LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startTime.value = System.currentTimeMillis()
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}