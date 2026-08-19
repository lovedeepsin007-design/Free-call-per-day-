package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallAmber
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldReward
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SpinWheelDialog(
    onClaimReward: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var wonReward by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        label = "wheel_spin_anim"
    )

    val segments = listOf(
        Pair("1 Call", 1),
        Pair("2 Calls", 2),
        Pair("1 Call", 1),
        Pair("3 Calls!", 3),
        Pair("1 Call", 1),
        Pair("2 Calls", 2)
    )

    AlertDialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        modifier = Modifier.testTag("spin_wheel_dialog"),
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
                            .background(GoldReward.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = GoldReward,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lucky Spin & Win",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!isSpinning) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spin the prize wheel to earn bonus free calls today!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wheel Container with Needle
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Wheel Canvas
                    Canvas(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(animatedRotation)
                    ) {
                        val segmentAngle = 360f / segments.size
                        val colors = listOf(
                            Color(0xFF00A86B),
                            Color(0xFF06B6D4),
                            Color(0xFFF59E0B),
                            Color(0xFF8B5CF6),
                            Color(0xFF10B981),
                            Color(0xFFEC4899)
                        )

                        for (i in segments.indices) {
                            drawArc(
                                color = colors[i % colors.size],
                                startAngle = i * segmentAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                        }

                        // Outer ring border
                        drawCircle(
                            color = Color.White,
                            radius = size.width / 2f,
                            style = Stroke(width = 6f)
                        )
                    }

                    // Center Hub
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Spin",
                            tint = GoldReward,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Needle pointer at top
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(16.dp, 22.dp)
                            .background(Color.White, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (wonReward > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CallGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎉 You won +$wonReward Free Call${if (wonReward > 1) "s" else ""}!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CallGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (wonReward > 0) {
                Button(
                    onClick = { onClaimReward(wonReward) },
                    colors = ButtonDefaults.buttonColors(containerColor = CallGreen),
                    modifier = Modifier.testTag("claim_spin_reward_button")
                ) {
                    Text("Claim Reward", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (!isSpinning) {
                            isSpinning = true
                            val chosenIndex = Random.nextInt(segments.size)
                            val randomReward = segments[chosenIndex].second
                            val extraRounds = 5 * 360f
                            val targetAngle = extraRounds + (chosenIndex * 60f) + 30f
                            rotationAngle = targetAngle

                            scope.launch {
                                delay(3100)
                                isSpinning = false
                                wonReward = randomReward
                            }
                        }
                    },
                    enabled = !isSpinning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("spin_wheel_action_button")
                ) {
                    Text(if (isSpinning) "Spinning..." else "Spin Now!", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isSpinning && wonReward == 0) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
