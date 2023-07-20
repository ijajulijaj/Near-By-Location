package com.test.test2

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.test.test2.key.Key
import com.test.test2.model.Places
import com.test.test2.remote.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapsFragment : Fragment(), OnMapReadyCallback {

    lateinit var mMap:GoogleMap
    private lateinit var pType: String
    private lateinit var currentLocation:Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var mServices: ApiService
    internal lateinit var currentPlace: Places

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mServices = Key.apiService
        val bottomNavigation: BottomNavigationView = view.findViewById(R.id.bottom_navigation_view)

        bottomNavigation.selectedItemId = R.id.action_bank
        bottomNavigation.setOnItemSelectedListener {item ->
           when(item.itemId){
               R.id.action_bank -> nearByBank(mMap,"Bank")
               R.id.action_office -> nearByBank(mMap,"Office")
               R.id.action_shop -> nearByBank(mMap,"Shop")
               else -> {true}
           }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getCurrentLocation()

    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location ->
            if (location != null){
                currentLocation = location
                Toast.makeText(requireContext(),currentLocation.latitude.toString() + "" + currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()
                (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onRequestPermissionsResult(requestCode, permissions, grantResults)",
        "androidx.fragment.app.Fragment"
    ))
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
        }
    }

    override fun onMapReady(googleMap:GoogleMap) {
        mMap = googleMap
        val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
        val markerOption = MarkerOptions().position(latLng).title("Current Location")
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mMap.addMarker(markerOption)
    }

    private fun nearByBank(googleMap: GoogleMap, pType: String):Boolean{
        mMap = googleMap
        mMap.clear()
        var latitude = currentLocation.latitude
        var longitude = currentLocation.longitude
        val url = getUrl(latitude,longitude,pType)
        mServices.getNearbyBank(url)
            .enqueue(object : Callback<Places>{
                override fun onResponse(
                    call: Call<Places>,
                    response: Response<Places>
                ) {
                    currentPlace = response.body()!!
                    if (response.isSuccessful){
                        for (i in 0 until response.body()!!.places.size){
                            val markerOptions = MarkerOptions()
                            val googlePlace = response.body()!!.places[i]
                            latitude = googlePlace.latitude!!
                            longitude = googlePlace.longitude!!
                            val placeName = googlePlace.name
                            val latLng = LatLng(latitude, longitude)
                            markerOptions.position(latLng)
                            markerOptions.title(placeName)

                            if (pType == "Bank"){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_target))
                            }
                            if (pType == "Office"){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_target))
                            }
                            if (pType == "shop"){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_target))
                            }else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                            markerOptions.snippet(i.toString())
                            mMap.addMarker(markerOptions)
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
                        }
                    }

                }

                override fun onFailure(call: Call<Places>, t: Throwable) {
                    Toast.makeText(requireContext(),"" + t.message,Toast.LENGTH_SHORT).show()

                }

            })
        return true
    }

    private fun getUrl(latitude: Double, longitude: Double, pType: String): String {
        val googlePlaceUrl = StringBuilder("https://barikoi.xyz/v2/api/search/nearby/category/")
        googlePlaceUrl.append("NDgwODo3UThPTFQ2NFJF/")
        googlePlaceUrl.append("0.5/")
        googlePlaceUrl.append("10?")
        googlePlaceUrl.append("longitude=$longitude&latitude=$latitude")
        googlePlaceUrl.append("&ptype=$pType")

        Log.d("URL DEBUG",googlePlaceUrl.toString())
        return googlePlaceUrl.toString()

    }
}
