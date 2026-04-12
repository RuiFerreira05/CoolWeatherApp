package dam_A51597.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import dam_A51597.coolweatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val day = false

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) setTheme(R.style.Theme_Day) else setTheme(R.style.Theme_Night)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) setTheme(R.style.Theme_Day_Land) else setTheme(R.style.Theme_Night_Land)
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.updateButton.setOnClickListener {
            val latStr = binding.latitudeEdit.text.toString()
            val longStr = binding.longitudeEdit.text.toString()
            viewModel.fetchWeatherData(latStr, longStr)
        }

        viewModel.weatherData.observe(this) { request ->
            val mapt = getWeatherCodeMap()
            val wImage = when (val wCode = mapt[request.current_weather.weathercode]) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY -> if (day) wCode.image + "day" else wCode.image + "night"
                else -> wCode?.image
            }

            val resID = resources.getIdentifier(wImage, "drawable", packageName)
            val drawable = AppCompatResources.getDrawable(this, resID)
            binding.imageView.setImageDrawable(drawable)
        }
    }
}