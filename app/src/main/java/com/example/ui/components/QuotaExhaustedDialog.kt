package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallAmber
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldReward
import java.util.concurrent.TimeUnit

@Composable
fun QuotaExhaustedDialog(
    timeToMidnightMs: Long,
    hasCheckedIn: Boolean,
    onWatchVideo: () -> Unit,
    onDailyCheckIn: () -> Unit,
    onOpenSpinWheel: () -> Unit,
    onDismiss: () -> Unit
) {
    val hours = TimeUnit.MILLISECONDS.toHours(timeToMidnightMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeToMidnightMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeToMidnightMs) % 60
    val countdownText = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("quota_exhausted_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CallAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CallAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Limit Reached",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You have used all 3 free calls allocated for today. Free quota resets every midnight at 00:00:00.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Countdown Timer Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AvTimer,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next 3 Free Calls in: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Or unlock extra calls immediately:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 1: Watch short video (+1 Call)
                RewardOptionItem(
                    icon = Icons.Default.PlayCircleOutline,
                    title = "Watch 5s Sponsor Video",
                    sub = "+1 Free Call immediately",
                    badgeColor = CallGreen,
                    onClick = onWatchVideo,
                    testTag = "watch_video_reward_option"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Daily Check-In (+1 Call)
                RewardOptionItem(
                    icon = Icons.Default.Check,
                    title = if (hasCheckedIn) "Daily Check-In (Claimed)" else "Claim Daily Check-In",
                    sub = if (hasCheckedIn) "Already claimed for today" else "+1 Free Call reward",
                    badgeColor = if (hasCheckedIn) MaterialTheme.colorScheme.outline else GoldReward,
                    enabled = !hasCheckedIn,
                    onClick = onDailyCheckIn,
                    testTag = "daily_checkin_option"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Lucky Spin Wheel
                RewardOptionItem(
                    icon = Icons.Default.Casino,
                    title = "Spin Lucky Wheel",
                    sub = "Win 1 to 3 Free Calls",
                    badgeColor = CyanAccent,
                    onClick = onOpenSpinWheel,
                    testTag = "spin_wheel_option"
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun RewardOptionItem(
    icon: ImageVector,
    title: String,
    sub: String,
    badgeColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorShapeTint(badgeColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = if (enabled) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun MaterialTheme.colorShapeTint(color: Color): Color {
    return color.copy(alpha = 0.08f)
}
