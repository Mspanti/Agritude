package com.pant.agritude

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.pant.agritude.data.MarketDataResponse
import com.pant.agritude.MessageDao
import com.pant.agritude.MessageEntity
import retrofit2.HttpException
import java.io.IOException


class AgriTudeRepository(
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val apiService: AgriTudeApiService
) {

    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()


    val userProfile: Flow<UserEntity?> = userDao.getUser()

    suspend fun insert(message: MessageEntity): Long {
        return messageDao.insert(message)
    }

    suspend fun update(message: MessageEntity) {
        messageDao.update(message)
    }

    suspend fun updateProfile(user: UserEntity) {
        userDao.insert(user)
    }


    fun getMarketPrices(): Flow<MarketDataResponse> = flow {
        try {
            val response = apiService.getMandiPrices(
                apiKey = "",
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
