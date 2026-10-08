package com.example.yesiwant

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
fun VolumeDotsControl(
    modifier: Modifier = Modifier,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit
) {
    val totalDots = 14
    val columns = 7
    var containerWidth by remember { mutableFloatStateOf(1f) }
    val haptic = LocalHapticFeedback.current
    var lastHapticIndex by remember { mutableIntStateOf(-1) }

    Row(
        modifier = modifier
            .onGloballyPositioned { containerWidth = it.size.width.toFloat() }

            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val rawVolume = (x / containerWidth).coerceIn(0f, 1f)
                    onVolumeChange(rawVolume)


                    val currentIndex = (rawVolume * totalDots).toInt()
                    if (currentIndex != lastHapticIndex) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastHapticIndex = currentIndex
                    }
                }
            }

            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val x = offset.x
                    val rawVolume = (x / containerWidth).coerceIn(0f, 1f)
                    onVolumeChange(rawVolume)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }

            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        for (col in 0 until columns) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val topDotIndex = col * 2
                val isTopActive = (topDotIndex.toFloat() / totalDots) <= currentVolume
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTopActive) Color(0xFFEDF1FA) else Color(0xFFC7CEE2))
                )


                val bottomDotIndex = col * 2 + 1
                val isBottomActive = (bottomDotIndex.toFloat() / totalDots) <= currentVolume
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isBottomActive) Color(0xFFEDF1FA) else Color(0xFFC7CEE2))
                )
            }
        }
    }
}