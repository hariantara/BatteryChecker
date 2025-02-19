package xyz.batterychecker.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.batterychecker.viewmodel.HealthViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Info

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = viewModel()
) {
    val healthState by viewModel.healthState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Circle Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthCircle(
                        percentage = healthState.healthPercentage,
                        status = healthState.healthStatus
                    )
                    
                }
            }

            // Capacity Section
            item {
                HealthInfoCard(
                    title = "Battery Capacity",
                    value = "${healthState.currentCapacity} mAh",
                    description = if (healthState.originalCapacity > 0) {
                        "Current capacity of ${((healthState.currentCapacity.toFloat() / healthState.originalCapacity.toFloat()) * 100).toInt()}% from original ${healthState.originalCapacity} mAh"
                    } else {
                        "Original capacity information unavailable"
                    },
                    icon = Icons.Rounded.BatteryChargingFull
                )
            }

            // Cycle Count Section
            item {
                HealthInfoCard(
                    title = "Charge Cycles",
                    value = "${healthState.chargeCycles}",
                    description = if (healthState.chargeCycles > 0) {
                        "Total number of charge cycles"
                    } else {
                        "Cycle count information unavailable"
                    },
                    icon = Icons.Rounded.Loop
                )
            }

            // Temperature Section
            item {
                HealthInfoCard(
                    title = "Temperature Status",
                    value = "%.1f°C".format(healthState.temperature),
                    description = getTemperatureDescription(healthState.temperature),
                    icon = Icons.Rounded.Thermostat
                )
            }

            // Recommendations Section
            if (healthState.recommendations.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            healthState.recommendations.forEach { recommendation ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = recommendation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthCircle(
    percentage: Float,
    status: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(200.dp)
    ) {
        val animatedPercentage = animateFloatAsState(
            targetValue = percentage / 100f,
            label = "health_percentage"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 25f, cap = StrokeCap.Round)
            )
            
            // Health level arc
            drawArc(
                color = getHealthColor(percentage),
                startAngle = -90f,
                sweepAngle = animatedPercentage.value * 360f,
                useCenter = false,
                style = Stroke(width = 25f, cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = getHealthColor(percentage)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthInfoCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getHealthColor(percentage: Float): Color {
    return when {
        percentage >= 80f -> Color(0xFF4CAF50) // Green
        percentage >= 60f -> Color(0xFFFFA000) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getTemperatureDescription(temperature: Float): String {
    return when {
        temperature >= 45f -> "Temperature too high! Cool down your device"
        temperature >= 40f -> "Temperature is getting high"
        temperature <= 0f -> "Temperature too low! Warm up your device"
        temperature <= 5f -> "Temperature is getting low"
        else -> "Temperature is optimal"
    }
}