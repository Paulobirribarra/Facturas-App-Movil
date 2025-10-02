package com.example.test.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.test.R

object ApiClient {
    private var baseUrl: String? = null
    private var enableLogs: Boolean = false

    fun initialize(context: Context) {
        baseUrl = context.getString(R.string.api_base_url)
        enableLogs = context.resources.getBoolean(R.bool.enable_http_logs)
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        if (enableLogs) {
            Log.d("HTTP_REQUEST", message)
        }
    }.apply {
        level = if (enableLogs) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()

            if (enableLogs) {
                val empresaId = request.header("X-Empresa-ID")
                Log.d("ApiClient", "=== ENVIANDO REQUEST ===")
                Log.d("ApiClient", "URL: ${request.url}")
                Log.d("ApiClient", "Method: ${request.method}")
                Log.d("ApiClient", "X-Empresa-ID Header: $empresaId")
                // No mostrar el token completo por seguridad
                val authHeader = request.header("Authorization")
                if (authHeader != null) {
                    Log.d("ApiClient", "Authorization Header: Bearer ***${authHeader.takeLast(8)}")
                }
            }

            val response = chain.proceed(request)

            if (enableLogs) {
                Log.d("ApiClient", "Response Code: ${response.code}")
            }

            response
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        if (baseUrl == null) {
            throw IllegalStateException("ApiClient no ha sido inicializado. Llama a ApiClient.initialize(context) primero.")
        }

        Retrofit.Builder()
            .baseUrl(baseUrl!!)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
