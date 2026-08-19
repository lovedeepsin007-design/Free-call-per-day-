package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.viewmodel.ActiveCallState
import com.example.viewmodel.CallStatus

@Composable
fun ActiveCallScreen(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHoldToggle: () -> Unit,
    onKeypadToggle: () -> Unit,
    onDigitClick: (Char) -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_anim"
    )

    val formatDuration: (Int) -> String = { totalSecs ->
        val m = totalSecs / 60
        val s = totalSecs % 60
        String.format("%02d:%02d", m, s)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_call_screen"),
        color = DarkBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Caller Info, Security Badge & Call Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy / 10-Digit Virtual SIM Indicator
                    if (callState.isAnonymousCallerId) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F2027),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Hidden Real Number",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Receiver SIM sees: ${callState.maskedCallerId} (Real Number Hidden)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    Text(
                        text = callState.country.flagEmoji + " " + callState.country.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = callState.contactName ?: callState.phoneNumber,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (callState.contactName != null) {
                        Text(
                            text = "${callState.country.dialCode} ${callState.phoneNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Badge & Timer
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (callState.status) {
                            CallStatus.CONNECTED -> CallGreen.copy(alpha = 0.2f)
                            CallStatus.ENDED -> CallRed.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (callState.status) {
                                            CallStatus.CONNECTED -> CallGreen
                                            CallStatus.ENDED -> CallRed
                                            else -> CyanAccent
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (callState.status == CallStatus.CONNECTED)
                                    "Connected • ${formatDuration(callState.durationSeconds)}"
                                else
                                    callState.statusMessage,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Middle: Visual Avatar with Animated Sound Waves
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
                    ) {
                        if (callState.status == CallStatus.CONNECTED || callState.status == CallStatus.RINGING) {
                            // Pulsing glowing ring
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                CallGreen.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        // Central Avatar
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF00A86B),
                                            Color(0xFF06B6D4)
                                        )
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = (callState.contactName ?: callState.phoneNumber).firstOrNull()?.uppercase() ?: "?"
                            Text(
                                text = initial,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Simulated Voice Spectrum Bars
                    if (callState.status == CallStatus.CONNECTED && !callState.isHold) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Canvas(
                            modifier = Modifier
                                .width(160.dp)
                                .height(26.dp)
                        ) {
                            val barCount = 12
                            val barWidth = size.width / (barCount * 1.8f)
                            val spacing = barWidth * 0.8f
                            val maxH = size.height

                            for (i in 0 until barCount) {
                                val factor = ((i + waveOffset * 4) % 4) / 4f
                                val h = (maxH * (0.25f + factor * 0.75f)).coerceIn(4f, maxH)
                                val x = i * (barWidth + spacing)
                                val y = (maxH - h) / 2f

                                drawRoundRect(
                                    color = CallGreen,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, h),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${callState.liveTranscript}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // In-Call Keypad (if opened)
                AnimatedVisibility(
                    visible = callState.isInCallKeypadOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131F33), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (callState.inCallKeypadText.isEmpty()) "DTMF Dialpad" else callState.inCallKeypadText,
                            style = MaterialTheme.typography.titleMedium,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val keys = listOf(
                            listOf('1', '2', '3'),
                            listOf('4', '5', '6'),
                            listOf('7', '8', '9'),
                            listOf('*', '0', '#')
                        )
                        for (row in keys) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (digit in row) {
                                    Surface(
                                        modifier = Modifier
                                            .size(54.dp, 40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onDigitClick(digit) },
                                        color = Color(0xFF1B2A45)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = digit.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // Bottom Controls: Mute, Speaker, Keypad, Hold, End Call
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallControlButton(
                            icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (callState.isMuted) "Muted" else "Mute",
                            isActive = callState.isMuted,
                            onClick = onMuteToggle,
                            testTag = "incall_mute_button"
                        )

                        InCallControlButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            isActive = callState.isInCallKeypadOpen,
                            onClick = onKeypadToggle,
                            testTag = "incall_keypad_button"
                        )

                        InCallControlButton(
                            icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            label = if (callState.isSpeakerOn) "Speaker" else "Earpiece",
                            isActive = callState.isSpeakerOn,
                            onClick = onSpeakerToggle,
                            testTag = "incall_speaker_button"
                        )

                        InCallControlButton(
                            icon = if (callState.isHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                            label = if (callState.isHold) "Resume" else "Hold",
                            isActive = callState.isHold,
                            onClick = onHoldToggle,
                            testTag = "incall_hold_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // End Call Button (Large Red Circle)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CallRed)
                            .clickable(onClick = onEndCall)
                            .testTag("end_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun InCallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFF1B2A45)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) CyanAccent else Color.White.copy(alpha = 0.8f)
        )
    }
}
