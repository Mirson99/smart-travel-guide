package com.example.smarttravelguide

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.ConfigurationCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.smarttravelguide.databinding.ActivityMapsBinding
import com.example.smarttravelguide.ui.auth.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var WEATHER_URL = ""
    private var API_KEY = "36e1b8a9265f4b2c83c5136e7cd30a8a"

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private lateinit var tvCurrentAddress: TextView
    private lateinit var button: Button

    private var end_latitude = 0.0
    private var end_longitude = 0.0
    var origin: MarkerOptions? = null
    var destination: MarkerOptions? = null
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authViewModel: AuthViewModel by viewModels()

        val logoutButton = findViewById<Button>(R.id.logout_button)

        logoutButton.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        val userButton = findViewById<ImageButton>(R.id.user_button)

        userButton.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        tvCurrentAddress = findViewById<TextView>(R.id.tvAdd)
        button = findViewById<Button>(R.id.B_search)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationUser()


        // this function will, fetch data from URL



        button.setOnClickListener {
            searchArea()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun searchArea() {
        val tf_location = findViewById<View>(R.id.TF_location) as EditText
        val location = tf_location.text.toString()
        var addressList: List<Address>? = null
        val markerOptions = MarkerOptions()
        Log.d("location = ", location)
        if (location != "") {
            val geocoder = Geocoder(applicationContext)
            try {
                addressList = geocoder.getFromLocationName(location, 5)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (addressList != null) {
                for (i in addressList.indices) {
                    val myAddress = addressList[i]
                    val latLng = LatLng(myAddress.latitude, myAddress.longitude)
                    markerOptions.position(latLng)
                    mMap.addMarker(markerOptions)
                    end_latitude = myAddress.latitude
                    end_longitude = myAddress.longitude
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    val mo = MarkerOptions()
                    mo.title("Distance")
                    val results = FloatArray(10)
                    Location.distanceBetween(
                        latitude,
                        longitude,
                        end_latitude,
                        end_longitude,
                        results
                    )

                    val s = String.format("%.1f", results[0]/1000)

                    origin = MarkerOptions().position(LatLng(latitude, longitude))
                        .title("HSR Layout").snippet("origin")
                    destination =
                        MarkerOptions().position(LatLng(end_latitude, end_longitude))
                            .title(tf_location.text.toString())
                            .snippet("Distance = $s KM")
                    mMap.addMarker(destination!!)
                    Toast.makeText(
                        this@MapsActivity,
                        "Distance = $s KM",
                        Toast.LENGTH_LONG
                    ).show()

                    tvCurrentAddress.text = "Distance = $s KM"
                }

            }
        }
    }

    private fun getCurrentLocationUser() {
        if(ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }

        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location ->
            if (location != null) {
                currentLocation = location

                latitude = currentLocation.latitude
                longitude = currentLocation.longitude


                Toast.makeText(applicationContext, "Your current location: " +
                        currentLocation.latitude.toString() + "" +
                currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()


                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

                getWeatherInformation()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                 getCurrentLocationUser()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
        googleMap.addMarker(markerOptions)
    }


    fun showWeatherInformation(jsonWeather: String) {
        val obj = JSONObject(jsonWeather)
        val arr = obj.getJSONArray("data")  // weather info is in the array called data
        var objData = arr.getJSONObject(0)  // get position 0 of the array
        ConfigurationCompat.getLocales(resources.configuration)[0]
        Log.e("TAG", "Data: $arr")


        val objWeather = objData.getJSONObject("weather")

        //Forecast day 1
        objData = arr.getJSONObject(0)  // get position 1 of the array
        "${objData.getString("temp")}??".also { findViewById<TextView>(R.id.textDay1Temp).text = it }
        var imageIconCode = objWeather.getString("icon")
        var drawableResourseId =
            this.resources.getIdentifier(imageIconCode, "drawable", this.packageName)
        findViewById<ImageView>(R.id.imageWeatherIconDay1).setImageResource(drawableResourseId)
        var myDate = objData.getString("datetime")
        var date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(myDate)
        var calendar = Calendar.getInstance()
        calendar.time = date!!
        var formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        var dayWeek = formatter.format(calendar.time)
        Log.e("TAG", "getWeekDay: $dayWeek")
        findViewById<TextView>(R.id.textDay1).text = dayWeek

        //Forecast day 2
        objData = arr.getJSONObject(1)  // get position 1 of the array
        "${objData.getString("temp")}??".also { findViewById<TextView>(R.id.textDay2Temp).text = it }
        imageIconCode = objWeather.getString("icon")
        drawableResourseId =
            this.resources.getIdentifier(imageIconCode, "drawable", this.packageName)
        findViewById<ImageView>(R.id.imageWeatherIconDay2).setImageResource(drawableResourseId)
        myDate = objData.getString("datetime")
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(myDate)
        calendar = Calendar.getInstance()
        calendar.time = date!!
        formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        dayWeek = formatter.format(calendar.time)
        Log.e("TAG", "getWeekDay: $dayWeek")
        findViewById<TextView>(R.id.textDay2).text = dayWeek


        //Forecast day 3
        objData = arr.getJSONObject(2)  // get position 1 of the array
        "${objData.getString("temp")}??".also { findViewById<TextView>(R.id.textDay3Temp).text = it }
        imageIconCode = objWeather.getString("icon")
        drawableResourseId =
            this.resources.getIdentifier(imageIconCode, "drawable", this.packageName)
        findViewById<ImageView>(R.id.imageWeatherIconDay3).setImageResource(drawableResourseId)
        myDate = objData.getString("datetime")
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(myDate)
        calendar = Calendar.getInstance()
        calendar.time = date!!
        formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        dayWeek = formatter.format(calendar.time)
        Log.e("TAG", "getWeekDay: $dayWeek")
        findViewById<TextView>(R.id.textDay3).text = dayWeek

    }

    private fun getWeatherInformation() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        WEATHER_URL =
            "https://api.weatherbit.io/v2.0/forecast/daily?" +
                    "lat=" + latitude +
                    "&lon=" + longitude +
                    "&key=" + API_KEY

        Log.e("TAG", "URL: $WEATHER_URL")

        // Request a string response  from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, WEATHER_URL,
            { response ->

                Log.e("TAG", "Response: $response")
                showWeatherInformation(response)
            },
            // In case of any error
            {
                Toast.makeText(getApplicationContext(), "Could not get weather information", Toast.LENGTH_SHORT).show()
            })
        queue.add(stringReq)
    }
}