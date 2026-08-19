package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.model.DailyQuota
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class CallRepository(private val database: AppDatabase) {
    private val callDao = database.callDao()
    private val contactDao = database.contactDao()
    private val quotaDao = database.dailyQuotaDao()

    val allCalls: Flow<List<CallRecord>> = callDao.getAllCalls()
    val allContacts: Flow<List<ContactItem>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<ContactItem>> = contactDao.getFavoriteContacts()

    fun generate10DigitVirtualCallerId(countryDialCode: String = "+1"): String {
        // Generates a 10-digit anonymous VoIP virtual number (e.g., 9874561230 or 5550198421)
        val firstDigit = Random.nextInt(6, 10) // 6, 7, 8, 9
        val remaining9Digits = (1..9).map { Random.nextInt(0, 10) }.joinToString("")
        val tenDigits = "$firstDigit$remaining9Digits"
        return "$countryDialCode $tenDigits"
    }

    fun getTodayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getTodayQuota(): Flow<DailyQuota?> {
        return quotaDao.getQuotaForDate(getTodayKey())
    }

    fun getWeeklyQuotas(): Flow<List<DailyQuota>> {
        return quotaDao.getRecentWeeklyQuotas()
    }

    suspend fun getOrCreateTodayQuota(): DailyQuota {
        val todayKey = getTodayKey()
        var quota = quotaDao.getQuotaDirect(todayKey)
        if (quota == null) {
            quota = DailyQuota(dateKey = todayKey, isUnlimited = true)
            quotaDao.insertQuota(quota)
        }
        return quota
    }

    suspend fun setUnlimitedMode(enabled: Boolean) {
        val todayQuota = getOrCreateTodayQuota()
        val updated = todayQuota.copy(isUnlimited = enabled)
        quotaDao.updateQuota(updated)
    }

    suspend fun recordCall(
        phoneNumber: String,
        contactName: String?,
        durationSeconds: Int,
        countryCode: String,
        countryName: String,
        maskedCallerId: String = "",
        isAnonymousCallerId: Boolean = true,
        status: String = "Completed"
    ): Pair<Boolean, DailyQuota> {
        val todayQuota = getOrCreateTodayQuota()

        if (todayQuota.isUnlimited) {
            val updatedQuota = todayQuota.copy(freeCallsUsed = todayQuota.freeCallsUsed + 1)
            quotaDao.updateQuota(updatedQuota)

            val record = CallRecord(
                phoneNumber = phoneNumber,
                contactName = contactName,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                callType = CallType.OUTGOING_UNLIMITED,
                status = status,
                countryCode = countryCode,
                countryName = countryName,
                maskedCallerId = maskedCallerId,
                isAnonymousCallerId = isAnonymousCallerId
            )
            callDao.insertCall(record)
            return Pair(true, updatedQuota)
        }

        val hasFree = todayQuota.freeCallsRemaining > 0
        val hasBonus = todayQuota.bonusCallsRemaining > 0

        if (!hasFree && !hasBonus) {
            return Pair(false, todayQuota)
        }

        val callType: CallType
        val updatedQuota = if (hasFree) {
            callType = CallType.OUTGOING_FREE
            todayQuota.copy(freeCallsUsed = todayQuota.freeCallsUsed + 1)
        } else {
            callType = CallType.OUTGOING_BONUS
            todayQuota.copy(bonusCallsUsed = todayQuota.bonusCallsUsed + 1)
        }

        quotaDao.updateQuota(updatedQuota)

        val record = CallRecord(
            phoneNumber = phoneNumber,
            contactName = contactName,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            callType = callType,
            status = status,
            countryCode = countryCode,
            countryName = countryName,
            maskedCallerId = maskedCallerId,
            isAnonymousCallerId = isAnonymousCallerId
        )
        callDao.insertCall(record)

        return Pair(true, updatedQuota)
    }

    suspend fun claimDailyCheckIn(): Pair<Boolean, String> {
        val todayQuota = getOrCreateTodayQuota()
        if (todayQuota.checkedIn) {
            return Pair(false, "Already checked in today! Come back tomorrow.")
        }
        val newStreak = todayQuota.streakDays + 1
        val bonus = 1 // +1 free call for check-in
        val updated = todayQuota.copy(
            checkedIn = true,
            streakDays = newStreak,
            bonusCallsEarned = todayQuota.bonusCallsEarned + bonus
        )
        quotaDao.updateQuota(updated)
        return Pair(true, "Checked in! Streak: $newStreak days 🔥 Unlimited Free Calls Active!")
    }

    suspend fun rewardVideoWatched(): Pair<Boolean, String> {
        val todayQuota = getOrCreateTodayQuota()
        val updated = todayQuota.copy(
            videoAdsWatched = todayQuota.videoAdsWatched + 1,
            bonusCallsEarned = todayQuota.bonusCallsEarned + 1
        )
        quotaDao.updateQuota(updated)
        return Pair(true, "Bonus unlocked! Unlimited Free Calling Active.")
    }

    suspend fun claimSpinReward(bonusEarned: Int): Pair<Boolean, String> {
        val todayQuota = getOrCreateTodayQuota()
        val updated = todayQuota.copy(
            spinRewardsClaimed = todayQuota.spinRewardsClaimed + 1,
            bonusCallsEarned = todayQuota.bonusCallsEarned + bonusEarned
        )
        quotaDao.updateQuota(updated)
        return Pair(true, "Congratulations! You won VIP Ultra Calling perk!")
    }

    suspend fun resetQuotaForTesting() {
        val todayQuota = getOrCreateTodayQuota()
        val updated = todayQuota.copy(
            freeCallsUsed = 0,
            bonusCallsUsed = 0,
            bonusCallsEarned = 0,
            checkedIn = false,
            spinRewardsClaimed = 0,
            videoAdsWatched = 0,
            isUnlimited = true
        )
        quotaDao.updateQuota(updated)
    }

    suspend fun deleteCall(id: Long) {
        callDao.deleteCallById(id)
    }

    suspend fun clearHistory() {
        callDao.clearAllCalls()
    }

    suspend fun insertContact(contact: ContactItem) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactItem) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactItem) {
        contactDao.deleteContact(contact)
    }

    suspend fun searchContacts(query: String): Flow<List<ContactItem>> {
        return contactDao.searchContacts(query)
    }

    suspend fun preseedContactsIfEmpty() {
        if (contactDao.getContactCount() == 0) {
            val sampleContacts = listOf(
                ContactItem(
                    name = "Mom ❤️",
                    phoneNumber = "555-0192",
                    countryCode = "+1",
                    avatarColorHex = 0xFFE91E63,
                    isFavorite = true,
                    category = "Family",
                    email = "mom@family.com"
                ),
                ContactItem(
                    name = "Alex Turner",
                    phoneNumber = "555-0143",
                    countryCode = "+1",
                    avatarColorHex = 0xFF3B82F6,
                    isFavorite = true,
                    category = "Friends",
                    email = "alex.t@example.com"
                ),
                ContactItem(
                    name = "Emergency Help Line",
                    phoneNumber = "911",
                    countryCode = "+1",
                    avatarColorHex = 0xFFEF4444,
                    isFavorite = true,
                    category = "Emergency",
                    email = "support@safety.org"
                ),
                ContactItem(
                    name = "Sarah Jenkins",
                    phoneNumber = "7911-123456",
                    countryCode = "+44",
                    avatarColorHex = 0xFF10B981,
                    isFavorite = false,
                    category = "Work",
                    email = "sarah.j@techcorp.co.uk"
                ),
                ContactItem(
                    name = "Rajesh Sharma",
                    phoneNumber = "98765-43210",
                    countryCode = "+91",
                    avatarColorHex = 0xFFF59E0B,
                    isFavorite = false,
                    category = "Friends",
                    email = "rajesh.sharma@domain.in"
                ),
                ContactItem(
                    name = "Dr. Emily Watson",
                    phoneNumber = "555-0188",
                    countryCode = "+1",
                    avatarColorHex = 0xFF8B5CF6,
                    isFavorite = false,
                    category = "Medical",
                    email = "dr.emily@clinic.org"
                )
            )
            contactDao.insertContacts(sampleContacts)
        }
    }

    fun getTimeUntilMidnight(): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return (c.timeInMillis - System.currentTimeMillis()).coerceAtLeast(0)
    }
}
