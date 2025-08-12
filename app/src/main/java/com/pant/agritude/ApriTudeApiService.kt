// ApriTudeApiService.kt
package com.pant.agritude

import com.pant.agritude.data.MarketDataResponse // MarketDataResponse-ஐ இறக்குமதி செய்கிறது
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

// API response data class (இந்த class-ஐ இங்கு மட்டும் வைத்திருக்கிறோம்)
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
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getMandiPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10,
        @QueryMap filters: Map<String, String>
    ): Response<MarketDataResponse> // <MarketDataResponse> என மாற்றப்பட்டுள்ளது
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
