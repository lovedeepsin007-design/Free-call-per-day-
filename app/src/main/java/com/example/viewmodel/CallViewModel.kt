package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.model.Country
import com.example.data.model.CountryList
import com.example.data.model.DailyQuota
import com.example.data.repository.CallRepository
import com.example.util.AudioFeedbackHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab {
    DIALER,
    RECENTS,
    CONTACTS,
    REWARDS
}

enum class CallStatus {
    IDLE,
    DIALING,
    RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCallState(
    val isActive: Boolean = false,
    val status: CallStatus = CallStatus.IDLE,
    val phoneNumber: String = "",
    val contactName: String? = null,
    val country: Country = CountryList.all.first(),
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isHold: Boolean = false,
    val isInCallKeypadOpen: Boolean = false,
    val inCallKeypadText: String = "",
    val liveTranscript: String = "",
    val statusMessage: String = "Connecting...",
    val isAnonymousCallerId: Boolean = true,
    val maskedCallerId: String = ""
)

enum class HistoryFilter {
    ALL,
    FREE_CALLS,
    BONUS_CALLS
}

class CallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CallRepository(AppDatabase.getDatabase(application))
    val audioFeedback = AudioFeedbackHelper(application)

    private val _selectedTab = MutableStateFlow(AppTab.DIALER)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _dialerNumber = MutableStateFlow("")
    val dialerNumber: StateFlow<String> = _dialerNumber.asStateFlow()

    private val _selectedCountry = MutableStateFlow(CountryList.all.first())
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    // 10-Digit Virtual / Anonymous Caller ID (Hides real number on receiver's SIM)
    private val _isAnonymousCallerId = MutableStateFlow(true)
    val isAnonymousCallerId: StateFlow<Boolean> = _isAnonymousCallerId.asStateFlow()

    private val _maskedCallerId = MutableStateFlow(repository.generate10DigitVirtualCallerId("+1"))
    val maskedCallerId: StateFlow<String> = _maskedCallerId.asStateFlow()

    private val _showCallerIdSettingsDialog = MutableStateFlow(false)
    val showCallerIdSettingsDialog: StateFlow<Boolean> = _showCallerIdSettingsDialog.asStateFlow()

    private val _activeCall = MutableStateFlow(ActiveCallState())
    val activeCall: StateFlow<ActiveCallState> = _activeCall.asStateFlow()

    private val _showQuotaExhaustedDialog = MutableStateFlow(false)
    val showQuotaExhaustedDialog: StateFlow<Boolean> = _showQuotaExhaustedDialog.asStateFlow()

    private val _showAddContactDialog = MutableStateFlow(false)
    val showAddContactDialog: StateFlow<Boolean> = _showAddContactDialog.asStateFlow()

    private val _showCountryPicker = MutableStateFlow(false)
    val showCountryPicker: StateFlow<Boolean> = _showCountryPicker.asStateFlow()

    private val _showRewardVideo = MutableStateFlow(false)
    val showRewardVideo: StateFlow<Boolean> = _showRewardVideo.asStateFlow()

    private val _rewardVideoProgress = MutableStateFlow(0f)
    val rewardVideoProgress: StateFlow<Float> = _rewardVideoProgress.asStateFlow()

    private val _showSpinWheel = MutableStateFlow(false)
    val showSpinWheel: StateFlow<Boolean> = _showSpinWheel.asStateFlow()

    private val _timeToMidnight = MutableStateFlow(repository.getTimeUntilMidnight())
    val timeToMidnight: StateFlow<Long> = _timeToMidnight.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    val todayQuota: StateFlow<DailyQuota> = repository.getTodayQuota()
        .combine(MutableStateFlow(Unit)) { quota, _ ->
            quota ?: repository.getOrCreateTodayQuota()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyQuota(dateKey = repository.getTodayKey(), isUnlimited = true)
        )

