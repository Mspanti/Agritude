package com.pant.agritude

import com.pant.agritude.data.MarketDataResponse
import com.pant.agritude.data.MarketPrice
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

class MockAgriTudeApiService : AgriTudeApiService {

    override suspend fun getMandiPrices(
        apiKey: String,
        format: String,
        limit: Int,
        filters: Map<String, String>
    ): Response<MarketDataResponse> {
        delay(TimeUnit.SECONDS.toMillis(1))

        // இப்போது பல பயிர்களைக் கொண்ட ஒரு போலிப் பட்டியலை உருவாக்குகிறோம்
        val mockPrices = listOf(
            MarketPrice(
                commodity = "Tomato",
                variety = "Local",
                market = "Chennai",
                price = "45",
                minPrice = "40",
                maxPrice = "50",
                date = "2024-05-20"
            ),
            MarketPrice(
                commodity = "Onion",
                variety = "Small",
                market = "Madurai",
                price = "30",
                minPrice = "25",
                maxPrice = "35",
                date = "2024-05-20"
            ),
            MarketPrice(
                commodity = "Potato",
                variety = "C-40",
                market = "Coimbatore",
                price = "25",
                minPrice = "20",
                maxPrice = "30",
                date = "2024-05-20"
            ),
            MarketPrice(
                commodity = "Brinjal",
                variety = "Purple",
                market = "Trichy",
                price = "20",
                minPrice = "15",
                maxPrice = "25",
                date = "2024-05-20"
            ),
            MarketPrice(
                commodity = "Chilli",
                variety = "Red",
                market = "Theni",
                price = "70",
                minPrice = "65",
                maxPrice = "75",
                date = "2024-05-20"
            ),
            MarketPrice(
                commodity = "Banana",
                variety = "Nendran",
                market = "Dindigul",
                price = "15",
                minPrice = "12",
                maxPrice = "18",
                date = "2024-05-20"
            )
        )
        val mockResponse = MarketDataResponse(records = mockPrices)
        return Response.success(mockResponse)
    }
}
