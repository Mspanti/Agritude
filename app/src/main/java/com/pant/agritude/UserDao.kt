package com.pant.agritude

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUser(): Flow<UserEntity?>

}