    val weeklyQuotas: StateFlow<List<DailyQuota>> = repository.getWeeklyQuotas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCalls: StateFlow<List<CallRecord>> = repository.allCalls
        .combine(_historyFilter) { calls, filter ->
            when (filter) {
                HistoryFilter.ALL -> calls
                HistoryFilter.FREE_CALLS -> calls.filter {
                    it.callType == CallType.OUTGOING_UNLIMITED || it.callType == CallType.OUTGOING_FREE
                }
                HistoryFilter.BONUS_CALLS -> calls.filter {
                    it.callType == CallType.OUTGOING_BONUS
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val contacts: StateFlow<List<ContactItem>> = repository.allContacts
        .combine(_searchQuery) { contactList, query ->
            if (query.isBlank()) {
                contactList
            } else {
                contactList.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.phoneNumber.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteContacts: StateFlow<List<ContactItem>> = repository.favoriteContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var callTimerJob: Job? = null
    private var midnightCountdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.preseedContactsIfEmpty()
            repository.getOrCreateTodayQuota()
        }
        startMidnightCountdown()
    }

    private fun startMidnightCountdown() {
        midnightCountdownJob?.cancel()
        midnightCountdownJob = viewModelScope.launch {
            while (isActive) {
                _timeToMidnight.value = repository.getTimeUntilMidnight()
                delay(1000)
            }
        }
    }

    fun setTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setCountry(country: Country) {
        _selectedCountry.value = country
        _showCountryPicker.value = false
        // Update virtual masked number with selected country code
        regenerateMaskedCallerId(country.dialCode)
    }

    fun setShowCountryPicker(show: Boolean) {
        _showCountryPicker.value = show
    }

    fun toggleAnonymousCallerId(enabled: Boolean) {
        _isAnonymousCallerId.value = enabled
        showToast(
            if (enabled) "🔒 Real Number Hidden! 10-Digit Virtual ID will be shown to receiver."
            else "Standard Caller ID enabled."
        )
    }

    fun regenerateMaskedCallerId(dialCode: String? = null) {
        val code = dialCode ?: _selectedCountry.value.dialCode
        val newId = repository.generate10DigitVirtualCallerId(code)
        _maskedCallerId.value = newId
        audioFeedback.vibrate(20)
        showToast("✨ New 10-Digit Virtual Number generated: $newId")
    }

    fun setShowCallerIdSettings(show: Boolean) {
        _showCallerIdSettingsDialog.value = show
    }

    fun toggleUnlimitedMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUnlimitedMode(enabled)
            showToast(if (enabled) "✨ Unlimited Free Calls Mode Activated!" else "Standard Daily 3-Call limit mode set.")
        }
    }

    fun onDialerDigit(char: Char) {
        audioFeedback.playDtmf(char)
        if (_activeCall.value.isActive && _activeCall.value.isInCallKeypadOpen) {
            _activeCall.value = _activeCall.value.copy(
                inCallKeypadText = _activeCall.value.inCallKeypadText + char
            )
        } else {
            _dialerNumber.value = _dialerNumber.value + char
        }
    }

    fun onDialerBackspace() {
        audioFeedback.vibrate(10)
        if (_activeCall.value.isActive && _activeCall.value.isInCallKeypadOpen) {
            val current = _activeCall.value.inCallKeypadText
            if (current.isNotEmpty()) {
                _activeCall.value = _activeCall.value.copy(
                    inCallKeypadText = current.dropLast(1)
                )
            }
        } else {
            val current = _dialerNumber.value
            if (current.isNotEmpty()) {
                _dialerNumber.value = current.dropLast(1)
            }
        }
    }

    fun onDialerClear() {
        audioFeedback.vibrate(25)
        _dialerNumber.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun initiateCall(phoneNumber: String? = null, contactName: String? = null, country: Country? = null) {
        val targetNum = phoneNumber ?: _dialerNumber.value
        if (targetNum.isBlank()) {
            showToast("Please enter a valid phone number.")
            return
        }

        val targetCountry = country ?: _selectedCountry.value
        val quota = todayQuota.value

        if (!quota.isUnlimited && quota.totalCallsRemaining <= 0) {
            _showQuotaExhaustedDialog.value = true
            return
        }

        val masked = _maskedCallerId.value
        val isAnon = _isAnonymousCallerId.value

        // Start Call Flow
        audioFeedback.vibrate(30)
        _activeCall.value = ActiveCallState(
            isActive = true,
            status = CallStatus.DIALING,
            phoneNumber = targetNum,
            contactName = contactName ?: findContactName(targetNum),
            country = targetCountry,
            durationSeconds = 0,
            statusMessage = "Dialing ${targetCountry.dialCode} $targetNum via VoIP...",
            isAnonymousCallerId = isAnon,
            maskedCallerId = masked
        )

        startCallSimulationFlow(targetNum, contactName, targetCountry)
    }

    private fun findContactName(phoneNumber: String): String? {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return contacts.value.find {
            it.phoneNumber.replace(Regex("[^0-9]"), "") == cleanNumber
        }?.name
    }

    private val sampleVoiceResponses = listOf(
        "Hello! Connected to recipient's SIM. Your real number is hidden and safe.",
        "Hi! Voice call streaming to receiver's SIM with masked 10-digit Caller ID.",
        "Hey! The call is live and crystal clear with complete caller privacy.",
        "Hello! Receiver sees 10-digit virtual VoIP ID. Real phone number protected."
    )

    private fun startCallSimulationFlow(
        phoneNumber: String,
        contactName: String?,
        country: Country
    ) {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            // 1. Dialing phase (1.5 seconds)
            audioFeedback.playRingTone()
            delay(1500)

            // 2. Ringing phase (2.5 seconds)
            _activeCall.value = _activeCall.value.copy(
                status = CallStatus.RINGING,
                statusMessage = "Ringing receiver's SIM card..."
            )
            audioFeedback.playRingTone()
            delay(2000)

            // 3. Connected phase
            val greeting = sampleVoiceResponses.random()
            _activeCall.value = _activeCall.value.copy(
                status = CallStatus.CONNECTED,
                statusMessage = "HD Voice Connected to SIM",
                liveTranscript = greeting
            )
            audioFeedback.vibrate(50)

            // Timer loop
            var seconds = 0
            while (isActive && _activeCall.value.status == CallStatus.CONNECTED) {
                delay(1000)
                seconds++
                _activeCall.value = _activeCall.value.copy(
                    durationSeconds = seconds
                )
            }
        }
    }

    fun toggleMute() {
        audioFeedback.vibrate(15)
        _activeCall.value = _activeCall.value.copy(
            isMuted = !_activeCall.value.isMuted
        )
    }

    fun toggleSpeaker() {
        audioFeedback.vibrate(15)
        _activeCall.value = _activeCall.value.copy(
            isSpeakerOn = !_activeCall.value.isSpeakerOn
        )
    }

    fun toggleHold() {
        audioFeedback.vibrate(15)
        val newHold = !_activeCall.value.isHold
        _activeCall.value = _activeCall.value.copy(
            isHold = newHold,
            statusMessage = if (newHold) "Call on Hold" else "HD Voice Connected to SIM"
        )
    }

    fun toggleInCallKeypad() {
        audioFeedback.vibrate(15)
        _activeCall.value = _activeCall.value.copy(
            isInCallKeypadOpen = !_activeCall.value.isInCallKeypadOpen
        )
    }

    fun endCall(reason: String = "Call Ended") {
        audioFeedback.playCallEndTone()
        callTimerJob?.cancel()

        val current = _activeCall.value
        val duration = current.durationSeconds
        val phone = current.phoneNumber
        val name = current.contactName
        val country = current.country
        val masked = current.maskedCallerId
        val isAnon = current.isAnonymousCallerId

        _activeCall.value = current.copy(
            status = CallStatus.ENDED,
            statusMessage = reason
        )

        viewModelScope.launch {
            // Deduct quota and record into DB
            repository.recordCall(
                phoneNumber = phone,
                contactName = name,
                durationSeconds = duration,
                countryCode = country.dialCode,
                countryName = country.name,
                maskedCallerId = masked,
                isAnonymousCallerId = isAnon,
                status = "Completed"
            )

            delay(1200)
            _activeCall.value = ActiveCallState(isActive = false)
        }
    }

    fun dismissQuotaDialog() {
        _showQuotaExhaustedDialog.value = false
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val (success, msg) = repository.claimDailyCheckIn()
            audioFeedback.vibrate(40)
            showToast(msg)
            if (success) {
                _showQuotaExhaustedDialog.value = false
            }
        }
    }

