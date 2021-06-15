package ru.mrrobot1413.distancetracker.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.databinding.FragmentMapsBinding
import ru.mrrobot1413.distancetracker.misc.Constants
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.disable
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.enable
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.hide
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.show
import ru.mrrobot1413.distancetracker.misc.MapUtils
import ru.mrrobot1413.distancetracker.misc.Permissions
import ru.mrrobot1413.distancetracker.models.Result
import ru.mrrobot1413.distancetracker.service.TrackerService

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap

    val started = MutableLiveData(false)

    private var startTime = 0L
    private var stopTime = 0L

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.tracking = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.apply {
            btnStart.setOnClickListener {
                if(Permissions.hasBackgroundLocationPermission(requireContext())){
                    lifecycleScope.launch {
                        binding.btnStart.hide()
                        binding.btnStop.show()
                        startTimer()
                    }
                } else{
                    Permissions.requestBackgroundLocationPermission(this@MapsFragment)
                }
            }

            btnReset.setOnClickListener {
                mapReset()
            }

            btnStop.setOnClickListener {
                stopForegroundService()
                binding.btnStop.hide()
                binding.btnStart.disable()
                binding.btnStart.show()
            }
        }
    }

    private fun startTimer(){
        binding.apply {
            txtTimer.show()
            btnStop.disable()
            btnStop.setBackgroundColor(Color.GRAY)
            btnStop.setTextColor(Color.parseColor("#c5c7c9"))
            val timer: CountDownTimer = object : CountDownTimer(4000, 1000){
                override fun onTick(millisUntilFinished: Long) {
                    val animation: Animation = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
                    animation.reset()
                    txtTimer.clearAnimation()
                    txtTimer.startAnimation(animation)

                    val seconds = millisUntilFinished / 1000
                    txtTimer.text = seconds.toString()
                    if(seconds.toString() == "0"){
                        txtTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        txtTimer.text = "GO"
                    }
                }

                override fun onFinish() {
                    binding.btnStart.hide()
                    sendActionCommandToService(Constants.ACTION_SERVICE_START)
                    txtTimer.hide()
                }

            }
            timer.start()
        }
    }

    private fun sendActionCommandToService(action: String){
        Intent(
            requireContext(),
            TrackerService::class.java
        ).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    private fun stopForegroundService(){
        sendActionCommandToService(Constants.ACTION_SERVICE_STOP)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }
        observerTrackerService()
    }

    private fun observerTrackerService(){
        TrackerService.locationList.observe(viewLifecycleOwner, {
            if(it != null){
                locationList = it
                drawPolyline()
                followUser()
                if(locationList.size >= 1){
                    binding.apply {
                        btnStop.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red
                            )
                        )
                        btnStop.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        btnStop.enable()
                    }
                }
            }
        })

        TrackerService.started.observe(viewLifecycleOwner, {
            started.value= it
        })
        TrackerService.startTime.observe(viewLifecycleOwner, {
            startTime = it
        })
        TrackerService.stopTime.observe(viewLifecycleOwner, {
            stopTime = it
            if(stopTime != 0L){
                showBiggerPicture()
                displayResults()
            }
        })
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        locationList.forEach {
            bounds.include(it)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
            1000, null
        )
    }

    private fun displayResults(){
        val result = Result(
            MapUtils.calculateDistance(locationList),
            MapUtils.calculateElapsedTime(startTime, stopTime)
        )
        lifecycleScope.launch {
            delay(2500)
            val directions = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(directions)
            binding.apply {
                binding.btnStart.apply {
                    hide()
                    enable()
                }
                btnStop.hide()
                binding.btnReset.show()
            }
        }
    }

    private fun mapReset(){
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            polylineList.forEach { polyline ->
                polyline.remove()
            }
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    MapUtils.setCameraPosition(lastKnownLocation)
                )
            )
            locationList.clear()
            binding.apply {
                btnReset.hide()
                btnStart.show()
            }
        }
    }

    private fun drawPolyline(){
        map.clear()
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(15f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    private fun followUser(){
        if(locationList.isNotEmpty()) {
            locationList.forEach {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(MapUtils.setCameraPosition(it)), 1000, null)
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.txtHint.animate().alpha(0f).duration = 500
        lifecycleScope.launch {
            delay(2000)
            binding.txtHint.hide()
            binding.btnStart.show()
        }
        return false
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms[0])){
            SettingsDialog.Builder(requireActivity()).build().show()
        } else{
            Permissions.requestBackgroundLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}