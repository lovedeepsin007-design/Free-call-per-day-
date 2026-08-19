package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.data.model.Country
import com.example.data.model.DailyQuota
import com.example.ui.components.CallerIdBanner
import com.example.ui.components.DailyQuotaCard
import com.example.ui.components.DialpadView
import com.example.ui.theme.GoldReward

@Composable
fun DialerScreen(
    dialerNumber: String,
    selectedCountry: Country,
    todayQuota: DailyQuota,
    favoriteContacts: List<ContactItem>,
    timeToMidnightMs: Long,
    isAnonymousCallerId: Boolean,
    maskedCallerId: String,
    onRegenerateCallerId: () -> Unit,
    onOpenCallerIdSettings: () -> Unit,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onCallClick: () -> Unit,
    onQuickCallContact: (ContactItem) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onGetMoreCalls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 10.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Real Number Hidden / 10-Digit Virtual Caller ID Banner
        CallerIdBanner(
            isAnonymous = isAnonymousCallerId,
            maskedCallerId = maskedCallerId,
            onRegenerateNumber = onRegenerateCallerId,
            onOpenSettings = onOpenCallerIdSettings,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Daily / Unlimited Quota Card
        DailyQuotaCard(
            quota = todayQuota,
            timeToMidnightMs = timeToMidnightMs,
            onGetMoreCalls = onGetMoreCalls,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Speed Dial / Favorites Row (if any)
        if (favoriteContacts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldReward,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speed Dial",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favoriteContacts) { contact ->
                        SpeedDialChip(
                            contact = contact,
                            onClick = { onQuickCallContact(contact) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Tactile Keypad
        DialpadView(
            dialerNumber = dialerNumber,
            selectedCountry = selectedCountry,
            remainingCalls = todayQuota.totalCallsRemaining,
            isUnlimited = todayQuota.isUnlimited,
            onDigitClick = onDigitClick,
            onBackspaceClick = onBackspaceClick,
            onClearClick = onClearClick,
            onCallClick = onCallClick,
            onOpenCountryPicker = onOpenCountryPicker
        )
    }
}

@Composable
fun SpeedDialChip(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("speed_dial_${contact.name}"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(contact.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = contact.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
