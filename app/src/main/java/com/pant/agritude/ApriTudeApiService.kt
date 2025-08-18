package com.pant.agritude

import com.pant.agritude.data.MarketDataResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import com.pant.agritude.gemini.GeminiRequest
import com.pant.agritude.gemini.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class MandiPriceData(
    val records: List<Record>
)

data class Record(
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val min_price: String,
    val max_price: String,
    val modal_price: String,
    val date: String
)

// Retrofit Interface for the API
interface AgriTudeApiService {
    @GET("resource/")
    suspend fun getMandiPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10,
        @QueryMap filters: Map<String, String>
    ): Response<MarketDataResponse>

    // Gemini API-க்கான புதிய POST முறை
    // Here we add the 'key' parameter to send the API key with the request.
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String // <-- இங்கு ஏபிஐ கீ சேர்க்கப்பட்டுள்ளது
    ): GeminiResponse
}

// Retrofit Client to make API calls (இங்கு மட்டும் வைத்திருக்கிறோம்)
object RetrofitClient {
    private const val BASE_URL = "https://api.data.gov.in/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: AgriTudeApiService by lazy {
        retrofit.create(AgriTudeApiService::class.java)
    }
}
