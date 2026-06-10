package com.example.a20222356sddas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.InbodyRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun CustomLineChart(
    records: List<InbodyRecord>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(0) } // 0: 체중, 1: 골격근량, 2: 체지방률
    var timeFilter by remember { mutableStateOf(0) } // 0: 최근 7회, 1: 최근 30회, 2: 전체

    val filteredRecords = remember(records, timeFilter) {
        val sorted = records.sortedBy { it.date }
        when (timeFilter) {
            0 -> sorted.takeLast(7)
            1 -> sorted.takeLast(30)
            else -> sorted
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "변화 트렌드",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Time Range Filters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("주간(7회)", "월간(30회)", "전체").forEachIndexed { index, label ->
                        val isSelected = timeFilter == index
                        Button(
                            onClick = { timeFilter = index },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val metricNames = listOf("체중 (kg)", "골격근량 (kg)", "체지방률 (%)")
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                
                val colors = listOf(primaryColor, secondaryColor, tertiaryColor)

                metricNames.forEachIndexed { index, name ->
                    val isSelected = selectedMetric == index
                    val activeColor = colors[index]
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { selectedMetric = index },
                        label = { Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = activeColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeColor,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "시각화할 인바디 기록이 부족합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Interactive Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val textMeasurer = rememberTextMeasurer()
                    
                    val values = remember(filteredRecords, selectedMetric) {
                        filteredRecords.map {
                            when (selectedMetric) {
                                0 -> it.weight
                                1 -> it.skeletalMuscleMass
                                else -> it.bodyFatPercentage
                            }
                        }
                    }

                    val dates = remember(filteredRecords) {
                        filteredRecords.map {
                            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.date))
                        }
                    }

                    val minValue = remember(values) { (values.minOrNull() ?: 0.0) - 2.0 }
                    val maxValue = remember(values) { (values.maxOrNull() ?: 10.0) + 2.0 }
                    val valueRange = remember(minValue, maxValue) {
                        if (maxValue - minValue == 0.0) 4.0 else maxValue - minValue
                    }

                    val lineColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary
                    )
                    val lineColor = lineColors[selectedMetric]

                    var touchedPointIndex by remember { mutableStateOf<Int?>(null) }
                    var touchedOffset by remember { mutableStateOf(Offset.Zero) }

                    // Reset touched point when metric/records change
                    LaunchedEffect(selectedMetric, records, timeFilter) {
                        touchedPointIndex = null
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(values, dates) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        // Calculate nearest index
                                        val canvasWidth = size.width
                                        val paddingRight = 40f
                                        val paddingLeft = 60f
                                        val drawableWidth = canvasWidth - paddingLeft - paddingRight

                                        if (values.size > 1) {
                                            val stepX = drawableWidth / (values.size - 1)
                                            var nearestIndex = 0
                                            var minDist = Float.MAX_VALUE
                                            for (i in values.indices) {
                                                val x = paddingLeft + i * stepX
                                                val dist = abs(offset.x - x)
                                                if (dist < minDist) {
                                                    minDist = dist
                                                    nearestIndex = i
                                                }
                                            }
                                            if (minDist < stepX / 2f || minDist < 40f) {
                                                touchedPointIndex = nearestIndex
                                                touchedOffset = offset
                                            } else {
                                                touchedPointIndex = null
                                            }
                                        } else if (values.size == 1) {
                                            touchedPointIndex = 0
                                            touchedOffset = offset
                                        }
                                    }
                                )
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        val paddingTop = 30f
                        val paddingBottom = 40f
                        val paddingLeft = 70f
                        val paddingRight = 40f

                        val drawableWidth = canvasWidth - paddingLeft - paddingRight
                        val drawableHeight = canvasHeight - paddingTop - paddingBottom

                        // 1. Draw Grid Lines & Y Axis Labels
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val ratio = i.toFloat() / gridCount
                            val y = paddingTop + drawableHeight * (1f - ratio)
                            val gridVal = minValue + valueRange * ratio

                            // Horizontal Line
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(paddingLeft, y),
                                end = Offset(canvasWidth - paddingRight, y),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Y Label
                            val yLabel = String.format("%.1f", gridVal)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = yLabel,
                                style = TextStyle(
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                topLeft = Offset(10f, y - 15f)
                            )
                        }

                        // Exit early if no values
                        if (values.isEmpty()) return@Canvas

                        // 2. Compute Points
                        val points = mutableListOf<Offset>()
                        if (values.size == 1) {
                            val x = paddingLeft + drawableWidth / 2f
                            val y = paddingTop + drawableHeight * (1f - ((values[0] - minValue) / valueRange).toFloat())
                            points.add(Offset(x, y))
                        } else {
                            val stepX = drawableWidth / (values.size - 1)
                            for (i in values.indices) {
                                val x = paddingLeft + i * stepX
                                val y = paddingTop + drawableHeight * (1f - ((values[i] - minValue) / valueRange).toFloat())
                                points.add(Offset(x, y))
                            }
                        }

                        // 3. Draw Gradient under path
                        if (points.size > 1) {
                            val gradPath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                val stepX = drawableWidth / (values.size - 1)
                                for (i in 1 until points.size) {
                                    // Smooth bezier interpolation
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cp1 = Offset(prev.x + stepX / 3f, prev.y)
                                    val cp2 = Offset(curr.x - stepX / 3f, curr.y)
                                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                                }
                                lineTo(points.last().x, paddingTop + drawableHeight)
                                lineTo(points.first().x, paddingTop + drawableHeight)
                                close()
                            }

                            drawPath(
                                path = gradPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                                    startY = points.map { it.y }.minOrNull() ?: 0f,
                                    endY = paddingTop + drawableHeight
                                )
                            )
                        }

                        // 4. Draw Chart Line
                        if (points.size > 1) {
                            val strokePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                val stepX = drawableWidth / (values.size - 1)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cp1 = Offset(prev.x + stepX / 3f, prev.y)
                                    val cp2 = Offset(curr.x - stepX / 3f, curr.y)
                                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // 5. Draw Dots & X Labels
                        val drawXLabelsCount = if (points.size > 7) 5 else points.size
                        val xLabelStep = if (points.size > 7) points.size / 4 else 1

                        points.forEachIndexed { i, pt ->
                            // Draw Data Point Dot
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 2.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Draw X Label
                            if (i % xLabelStep == 0 || i == points.lastIndex) {
                                val labelText = dates[i]
                                val labelSize = textMeasurer.measure(
                                    text = labelText,
                                    style = TextStyle(fontSize = 9.sp)
                                ).size
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    style = TextStyle(
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    topLeft = Offset(pt.x - labelSize.width / 2f, paddingTop + drawableHeight + 10f)
                                )
                            }
                        }

                        // 6. Draw interactive selected point & tooltip
                        touchedPointIndex?.let { index ->
                            if (index in points.indices) {
                                val pt = points[index]
                                val valText = "${values[index]}"
                                val dateText = dates[index]
                                val tooltipString = "$dateText: $valText"

                                // Highlight Line
                                drawLine(
                                    color = lineColor.copy(alpha = 0.5f),
                                    start = Offset(pt.x, paddingTop),
                                    end = Offset(pt.x, paddingTop + drawableHeight),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )

                                // Draw outer circle glow
                                drawCircle(
                                    color = lineColor,
                                    radius = 8.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )

                                // Tooltip text measurement
                                val textLayoutResult = textMeasurer.measure(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                            append(tooltipString)
                                        }
                                    },
                                    style = TextStyle(fontSize = 10.sp)
                                )
                                val textWidth = textLayoutResult.size.width
                                val textHeight = textLayoutResult.size.height

                                // Draw tooltip background
                                val tipW = textWidth + 24f
                                val tipH = textHeight + 16f
                                val tipX = (pt.x - tipW / 2f).coerceIn(paddingLeft, canvasWidth - paddingRight - tipW)
                                val tipY = (pt.y - tipH - 20f).coerceAtLeast(10f)

                                drawRoundRect(
                                    color = Color.DarkGray.copy(alpha = 0.85f),
                                    topLeft = Offset(tipX, tipY),
                                    size = Size(tipW, tipH),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )

                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = tooltipString,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    topLeft = Offset(tipX + 12f, tipY + 8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
