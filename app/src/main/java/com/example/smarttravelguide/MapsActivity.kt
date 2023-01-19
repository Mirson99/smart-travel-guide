package com.example.smarttravelguide

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.smarttravelguide.databinding.ActivityMapsBinding
import com.example.smarttravelguide.ui.auth.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    lateinit var tvCurrentAddress: TextView
    lateinit var button: Button

    var end_latitude = 0.0
    var end_longitude = 0.0
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

        button.setOnClickListener {
            searchArea()
        }

    }

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
                    var latLng = LatLng(myAddress.latitude, myAddress.longitude)
                    markerOptions.position(latLng)
                    mMap!!.addMarker(markerOptions)
                    end_latitude = myAddress.latitude
                    end_longitude = myAddress.longitude
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
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

                    tvCurrentAddress!!.setText("Distance = $s KM")
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

        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
        googleMap?.addMarker(markerOptions)
    }
}