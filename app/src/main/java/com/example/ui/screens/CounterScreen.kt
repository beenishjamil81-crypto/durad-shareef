package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recitation
import com.example.ui.model.DUROOD_PRESETS
import com.example.ui.model.DuroodInfo
import com.example.ui.viewmodel.DuroodViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CounterScreen(
    viewModel: DuroodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val sessionCount by viewModel.sessionCount.collectAsState()
    val targetGoal by viewModel.targetGoal.collectAsState()
    val selectedDurood by viewModel.selectedDurood.collectAsState()
    val userName by viewModel.userNameInput.collectAsState()
    val dedication by viewModel.dedicationInput.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val userRecitationsState by viewModel.userRecitations.collectAsState()

    var isPressedAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressedAnim) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "tasbeeh_scale"
    )

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Person Name Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reciter / Person Name",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { viewModel.userNameInput.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("person_name_input"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text("Enter person's name (e.g. Ahmad, Fatima)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Durood Preset Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Durood Shareef",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = selectedDurood.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(DUROOD_PRESETS) { preset ->
                            FilterChip(
                                selected = selectedDurood.id == preset.id,
                                onClick = { viewModel.selectDurood(preset) },
                                label = { Text(preset.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Display with transliteration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedDurood.arabic,
                                fontSize = 20.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedDurood.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Circular Tasbeeh Bead Dial
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(animatedScale)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerVibration()
                        viewModel.incrementSession(1L)
                        isPressedAnim = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(100)
                            isPressedAnim = false
                        }
                    }
                    .testTag("tasbeeh_click_button"),
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress indicator
                val progress = if (targetGoal > 0) (sessionCount % targetGoal).toFloat() / targetGoal.toFloat() else 0f
                CircularProgressIndicator(
                    progress = { if (progress == 0f && sessionCount > 0) 1f else progress },
                    modifier = Modifier.size(210.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "COUNT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.getDefault()).format(sessionCount),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Goal: $targetGoal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to Count",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Increments & Goal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickAddChip(label = "+1", onClick = {
                    triggerVibration()
                    viewModel.incrementSession(1L)
                })
                QuickAddChip(label = "+10", onClick = {
                    triggerVibration()
                    viewModel.incrementSession(10L)
                })
                QuickAddChip(label = "+33", onClick = {
                    triggerVibration()
                    viewModel.incrementSession(33L)
                })
                QuickAddChip(label = "+100", onClick = {
                    triggerVibration()
                    viewModel.incrementSession(100L)
                })
                IconButton(
                    onClick = { viewModel.resetSession() },
                    modifier = Modifier.testTag("reset_counter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Counter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Target Goal Selector Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Target Cycle:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                listOf(33L, 100L, 500L, 1000L).forEach { goal ->
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.setTargetGoal(goal) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (targetGoal == goal) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = goal.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (targetGoal == goal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Dedication Note
            OutlinedTextField(
                value = dedication,
                onValueChange = { viewModel.dedicationInput.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dedication_input"),
                placeholder = { Text("Dedication or Intention (e.g. Friday blessings, for parents)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.submitCurrentSession { success, error ->
                        coroutineScope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Durood Shareef recitations synced successfully!")
                            } else if (error != null) {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    }
                },
                enabled = !isSubmitting && sessionCount > 0L,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_count_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit & Sync Recitations ($sessionCount)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User's Personal Cumulative Stats Card
            val myTotal = when (val state = userRecitationsState) {
                is UiState.Success -> state.data.sumOf { it.count }
                else -> 0L
            }
            val myEntries = when (val state = userRecitationsState) {
                is UiState.Success -> state.data.size
                else -> 0
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My Synced Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = NumberFormat.getNumberInstance(Locale.getDefault()).format(myTotal),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My Submissions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$myEntries batches",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun QuickAddChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
