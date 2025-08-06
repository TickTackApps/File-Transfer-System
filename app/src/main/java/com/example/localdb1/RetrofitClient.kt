package com.example.localdb1

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    val ip = getSavedServerIP()

    private var BASE_URL = "http://$ip:5000/"
    private var retrofit: Retrofit = createRetrofitInstance(BASE_URL)

    private fun createRetrofitInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }


    fun saveServerIP(context: Context, ip: String) {
        val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit()
        sharedPref.putString("server_ip", ip)
        sharedPref.apply()
    }


    fun getSavedServerIP(): String? {
        return MyApp.instance.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString("server_ip", null)
    }


    fun updateBaseUrl(context: Context, newBaseUrl: String) {

        val ip = getSavedServerIP()

        val formattedBaseUrl = "http://$ip:5000"
        BASE_URL = formattedBaseUrl
        saveServerIP(context, newBaseUrl)

        Log.d("DEBUG", "Updated Base URL: $BASE_URL")

        retrofit = createRetrofitInstance(BASE_URL)
    }


}
