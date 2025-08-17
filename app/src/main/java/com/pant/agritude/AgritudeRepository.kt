package com.pant.agritude

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.pant.agritude.data.MarketDataResponse
import com.pant.agritude.MessageDao
import com.pant.agritude.MessageEntity
import retrofit2.HttpException
import java.io.IOException

// Add UserDao to the constructor
class AgriTudeRepository(
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val apiService: AgriTudeApiService
) {

    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    // UserEntity-இல் உள்ள புதிய ஃபீல்டுகளைப் பயன்படுத்துகிறது.
    val userProfile: Flow<UserEntity?> = userDao.getUser()

    suspend fun insert(message: MessageEntity): Long {
        return messageDao.insert(message)
    }

    suspend fun update(message: MessageEntity) {
        messageDao.update(message)
    }

    // பயனர் சுயவிவரத்தை, புதிய ஃபீல்டுகளுடன் புதுப்பிக்கிறது.
    suspend fun updateProfile(user: UserEntity) {
        userDao.insert(user)
    }

    // Function to get latest market prices from the API.
    fun getMarketPrices(): Flow<MarketDataResponse> = flow {
        try {
            val response = apiService.getMandiPrices(
                apiKey = "579b464db66ec23bdd000001da78fa78988a42c75a8cf43773001557",
                filters = emptyMap()
            )
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                emit(MarketDataResponse(records = emptyList()))
            }
        } catch (e: Exception) {
            emit(MarketDataResponse(records = emptyList()))
        }
    }
}
