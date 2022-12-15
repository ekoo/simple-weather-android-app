package com.ekoo.weather

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    val locationName = MutableLiveData<String>()
    val oneHourForecast = MutableLiveData<OneHourForecast>()
    val oneDayForecast = MutableLiveData<OneDayForecast.DailyForecast>()
    val status = MutableLiveData<Status>()
    var fetchJob: Job? = null


    private val client = HttpClient(CIO) {
        expectSuccess = true

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.accuweather.com"
                parameters.append("apikey", BuildConfig.API_KEY)
            }
        }

        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                setLenient()
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                }
            }
            level = LogLevel.ALL
        }
    }

    fun fetchData(task: Task<android.location.Location>, exception: (Exception) -> Unit) {
        if (fetchJob?.isActive == true) fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            status.postValue(Status.LOADING)
            try {
                val location = task.await()
                val queryLocation = "${location.latitude},${location.longitude}"
                val geoPosition = getLocation(queryLocation).body<Location>()

                val locationKey = geoPosition.key
                val oneHour = async { getOneHourForecast(locationKey) }
                val oneDay = async { getOneDayForecast(locationKey) }

                locationName.postValue(geoPosition.localizedName)
                oneHourForecast.postValue(oneHour.await().first())
                oneDayForecast.postValue(oneDay.await().dailyForecasts.first())
                status.postValue(Status.SUCCESS)
            } catch (e: Exception) {
                status.postValue(Status.ERROR)
                withContext(Dispatchers.Main) {
                    exception(e)
                }
            }
        }
    }

    val preference = getApplication<Application>()
        .dataStore
        .data
        .asLiveData(viewModelScope.coroutineContext)

    fun getCurrentTheme(result: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val theme = getApplication<Application>()
                .dataStore
                .data
                .firstOrNull()?.get(intPreferencesKey("theme")) ?: 0
            withContext(Dispatchers.Main) {
                result(theme)
            }
        }
    }

    fun setCurrentTheme(theme: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().dataStore.edit {
                it[intPreferencesKey("theme")] = theme
            }
        }
    }

    private suspend fun getLocation(geoPosition: String) = client
        .get("locations/v1/cities/geoposition/search.json") {
            parameter("q", geoPosition)
        }

    private suspend fun getOneHourForecast(locationKey: String) = client
        .get("forecasts/v1/hourly/1hour/$locationKey").body<List<OneHourForecast>>()

    private suspend fun getOneDayForecast(locationKey: String) = client
        .get("forecasts/v1/daily/1day/$locationKey").body<OneDayForecast>()
}