package com.example.yesiwant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

@Composable
fun iPodClickWheel(
    modifier: Modifier = Modifier,
    wheelColor: Color = Color.White.copy(alpha = 0.12f),
    centerButtonColor: Color = Color.White.copy(alpha = 0.18f),
    iconColor: Color = Color(0xFFE8EDF7).copy(alpha = 0.9f),
    onScrollDown: () -> Unit,
    onScrollUp: () -> Unit,
    onCenterClick: () -> Unit,
    onMenuClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    var previousAngle by remember { mutableFloatStateOf(0f) }
    var rotationAccumulator by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(320.dp)
            .clip(CircleShape)
            .background(wheelColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val x = offset.x - size.width / 2
                        val y = offset.y - size.height / 2
                        previousAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                    }
                ) { change, _ ->
                    val x = change.position.x - size.width / 2
                    val y = change.position.y - size.height / 2
                    val currentAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                    var delta = currentAngle - previousAngle
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f

                    rotationAccumulator += delta
                    previousAngle = currentAngle

                    if (rotationAccumulator > 15f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScrollDown()
                        rotationAccumulator = 0f
                    } else if (rotationAccumulator < -15f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScrollUp()
                        rotationAccumulator = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val labelColor = iconColor
        val labelWeight = FontWeight.Normal
        val labelSize = 18.sp

        Text(
            text = "menu",
            color = labelColor,
            fontWeight = labelWeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMenuClick()
                }
        )

        Text(
            text = "pause",
            color = labelColor,
            fontWeight = labelWeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPlayPauseClick()
                }
        )

        Text(
            text = "prev",
            color = labelColor,
            fontWeight = labelWeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPrevClick()
                }
        )

        Text(
            text = "next",
            color = labelColor,
            fontWeight = labelWeight,
            fontSize = labelSize,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNextClick()
                }
        )

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(centerButtonColor)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCenterClick()
                }
        )
    }
}