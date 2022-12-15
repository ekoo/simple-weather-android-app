package com.ekoo.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.intPreferencesKey
import com.ekoo.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val locationPermission = registerForActivityResult(RequestMultiplePermissions()) {
        if (!it.containsValue(true)) {
            showToast("Permission denied")
            finish()
            return@registerForActivityResult
        }

        fetchData()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.scrollView.isVisible = false
        viewModel.preference.observe(this) {
            val theme = it[(intPreferencesKey("theme"))] ?: 0
            changeTheme(theme)
        }

        binding.toolbar.apply {
            inflateMenu(R.menu.main_menu)
            setNavigationOnClickListener {
                viewModel.getCurrentTheme { currentTheme ->
                    showChangeThemeDialog(currentTheme)
                }
            }
            setOnMenuItemClickListener {
                fetchData()
                true
            }
        }
        viewModel.locationName.observe(this@MainActivity) {
            binding.locationTv.text = it
        }
        viewModel.oneHourForecast.observe(this) {
            binding.nameTv.text = it.iconPhase
            binding.currentTemperaturTv.text = "${it.temperature.value.toInt()}°F"
        }
        viewModel.oneDayForecast.observe(this) {
            binding.lowTemperaturTv.text = "${it.temperature.minimum.value}°"
            binding.highTemperaturTv.text = "${it.temperature.maximum.value}°"
        }
        viewModel.status.observe(this) { status ->
            binding.progressBar.isVisible = status == Status.LOADING
            binding.scrollView.isVisible = status == Status.SUCCESS
        }

        if (!isLocationPermissionGranted()) {
            showRequestPermissionDialog()
            return
        }

        if (isDataAlreadyFetched()) return

        fetchData()
    }

    private fun showChangeThemeDialog(currentTheme: Int) {
        var newTheme: Int? = null

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(arrayOf("System", "Dark", "Light"), currentTheme) { _, which ->
                newTheme = which
            }
            .setPositiveButton("Save") { _, _ ->
                viewModel.setCurrentTheme(newTheme ?: return@setPositiveButton)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    private fun showRequestPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Permission Required")
            .setCancelable(false)
            .setMessage("This app requires location permission to show weather data")
            .setPositiveButton("Ok") { _, _ ->
                locationPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                showToast("Permission denied")
                finish()
            }
            .show()
    }

    private fun isDataAlreadyFetched(): Boolean {
        return viewModel.fetchJob?.isActive == true || viewModel.oneHourForecast.value != null
    }

    @SuppressLint("MissingPermission")
    private fun fetchData() {
        if (!isGpsEnable()) {
            showToast("Please enable GPS")
            return
        }

        val locationTask = LocationServices
            .getFusedLocationProviderClient(this)
            .getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)

        viewModel.fetchData(locationTask, this::handleException)
    }

    private fun isGpsEnable(): Boolean {
        return (getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun handleException(exception: Exception) {
        showToast(exception.message ?: "Something went wrong")
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
