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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.databinding.FragmentMapsBinding
import ru.mrrobot1413.distancetracker.misc.Constans
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.disable
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.enable
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.hide
import ru.mrrobot1413.distancetracker.misc.ExtensionFunctions.show
import ru.mrrobot1413.distancetracker.misc.Permissions

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

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

            }

            btnStop.setOnClickListener {

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
                    sendActionCommandToService(Constans.ACTION_SERVICE_START)
                    txtTimer.hide()
                    btnStop.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                    btnStop.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    btnStop.enable()
                }

            }
            timer.start()
        }
    }

    private fun sendActionCommandToService(action: String){

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