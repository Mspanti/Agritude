package com.pant.agritude

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.pant.agritude.data.MarketDataResponse // MarketDataResponse-ஐ இறக்குமதி செய்கிறது
import com.pant.agritude.MessageDao
import com.pant.agritude.MessageEntity
import retrofit2.HttpException
import java.io.IOException

class AgriTudeRepository(
    private val messageDao: MessageDao,
    private val apiService: AgriTudeApiService // Add API service here
) {

    // Exposes the list of messages as a Flow.
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    // Inserts a new message into the database.
    suspend fun insert(message: MessageEntity) {
        messageDao.insert(message)
    }

    // New function to get latest market prices from the API.
    fun getMarketPrices(): Flow<MarketDataResponse> = flow {
        try {
            // getLatestMarketPrices getMandiPrices
            val response = apiService.getMandiPrices(
                apiKey = "579b464db66ec23bdd000001da78fa78988a42c75a8cf43773001557",
                filters = emptyMap()
            )
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                // MarketDataResponse-ஐ records
                emit(MarketDataResponse(records = emptyList()))
            }
        } catch (e: Exception) {
            // MarketDataResponse-ஐ records
            emit(MarketDataResponse(records = emptyList()))
        }
    }
}
