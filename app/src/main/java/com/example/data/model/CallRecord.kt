package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    OUTGOING_UNLIMITED,
    OUTGOING_FREE,
    OUTGOING_BONUS,
    MISSED,
    REJECTED
}

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val callType: CallType = CallType.OUTGOING_UNLIMITED,
    val status: String = "Completed",
    val countryCode: String = "+1",
    val countryName: String = "United States",
    val note: String = "",
    val maskedCallerId: String = "",
    val isAnonymousCallerId: Boolean = true
)
