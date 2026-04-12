package dam_A51597.coolweatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.URL

class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> get() = _weatherData

    val pressure = MutableLiveData<String>()
    val windDirection = MutableLiveData<String>()
    val windSpeed = MutableLiveData<String>()
    val temperature = MutableLiveData<String>()
    val time = MutableLiveData<String>()

    fun fetchWeatherData(latString: String, longString: String) {
        val lat = latString.toFloatOrNull() ?: return
        val long = longString.toFloatOrNull() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reqString = buildString {
                    append("https://api.open-meteo.com/v1/forecast?")
                    append("latitude=${lat}&longitude=${long}&")
                    append("current_weather=true&")
                    append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
                }
                val url = URL(reqString)
                url.openStream().use {
                    val request = Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)

                    _weatherData.postValue(request)
                    pressure.postValue("${request.hourly.pressure_msl[12]} hPa")
                    windDirection.postValue("${request.current_weather.winddirection}°")
                    windSpeed.postValue("${request.current_weather.windspeed} km/h")
                    temperature.postValue("${request.current_weather.temperature}°C")
                    time.postValue(request.current_weather.time)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}