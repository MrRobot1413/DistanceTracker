package ru.mrrobot1413.distancetracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrrobot1413.distancetracker.R
import ru.mrrobot1413.distancetracker.misc.Permissions

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)

        lifecycleScope.launch {
            delay(3000)
            navController.navigate(R.id.action_splashScreen_to_permissionFragment)
            if (Permissions.hasLocationPermission(this@MainActivity)) {
                val navOptions = NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim)
                    .setExitAnim(R.anim.nav_default_exit_anim)
                    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                    .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build()
                navController.navigate(
                    R.id.action_permissionFragment_to_mapsFragment,
                    null,
                    navOptions
                )
            }
        }
    }
}