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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.ui.theme.CallAmber
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CyanAccent
import com.example.viewmodel.HistoryFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    calls: List<CallRecord>,
    currentFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    onRedial: (CallRecord) -> Unit,
    onDeleteCall: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 80.dp)
    ) {
        // Top Filter Bar & Clear History Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter == HistoryFilter.ALL,
                    onClick = { onFilterChange(HistoryFilter.ALL) },
                    label = { Text("All (${calls.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = currentFilter == HistoryFilter.FREE_CALLS,
                    onClick = { onFilterChange(HistoryFilter.FREE_CALLS) },
                    label = { Text("Free Calls", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = currentFilter == HistoryFilter.BONUS_CALLS,
                    onClick = { onFilterChange(HistoryFilter.BONUS_CALLS) },
                    label = { Text("Bonus", fontSize = 12.sp) }
                )
            }

            if (calls.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (calls.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Call History Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your private 10-digit virtual SIM calls will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(calls, key = { it.id }) { call ->
                    CallHistoryItem(
                        call = call,
                        onRedial = { onRedial(call) },
                        onDelete = { onDeleteCall(call.id) }
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Call History") },
            text = { Text("Are you sure you want to clear all call records? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = CallRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CallHistoryItem(
    call: CallRecord,
    onRedial: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }
    val timeFormatted = remember(call.timestamp) { sdf.format(Date(call.timestamp)) }
    val durationFormatted = remember(call.durationSeconds) {
        val m = call.durationSeconds / 60
        val s = call.durationSeconds % 60
        String.format("%02d:%02d", m, s)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call Type Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when (call.callType) {
                            CallType.OUTGOING_UNLIMITED -> CallGreen.copy(alpha = 0.18f)
                            CallType.OUTGOING_FREE -> CallGreen.copy(alpha = 0.15f)
                            CallType.OUTGOING_BONUS -> CyanAccent.copy(alpha = 0.15f)
                            CallType.MISSED -> CallRed.copy(alpha = 0.15f)
                            CallType.REJECTED -> CallAmber.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (call.callType) {
                        CallType.OUTGOING_UNLIMITED -> Icons.Default.CallMade
                        CallType.OUTGOING_FREE -> Icons.Default.CallMade
                        CallType.OUTGOING_BONUS -> Icons.Default.CallMade
                        CallType.MISSED -> Icons.Default.PhoneMissed
                        CallType.REJECTED -> Icons.Default.CallReceived
                    },
                    contentDescription = null,
                    tint = when (call.callType) {
                        CallType.OUTGOING_UNLIMITED -> CallGreen
                        CallType.OUTGOING_FREE -> CallGreen
                        CallType.OUTGOING_BONUS -> CyanAccent
                        CallType.MISSED -> CallRed
                        CallType.REJECTED -> CallAmber
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = call.contactName ?: "${call.countryCode} ${call.phoneNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (call.callType) {
                            CallType.OUTGOING_UNLIMITED -> CallGreen.copy(alpha = 0.18f)
                            CallType.OUTGOING_FREE -> CallGreen.copy(alpha = 0.15f)
                            else -> CyanAccent.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = when (call.callType) {
                                CallType.OUTGOING_UNLIMITED -> "∞ Unlimited"
                                CallType.OUTGOING_FREE -> "Free Call"
                                else -> "Bonus Call"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (call.callType) {
                                CallType.OUTGOING_UNLIMITED -> CallGreen
                                CallType.OUTGOING_FREE -> CallGreen
                                else -> CyanAccent
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$timeFormatted • $durationFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (call.isAnonymousCallerId && call.maskedCallerId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hidden Number",
                            tint = CyanAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Receiver saw: ${call.maskedCallerId} (Real Number Hidden)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = CyanAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Redial Button
            IconButton(
                onClick = onRedial,
                modifier = Modifier.testTag("redial_button_${call.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Redial",
                    tint = CallGreen
                )
            }
        }
    }
}
