package com.example.yesiwant

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

@Composable
fun NowPlayingDisk(
    modifier: Modifier = Modifier,
    albumArtUri: Uri?,
    isPlaying: Boolean
) {
    val rotation = remember { Animatable(0f) }

    // The Animation Engine
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing)
                )
            )
        } else {
            rotation.stop()
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation.value
            },
        contentAlignment = Alignment.Center
    ) {
        if (albumArtUri != null) {

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUri)
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                val state = painter.state
                when (state) {
                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }
                    is AsyncImagePainter.State.Error -> {
                        FallbackCD()
                    }
                    else -> {

                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        } else {

            FallbackCD()
        }
    }
}


@Composable
fun FallbackCD() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        drawCircle(color = Color(0xFFD3D3D3), radius = radius)
        drawCircle(color = Color(0xFFA9A9A9), radius = radius * 0.95f, style = Stroke(width = 2f))
        drawCircle(color = Color(0xFFA9A9A9), radius = radius * 0.3f, style = Stroke(width = 1f))
        drawCircle(color = Color.Black, radius = radius * 0.15f)
    }
}