package ru.mrrobot1413.distancetracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.databinding.FragmentPermissionBinding
import ru.mrrobot1413.distancetracker.misc.Permissions

class PermissionFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.continueButton?.setOnClickListener {
            if (Permissions.hasLocationPermission(requireContext())) {
                findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
            } else {
                Permissions.requestLocationPermission(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms[0])){
            SettingsDialog.Builder(requireActivity()).build().show()
        } else{
            Permissions.requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}