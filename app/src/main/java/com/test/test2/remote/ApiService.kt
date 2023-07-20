package com.test.test2.remote

import com.test.test2.model.Places
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    fun getNearbyBank(@Url url:String):Call<Places>
}