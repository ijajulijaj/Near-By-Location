package com.test.test2.key

import com.test.test2.remote.ApiService
import com.test.test2.remote.RetrofitClient

object Key {
    //Barikoi Api
    val mapKey= "NDgwODo3UThPTFQ2NFJF"
    val base_url = "https://barikoi.xyz/"

    val apiService: ApiService
        get() = RetrofitClient.getClient(base_url).create(ApiService::class.java)
}