package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_quotas")
data class DailyQuota(
    @PrimaryKey
    val dateKey: String, // format: "yyyy-MM-dd"
    val isUnlimited: Boolean = true, // Default to Unlimited Free Calling
    val freeCallsUsed: Int = 0,
    val bonusCallsEarned: Int = 0,
    val bonusCallsUsed: Int = 0,
    val checkedIn: Boolean = false,
    val streakDays: Int = 1,
    val spinRewardsClaimed: Int = 0,
    val videoAdsWatched: Int = 0
) {
    companion object {
        const val DAILY_FREE_LIMIT = 3
    }

    val freeCallsRemaining: Int
        get() = if (isUnlimited) Int.MAX_VALUE else (DAILY_FREE_LIMIT - freeCallsUsed).coerceAtLeast(0)

    val bonusCallsRemaining: Int
        get() = (bonusCallsEarned - bonusCallsUsed).coerceAtLeast(0)

    val totalCallsRemaining: Int
        get() = if (isUnlimited) Int.MAX_VALUE else (freeCallsRemaining + bonusCallsRemaining)

    val totalCallsMadeToday: Int
        get() = freeCallsUsed + bonusCallsUsed
}
