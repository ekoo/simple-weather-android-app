package com.ekoo.weather

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ekoo.weather.databinding.ActivityMainBinding
import com.robin.locationgetter.EasyLocation
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.apply {
            locationName.observe(this@MainActivity) {
                binding.locationTv.text = it
            }
            oneHourForecast.observe(this@MainActivity) {
                binding.nameTv.text = it[0].iconPhase
                binding.currentTemperaturTv.text = "${it[0].temperature.value.toInt()}°F"

                val rawDate = it[0].dateTime.take(10)
                val oldFormat = SimpleDateFormat("yyyy-MM-dd")
                val date = oldFormat.parse(rawDate)
                val newFormat = SimpleDateFormat("dd-MMM-yyyy")

                if (date != null) {
                    binding.dateTv.text = newFormat.format(date)
                }

            }
            oneDayForecast.observe(this@MainActivity) {

                binding.lowTemperaturTv.text =
                    it.dailyForecasts[0].temperature.minimum.value.toString()
                binding.highTemperaturTv.text =
                    it.dailyForecasts[0].temperature.maximum.value.toString()

            }
        }

        EasyLocation(this, object : EasyLocation.EasyLocationCallBack{
            override fun getLocation(location: Location) {
                val latitudeLongitude = "${location.latitude},${location.longitude}"
                viewModel.fetchData(latitudeLongitude)
            }

            override fun locationSettingFailed() {
                TODO("Create Exception Handling")
            }

            override fun permissionDenied() {
                TODO("Create Exception Handling")
            }
        })

    }
}
