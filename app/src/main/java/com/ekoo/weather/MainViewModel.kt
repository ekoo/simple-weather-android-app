package com.ekoo.weather

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Services{

    @GET("locations/v1/cities/geoposition/search.json?")
    suspend fun getLocationKeyAsync(@Query(value = "q", encoded = true) latitudeLongitude: String, @Query("apikey") apiKey: String) : Location

    @GET("forecasts/v1/hourly/1hour/{locationKey}")
    suspend fun getOneHourForecastAsync(@Path(value = "locationKey", encoded = true) locationKey: String, @Query("apikey") apiKey: String): List<OneHourForecast>

    @GET("forecasts/v1/daily/1day/{locationKey}")
    suspend fun getOneDayForecastAsync(@Path(value = "locationKey", encoded = true) locationKey: String, @Query("apikey") apiKey: String): OneDayForecast

}

class MainViewModel : ViewModel(){

    //singleton retrofit instance
    companion object{
        private const val URL = "http://api.accuweather.com/"
        private const val API_KEY = "replace with your api key"

        fun getInstance(): Services {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(URL)
                .build()
                .create(Services::class.java)
        }
    }
    private val service = getInstance()

    val locationName = MutableLiveData<String>()
    val oneHourForecast = MutableLiveData<List<OneHourForecast>>()
    val oneDayForecast = MutableLiveData<OneDayForecast>()

    fun fetchData(latitudeLongitude: String) = viewModelScope.launch(Dispatchers.IO){
        val location = service.getLocationKeyAsync(latitudeLongitude, API_KEY)
        val locationKey = location.key

        locationName.postValue("${location.localizedName}, ${location.administrativeArea.localizedName}")
        oneHourForecast.postValue(service.getOneHourForecastAsync(locationKey, API_KEY))
        oneDayForecast.postValue(service.getOneDayForecastAsync(locationKey, API_KEY))
    }
}