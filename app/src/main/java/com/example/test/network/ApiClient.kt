package com.example.test.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.test.utils.NetworkConfigManager

object ApiClient {
    private var baseUrl: String? = null
    private var enableLogs: Boolean = false

    fun initialize(appContext: Context) {
        baseUrl = NetworkConfigManager.getBaseUrl(appContext)
        enableLogs = true // Always enabled for debugging

        Log.d("ApiClient", "=== API CLIENT INITIALIZATION ===")
        Log.d("ApiClient", "Base URL configured: $baseUrl")
        Log.d("ApiClient", "Is emulator: ${NetworkConfigManager.isEmulator()}")
        Log.d("ApiClient", "Auto-detection enabled: ${NetworkConfigManager.isAutoDetectionEnabled(appContext)}")
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
                Log.d("ApiClient", "=== SENDING REQUEST ===")
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
        .readTimeout(240, TimeUnit.SECONDS)  // 4 minutes for SII queries that can take up to 3 minutes
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(300, TimeUnit.SECONDS)  // 5 minutes total timeout for long operations
        .build()

    private var _retrofit: Retrofit? = null
    private var _apiService: ApiService? = null

    val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                if (baseUrl == null) {
                    throw IllegalStateException("ApiClient has not been initialized. Call ApiClient.initialize(context) first.")
                }

                // ✅ NUEVO: Configurar Gson con deserializadores personalizados para manejar campos numéricos vacíos
                val gson = com.google.gson.GsonBuilder()
                    .registerTypeAdapter(Double::class.java, com.example.test.models.SafeDoubleDeserializer())
                    .registerTypeAdapter(Int::class.java, com.example.test.models.SafeIntDeserializer())
                    .registerTypeAdapter(Int::class.javaObjectType, com.example.test.models.SafeNullableIntDeserializer())
                    .setLenient() // Permite JSON menos estricto
                    .create()

                _retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl!!)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Usar Gson personalizado
                    .build()

                Log.d("ApiClient", "✅ Retrofit configurado con deserializadores seguros para campos numéricos")
            }
            return _retrofit!!
        }

    val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = retrofit.create(ApiService::class.java)
            }
            return _apiService!!
        }
}
