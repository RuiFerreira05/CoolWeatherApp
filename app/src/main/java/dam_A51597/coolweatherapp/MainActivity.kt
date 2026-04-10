package dam_A51597.coolweatherapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    val day = false

    override fun onCreate(savedInstanceState: Bundle?) {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) {
                    setTheme(R.style.Theme_Day)
                } else {
                    setTheme(R.style.Theme_Night)
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) {
                    setTheme(R.style.Theme_Day_Land)
                } else {
                    setTheme(R.style.Theme_Night_Land)
                }
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val lat: EditText = findViewById(R.id.latitude_edit)
        val long: EditText = findViewById(R.id.longitude_edit)
        val updateButton: Button = findViewById(R.id.update_button)
        updateButton.setOnClickListener {
            fetchWeatherData(lat.text.toString().toFloat(), long.text.toString().toFloat()).start()
        }
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }
        val url = URL(reqString);
        url.openStream().use {
            val request =
                Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
            return request
        }
    }

    private fun fetchWeatherData(lat: Float, long: Float): Thread {
        return Thread {
            val weather = WeatherAPI_Call(lat, long)
            updateUI(weather)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            val weatherImage: ImageView = findViewById(R.id.imageView)
            val pressure: TextView = findViewById(R.id.sea_text)
            val windDirection: TextView = findViewById(R.id.wind_dir_text)
            val windSpeed: TextView = findViewById(R.id.wind_speed_text)
            val temp: TextView = findViewById(R.id.temperature_text)
            val time: TextView = findViewById(R.id.time_text)

            pressure.text = request.hourly.pressure_msl[12].toString() + " hPa"
            windDirection.text = request.current_weather.winddirection.toString() + "°"
            windSpeed.text = request.current_weather.windspeed.toString() + " km/h"
            temp.text = request.current_weather.temperature.toString() + "°C"
            time.text = request.current_weather.time

            val mapt = getWeatherCodeMap();
            val wImage = when (val wCode = mapt[request.current_weather.weathercode]) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY -> if (day) wCode.image + "day" else wCode.image + "night"
                else -> wCode?.image
            }

            val res = getResources()
            val resID = res.getIdentifier(wImage, "drawable", packageName);
            val drawable = AppCompatResources.getDrawable(this, resID);
            weatherImage.setImageDrawable(drawable);

//            val res = getResources()
//            weatherImage.setImageResource(R.drawable.fog)
//            val resID = res.getIdentifier(wImage, " drawable ", getPackageName());
//            val drawable = this.getDrawable(resID);
//            weatherImage.setImageDrawable(drawable);
        }
    }
}