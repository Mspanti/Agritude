
package com.pant.agritude.data

import com.google.gson.annotations.SerializedName

// API பதிலில் உள்ள ஒரு தனி சந்தை விலைத் தரவுக்கான மாதிரி
data class MarketPrice(
    @SerializedName("commodity") val commodity: String,
    @SerializedName("variety") val variety: String,
    @SerializedName("market") val market: String,
    @SerializedName("min_price") val minPrice: String,
    @SerializedName("max_price") val maxPrice: String,
    @SerializedName("date") val date: String,
    // DashboardScreen.kt-இல் பயன்படுத்தப்படும் price மற்றும் unit-க்கு,
    // இங்கே ஒரு தற்காலிகத் தீர்வாக modal_price-ஐப் பயன்படுத்துவோம்.
    @SerializedName("modal_price") val price: String,
    val unit: String = "₹/Quintal" // API-இல் இல்லாததால், ஒரு இயல்புநிலை மதிப்பை அமைக்கிறோம்.
)

// API பதிலுக்கான (response) மாதிரி
data class MarketDataResponse(
    @SerializedName("records") val records: List<MarketPrice>
)
