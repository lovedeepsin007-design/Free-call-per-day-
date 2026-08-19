package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActiveCallScreen
import com.example.ui.components.AddContactDialog
import com.example.ui.components.CallerIdSettingsDialog
import com.example.ui.components.CountryPickerDialog
import com.example.ui.components.QuotaExhaustedDialog
import com.example.ui.components.RewardVideoModal
import com.example.ui.components.SpinWheelDialog
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.theme.CallGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.CallViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: CallViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val dialerNumber by viewModel.dialerNumber.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val todayQuota by viewModel.todayQuota.collectAsStateWithLifecycle()
    val weeklyQuotas by viewModel.weeklyQuotas.collectAsStateWithLifecycle()
    val allCalls by viewModel.allCalls.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val favoriteContacts by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val timeToMidnightMs by viewModel.timeToMidnight.collectAsStateWithLifecycle()

    val isAnonymousCallerId by viewModel.isAnonymousCallerId.collectAsStateWithLifecycle()
    val maskedCallerId by viewModel.maskedCallerId.collectAsStateWithLifecycle()
    val showCallerIdSettings by viewModel.showCallerIdSettingsDialog.collectAsStateWithLifecycle()

    val showQuotaExhausted by viewModel.showQuotaExhaustedDialog.collectAsStateWithLifecycle()
    val showAddContact by viewModel.showAddContactDialog.collectAsStateWithLifecycle()
    val showCountryPicker by viewModel.showCountryPicker.collectAsStateWithLifecycle()
    val showRewardVideo by viewModel.showRewardVideo.collectAsStateWithLifecycle()
    val rewardVideoProgress by viewModel.rewardVideoProgress.collectAsStateWithLifecycle()
    val showSpinWheel by viewModel.showSpinWheel.collectAsStateWithLifecycle()
    val snackBarMessage by viewModel.snackBarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Comprehensive hierarchical BackHandler to fix unintended auto-back or closing
    BackHandler(enabled = true) {
        when {
            // 1. In Active Call
            activeCall.isActive -> {
                if (activeCall.isInCallKeypadOpen) {
                    viewModel.toggleInCallKeypad()
                } else {
                    viewModel.endCall("Call ended by user")
                }
            }
            // 2. Modals & Overlays
            showCallerIdSettings -> viewModel.setShowCallerIdSettings(false)
            showRewardVideo -> viewModel.dismissVideoReward()
            showSpinWheel -> viewModel.dismissSpinWheel()
            showQuotaExhausted -> viewModel.dismissQuotaDialog()
            showCountryPicker -> viewModel.setShowCountryPicker(false)
            showAddContact -> viewModel.setShowAddContact(false)

            // 3. Search query active
            searchQuery.isNotEmpty() -> viewModel.setSearchQuery("")

            // 4. Sub-tabs back to primary Keypad / Dialer
            selectedTab != AppTab.DIALER -> viewModel.setTab(AppTab.DIALER)

            // 5. Dialer input cleared first
            dialerNumber.isNotEmpty() -> viewModel.onDialerClear()

            // 6. Safe double-back exit on root Keypad screen
            else -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CallGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Free Call",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Top Calling Status Badge (Unlimited / Standard)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (todayQuota.isUnlimited) CallGreen.copy(alpha = 0.18f)
                            else if (todayQuota.totalCallsRemaining > 0) CallGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTab(AppTab.REWARDS) }
                                .padding(end = 12.dp)
                                .testTag("top_remaining_calls_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (todayQuota.isUnlimited) {
                                    Icon(
                                        imageVector = Icons.Default.AllInclusive,
                                        contentDescription = "Unlimited",
                                        tint = CallGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (todayQuota.totalCallsRemaining > 0) CallGreen else MaterialTheme.colorScheme.error)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (todayQuota.isUnlimited) "∞ Unlimited" else "${todayQuota.totalCallsRemaining}/3 Free",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (todayQuota.isUnlimited || todayQuota.totalCallsRemaining > 0) CallGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.DIALER,
                    onClick = { viewModel.setTab(AppTab.DIALER) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.DIALER) Icons.Filled.Dialpad else Icons.Outlined.Dialpad,
                            contentDescription = "Keypad"
                        )
                    },
                    label = { Text("Keypad") },
                    modifier = Modifier.testTag("tab_keypad")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.RECENTS,
                    onClick = { viewModel.setTab(AppTab.RECENTS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.RECENTS) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "Recents"
                        )
                    },
                    label = { Text("Recents") },
                    modifier = Modifier.testTag("tab_recents")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.CONTACTS,
                    onClick = { viewModel.setTab(AppTab.CONTACTS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.CONTACTS) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts") },
                    modifier = Modifier.testTag("tab_contacts")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.REWARDS,
                    onClick = { viewModel.setTab(AppTab.REWARDS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.REWARDS) Icons.Filled.CardGiftcard else Icons.Outlined.CardGiftcard,
                            contentDescription = "Quota & Rewards"
                        )
                    },
                    label = { Text("Free Calls") },
                    modifier = Modifier.testTag("tab_rewards")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.DIALER -> {
                    DialerScreen(
                        dialerNumber = dialerNumber,
                        selectedCountry = selectedCountry,
                        todayQuota = todayQuota,
                        favoriteContacts = favoriteContacts,
                        timeToMidnightMs = timeToMidnightMs,
                        isAnonymousCallerId = isAnonymousCallerId,
                        maskedCallerId = maskedCallerId,
                        onRegenerateCallerId = { viewModel.regenerateMaskedCallerId() },
                        onOpenCallerIdSettings = { viewModel.setShowCallerIdSettings(true) },
                        onDigitClick = viewModel::onDialerDigit,
                        onBackspaceClick = viewModel::onDialerBackspace,
                        onClearClick = viewModel::onDialerClear,
                        onCallClick = { viewModel.initiateCall() },
                        onQuickCallContact = { contact ->
                            viewModel.initiateCall(
                                phoneNumber = contact.phoneNumber,
                                contactName = contact.name
                            )
                        },
                        onOpenCountryPicker = { viewModel.setShowCountryPicker(true) },
                        onGetMoreCalls = { viewModel.setTab(AppTab.REWARDS) }
                    )
                }

                AppTab.RECENTS -> {
                    RecentsScreen(
                        calls = allCalls,
                        currentFilter = historyFilter,
                        onFilterChange = viewModel::setHistoryFilter,
                        onRedial = { call ->
                            viewModel.initiateCall(
                                phoneNumber = call.phoneNumber,
                                contactName = call.contactName
                            )
                        },
                        onDeleteCall = viewModel::deleteCallRecord,
                        onClearAll = viewModel::clearCallHistory
                    )
                }

                AppTab.CONTACTS -> {
                    ContactsScreen(
                        contacts = contacts,
                        favoriteContacts = favoriteContacts,
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::setSearchQuery,
                        onCallContact = { contact ->
                            viewModel.initiateCall(
                                phoneNumber = contact.phoneNumber,
                                contactName = contact.name
                            )
                        },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDeleteContact = viewModel::deleteContact,
                        onAddNewContactClick = { viewModel.setShowAddContact(true) }
                    )
                }

                AppTab.REWARDS -> {
                    RewardsScreen(
                        todayQuota = todayQuota,
                        weeklyQuotas = weeklyQuotas,
                        timeToMidnightMs = timeToMidnightMs,
                        onToggleUnlimited = viewModel::toggleUnlimitedMode,
                        onDailyCheckIn = viewModel::claimDailyCheckIn,
                        onWatchVideo = viewModel::startWatchVideoReward,
                        onOpenSpinWheel = viewModel::openSpinWheel,
                        onResetQuotaForTesting = viewModel::resetDailyQuotaForTesting
                    )
                }
            }
        }
    }

    // Active Call Fullscreen Overlay
    AnimatedVisibility(
        visible = activeCall.isActive,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        ActiveCallScreen(
            callState = activeCall,
            onMuteToggle = viewModel::toggleMute,
            onSpeakerToggle = viewModel::toggleSpeaker,
            onHoldToggle = viewModel::toggleHold,
            onKeypadToggle = viewModel::toggleInCallKeypad,
            onDigitClick = viewModel::onDialerDigit,
            onEndCall = { viewModel.endCall() }
        )
    }

    // Dialogs & Modals
    if (showCallerIdSettings) {
        CallerIdSettingsDialog(
            isAnonymous = isAnonymousCallerId,
            maskedCallerId = maskedCallerId,
            onToggleAnonymous = viewModel::toggleAnonymousCallerId,
            onRegenerateNumber = { viewModel.regenerateMaskedCallerId() },
            onDismiss = { viewModel.setShowCallerIdSettings(false) }
        )
    }

    if (showQuotaExhausted) {
        QuotaExhaustedDialog(
            timeToMidnightMs = timeToMidnightMs,
            hasCheckedIn = todayQuota.checkedIn,
            onWatchVideo = viewModel::startWatchVideoReward,
            onDailyCheckIn = viewModel::claimDailyCheckIn,
            onOpenSpinWheel = viewModel::openSpinWheel,
            onDismiss = viewModel::dismissQuotaDialog
        )
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentCountry = selectedCountry,
            onSelectCountry = viewModel::setCountry,
            onDismiss = { viewModel.setShowCountryPicker(false) }
        )
    }

    if (showAddContact) {
        AddContactDialog(
            onSave = viewModel::saveNewContact,
            onDismiss = { viewModel.setShowAddContact(false) }
        )
    }

    if (showRewardVideo) {
        RewardVideoModal(
            progress = rewardVideoProgress,
            onDismiss = viewModel::dismissVideoReward
        )
    }

    if (showSpinWheel) {
        SpinWheelDialog(
            onClaimReward = viewModel::claimSpinResult,
            onDismiss = viewModel::dismissSpinWheel
        )
    }
}
