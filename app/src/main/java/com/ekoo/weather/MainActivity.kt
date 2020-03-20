package com.ekoo.weather

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.robin.locationgetter.EasyLocation
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.apply {
            locationName.observe(this@MainActivity, Observer {
                location_tv.text = it
            })
            oneHourForecast.observe(this@MainActivity, Observer {
                name_tv.text = it[0].iconPhase
                current_temperatur_tv.text = "${it[0].temperature.value.toInt()}°F"

                val rawDate = it[0].dateTime.take(10)
                val oldFormat = SimpleDateFormat("yyyy-MM-dd")
                val date= oldFormat.parse(rawDate)
                val newFormat = SimpleDateFormat("dd-MMM-yyyy")

                if (date != null){
                    date_tv.text = newFormat.format(date)
                }

            })
            oneDayForecast.observe(this@MainActivity, Observer {

                low_temperatur_tv.text = it.dailyForecasts[0].temperature.minimum.value.toString()
                high_temperatur_tv.text = it.dailyForecasts[0].temperature.maximum.value.toString()

            })
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
