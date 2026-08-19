package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyQuota
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuotaDao {
    @Query("SELECT * FROM daily_quotas WHERE dateKey = :dateKey")
    fun getQuotaForDate(dateKey: String): Flow<DailyQuota?>

    @Query("SELECT * FROM daily_quotas WHERE dateKey = :dateKey")
    suspend fun getQuotaDirect(dateKey: String): DailyQuota?

    @Query("SELECT * FROM daily_quotas ORDER BY dateKey DESC LIMIT 7")
    fun getRecentWeeklyQuotas(): Flow<List<DailyQuota>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuota(quota: DailyQuota)

    @Update
    suspend fun updateQuota(quota: DailyQuota)
}