    fun startWatchVideoReward() {
        _showQuotaExhaustedDialog.value = false
        _showRewardVideo.value = true
        _rewardVideoProgress.value = 0f

        viewModelScope.launch {
            for (i in 1..100) {
                delay(50) // 5 seconds total
                _rewardVideoProgress.value = i / 100f
            }
            audioFeedback.vibrate(60)
            val (_, msg) = repository.rewardVideoWatched()
            showToast(msg)
            delay(500)
            _showRewardVideo.value = false
        }
    }

    fun dismissVideoReward() {
        _showRewardVideo.value = false
    }

    fun openSpinWheel() {
        _showQuotaExhaustedDialog.value = false
        _showSpinWheel.value = true
    }

    fun dismissSpinWheel() {
        _showSpinWheel.value = false
    }

    fun claimSpinResult(bonusCalls: Int) {
        viewModelScope.launch {
            audioFeedback.vibrate(50)
            val (_, msg) = repository.claimSpinReward(bonusCalls)
            showToast(msg)
            _showSpinWheel.value = false
        }
    }

    fun resetDailyQuotaForTesting() {
        viewModelScope.launch {
            repository.resetQuotaForTesting()
            showToast("Quota reset & Unlimited Free Calling refreshed!")
        }
    }

    fun deleteCallRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteCall(id)
            showToast("Call record deleted.")
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("Call history cleared.")
        }
    }

    fun setShowAddContact(show: Boolean) {
        _showAddContactDialog.value = show
    }

    fun saveNewContact(
        name: String,
        phoneNumber: String,
        countryCode: String,
        category: String,
        isFavorite: Boolean
    ) {
        if (name.isBlank() || phoneNumber.isBlank()) {
            showToast("Please enter a name and phone number.")
            return
        }

        viewModelScope.launch {
            val colorPalette = listOf(
                0xFF10B981, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B, 0xFF06B6D4
            )
            val contact = ContactItem(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                countryCode = countryCode,
                avatarColorHex = colorPalette.random(),
                isFavorite = isFavorite,
                category = category
            )
            repository.insertContact(contact)
            _showAddContactDialog.value = false
            showToast("Contact '$name' saved!")
        }
    }

    fun deleteContact(contact: ContactItem) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            showToast("Contact '${contact.name}' deleted.")
        }
    }

    fun toggleFavorite(contact: ContactItem) {
        viewModelScope.launch {
            val updated = contact.copy(isFavorite = !contact.isFavorite)
            repository.updateContact(updated)
        }
    }

    fun showToast(message: String) {
        _snackBarMessage.value = message
    }

    fun clearToast() {
        _snackBarMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioFeedback.release()
    }
}
