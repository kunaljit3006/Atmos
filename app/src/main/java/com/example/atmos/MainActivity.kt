package com.example.atmos

import android.graphics.Color
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.atmos.Interface.ApiInterface
import com.example.atmos.api.WeatherApp
import com.example.atmos.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding :ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchWeatherData("Silchar")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.searchbar

        // Style the internal EditText (typed text + hint)
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.BLACK)       // typed text color
        searchText.setHintTextColor(Color.GRAY)    // hint color
        searchView.queryHint = "Search for a City"

        // Set custom search icon
        val searchButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchButton.setImageResource(R.drawable.search)

        // Make SearchView clickable and expand on click
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT)
        }

        // Listener for submit and text change
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)   // your function to fetch weather data
                }
                searchView.clearFocus()       // hide keyboard
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: handle live text change
                return true
            }
        })
    }




    private fun fetchWeatherData(cityName:String) {
        val retrofit= Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
         val response= retrofit.getWeatherData(cityName, "a9ba6abd21eccf7b3916fadae190ee91" , "metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody= response.body()
                if (response.isSuccessful && responseBody!=null){
                    val temp =responseBody.main.temp
                    val humidity=responseBody.main.humidity
                    val windspeed=responseBody.wind.speed
                    val sunRise=responseBody.sys.sunrise.toLong()
                    val sunSet=responseBody.sys.sunset.toLong()
                    val seaLevel=responseBody.main.pressure
                    val condition= responseBody.weather.firstOrNull()?.main?:"unknown"
                    val  maxTemp=responseBody.main.temp_max
                    val  mintemp=responseBody.main.temp_min
                            binding.temp.text="$temp °C"
                            binding.weather.text=condition
                            binding.max.text="Max temp: $maxTemp °C"
                            binding.min.text="Min temp: $mintemp °C"
                           binding.humidity.text="$humidity %"
                           binding.wind.text="$windspeed m/s"
                           binding.sunrise.text="${time(sunRise)}"
                    binding.sunset.text="${time(sunSet)}"
                    binding.sealevel.text="$seaLevel hPa"
                    binding.condition.text=condition
                    binding.day.text=dayName(System.currentTimeMillis())
                        binding.date.text=date()
                        binding.city.text="$cityName"

                    //Log.d("TAG", "onResponse: $temp ")
                    changebg(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })


        }

    fun changebg(condition:String){
        when(condition){
            "Haze"->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain","Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            // ... (Possible 'else' branch or other conditions not fully visible)

           else ->{
               binding.root.setBackgroundResource(R.drawable.sunny_background)
               binding.lottieAnimationView.setAnimation(R.raw.sun)
           }
        }

        binding.lottieAnimationView.playAnimation()
    }
    fun dayName(timestamp:Long):String{
        val sdf=SimpleDateFormat("EEEE",Locale.getDefault())
        return sdf.format((Date()))
    } fun time(timestamp:Long):String{
        val sdf=SimpleDateFormat("HH:mm",Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }
    fun date():String{
        val sdf=SimpleDateFormat("dd MMMM yyyy",Locale.getDefault())
        return sdf.format(Date())
    }
}
