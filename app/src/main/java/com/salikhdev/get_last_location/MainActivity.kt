package com.salikhdev.get_last_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.salikhdev.get_last_location.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var fusedLocationClient: FusedLocationProviderClient? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.getCurrentLocationBtn.setOnClickListener {

            if (hasLocationPermission()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    if (checkGPSEnabled()) {
                        turnOnGPS()
                    } else {
                        getLastLocation()
                    }

                } else {
                    getLastLocation()
                }
            } else {

                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    showCostumeDialog(
                        "Location Permission",
                        "This app needs the location permission to track your location !!",
                        "ok",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                multiplePermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )

                            }
                        }, "Cancel", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                dialog?.cancel()

                            }
                        })

                } else {
                    multiplePermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

            }

        }


    }

    private fun turnOnGPS() {
        val request = LocationRequest.create().apply {
            interval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(this, 12345)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }.addOnSuccessListener {
            // On GPS
        }
    }

    private fun checkGPSEnabled(): Boolean {
        var isOn = false
        val manager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER).not()) {
            isOn = true
        }
        return isOn
    }

    private fun hasLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showCostumeDialog(
        title: String,
        message: String,
        ok: String,
        okBtn: DialogInterface.OnClickListener,
        cancel: String,
        cancelBtn: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(ok, okBtn)
            .setNegativeButton(cancel, cancelBtn)
            .create().show()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        fusedLocationClient!!.lastLocation.addOnCompleteListener { result ->

            if (result.isSuccessful) {
                val location = result.result

                binding.lonLat.setText("Lat : ${location.latitude} /n Lon : ${location.longitude}")

            }

        }

    }

    private val multiplePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        object : ActivityResultCallback<Map<String, Boolean>> {
            @SuppressLint("NewApi")
            override fun onActivityResult(result: Map<String, Boolean>) {

                var finePermissionAllowed = false
                if (result[Manifest.permission.ACCESS_FINE_LOCATION] != null) {
                    finePermissionAllowed = true
                    if (finePermissionAllowed) {
                        getLastLocation()
                    } else {

                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                            showCostumeDialog(
                                "Location Permission",
                                "This app needs the location permission to track your location !!",
                                "ok",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {

                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                        )
                                        startActivity(intent)

                                    }
                                }, "Cancel", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {

                                        dialog?.cancel()

                                    }
                                })

                        }

                    }
                }

            }
        })


}