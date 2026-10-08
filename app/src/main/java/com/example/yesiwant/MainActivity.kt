package com.example.yesiwant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.viewModels
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.hypot

enum class ScreenState {
    MENU,
    LIBRARY,
    PLAYER,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val tiltViewModel by viewModels<TiltViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        tiltViewModel.startListening(this)

        setContent {
            val context = LocalContext.current
            val exoPlayer = remember { ExoPlayer.Builder(context).build() }

            var musicList by remember { mutableStateOf<List<Song>>(emptyList()) }
            var selectedIndex by remember { mutableStateOf(0) }
            var currentScreen by remember { mutableStateOf(ScreenState.MENU) }

            val menuItems = listOf("Search", "Recents", "Likes", "Playlists", "Library", "Settings")
            var selectedMenuIndex by remember { mutableStateOf(4) }
            var isPlaying by remember { mutableStateOf(false) }

            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            var currentPosition by remember { mutableLongStateOf(0L) }
            var totalDuration by remember { mutableLongStateOf(0L) }

            val sharedPrefs = remember { context.getSharedPreferences("iPodSettings", MODE_PRIVATE) }
            var isParallaxEnabled by remember {
                mutableStateOf<Boolean>(sharedPrefs.getBoolean("parallax_enabled", true))
            }
            var currentTheme by remember {
                mutableStateOf(sharedPrefs.getString("app_theme", "Pitch Dark") ?: "Pitch Dark")
            }

            LaunchedEffect(isParallaxEnabled) {
                sharedPrefs.edit().putBoolean("parallax_enabled", isParallaxEnabled).apply()
            }
            LaunchedEffect(currentTheme) {
                sharedPrefs.edit().putString("app_theme", currentTheme).apply()
            }

            var currentVolume by remember { mutableFloatStateOf(1f) }

            val defaultBlue = Color(0xFF1A43C7)
            var extractedColor by remember { mutableStateOf(defaultBlue) }

            val animatedBaseColor by animateColorAsState(
                targetValue = extractedColor,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "colorMorph"
            )

            LaunchedEffect(selectedIndex, musicList) {
                if (musicList.isNotEmpty()) {
                    val uri = musicList[selectedIndex].albumArtUri
                    if (uri != null) {
                        withContext(Dispatchers.IO) {
                            val loader = ImageLoader(context)
                            val request = ImageRequest.Builder(context)
                                .data(uri)
                                .size(200)
                                .allowHardware(false)
                                .build()

                            val result = (loader.execute(request) as? SuccessResult)?.drawable
                            val bitmap = (result as? BitmapDrawable)?.bitmap

                            if (bitmap != null) {
                                val palette = Palette.from(bitmap).generate()
                                val colorInt = palette.darkMutedSwatch?.rgb
                                    ?: palette.dominantSwatch?.rgb
                                    ?: palette.darkVibrantSwatch?.rgb

                                if (colorInt != null) {
                                    extractedColor = Color(colorInt)
                                }
                            } else {
                                extractedColor = defaultBlue
                            }
                        }
                    } else {
                        extractedColor = defaultBlue
                    }
                }
            }

            LaunchedEffect(isPlaying) {
                while (isPlaying) {
                    currentPosition = exoPlayer.currentPosition
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                    delay(1000L)
                }
            }

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        isPlaying = isPlayingNow
                    }
                }
                exoPlayer.addListener(listener)
                onDispose { exoPlayer.removeListener(listener) }
            }

            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) musicList = getDeviceMusic(context)
            }

            LaunchedEffect(Unit) { permissionLauncher.launch(permission) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val backgroundModifier = if (currentTheme == "Industrial Yellow") {
                    Modifier.background(Color(0xFFFFD100))
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            0.0f to Color(0xFF244BCB),
                            0.4f to Color(0xFF1F46CB),
                            1.0f to Color(0xFF1A43C7)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(backgroundModifier)
                ) {
                    val morphTransition = updateTransition(targetState = currentScreen, label = "morph")

                    val maskWidth by morphTransition.animateDp {
                        if (it == ScreenState.PLAYER) 320.dp else 335.dp
                    }
                    val maskHeight by morphTransition.animateDp {
                        if (it == ScreenState.PLAYER) 320.dp else 400.dp
                    }
                    val maskRadius by morphTransition.animateDp(label = "radius") { state ->
                        if (state == ScreenState.PLAYER) 160.dp else 20.dp
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {

                            val pitch = if (isParallaxEnabled) tiltViewModel.pitch else 0f
                            val roll = if (isParallaxEnabled) tiltViewModel.roll else 0f
                            val totalLift = if (isParallaxEnabled) hypot(pitch, roll).coerceIn(0f, 1f) else 0.5f

                            Box(
                                modifier = Modifier
                                    .width(maskWidth)
                                    .height(maskHeight)
                                    .graphicsLayer {
                                        val p = if (isParallaxEnabled) tiltViewModel.pitch else 0f
                                        val r = if (isParallaxEnabled) tiltViewModel.roll else 0f
                                        val totalLift = if (isParallaxEnabled) hypot(p, r).coerceIn(0f, 1f) else 0.5f

                                        val dynamicBlur = 52.87f + (totalLift * 30f)
                                        renderEffect = RenderEffect
                                            .createBlurEffect(dynamicBlur, dynamicBlur, Shader.TileMode.DECAL)
                                            .asComposeRenderEffect()

                                        translationX = roll * 127.33f
                                        translationY = (-pitch * 127.33f) + 40.00f
                                        alpha = 0.95f * (totalLift * 1.5f).coerceIn(0f, 1f)
                                    }
                                    .background(Color.Black, RoundedCornerShape(maskRadius))
                            )

                            Box(
                                modifier = Modifier
                                    .width(maskWidth)
                                    .height(maskHeight)
                                    .graphicsLayer {
                                        val p = if (isParallaxEnabled) tiltViewModel.pitch else 0f
                                        val r = if (isParallaxEnabled) tiltViewModel.roll else 0f
                                        val totalLift = if (isParallaxEnabled) hypot(p, r).coerceIn(0f, 1f) else 0.5f
                                        val dynamicScale = if (isParallaxEnabled) {
                                            1.0f + (totalLift * 0.10f)
                                        } else {
                                            1.0f
                                        }
                                        scaleX = dynamicScale
                                        scaleY = dynamicScale

                                        translationX = -roll * 60f
                                        translationY = pitch * 60f

                                        rotationX = -pitch * 25f
                                        rotationY = -roll * 25f
                                        cameraDistance = 12f * density
                                    }
                                    .clip(RoundedCornerShape(maskRadius))
                                    .then(
                                        if (currentTheme == "Industrial Yellow") Modifier
                                        else Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(maskRadius))
                                    )
                            ) {
                                AnimatedContent(
                                    targetState = currentScreen,
                                    transitionSpec = {
                                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    label = "ContentFade"
                                ) { targetScreen ->
                                    when (targetScreen) {
                                        ScreenState.PLAYER -> {
                                            val currentAlbumArt = if (musicList.isNotEmpty()) musicList[selectedIndex].albumArtUri else null

                                            NowPlayingDisk(
                                                albumArtUri = currentAlbumArt,
                                                isPlaying = isPlaying
                                            )
                                        }
                                        ScreenState.LIBRARY -> {
                                            FloatingLibraryPanel(
                                                songList = musicList,
                                                selectedIndex = selectedIndex,
                                                listState = listState
                                            )
                                        }
                                        ScreenState.MENU -> {
                                            MainMenuPanel(
                                                menuItems = menuItems,
                                                selectedIndex = selectedMenuIndex
                                            )
                                        }
                                        ScreenState.SETTINGS -> {
                                            SettingsPanel(
                                                isParallaxEnabled = isParallaxEnabled,
                                                currentTheme = currentTheme,
                                                onThemeClick = {
                                                    currentTheme = if (currentTheme == "Pitch Dark") "Industrial Yellow" else "Pitch Dark"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = currentScreen == ScreenState.PLAYER,
                            enter = fadeIn(tween(400)) + expandVertically(tween(400, easing = FastOutSlowInEasing)),
                            exit = fadeOut(tween(250)) + shrinkVertically(tween(300, easing = FastOutSlowInEasing))
                        ) {
                            Column(
                                modifier = Modifier.width(320.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(32.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatTime(currentPosition),
                                        color = Color(0xFFBFC7E5).copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                    Text(
                                        text = "-" + formatTime(totalDuration - currentPosition),
                                        color = Color(0xFFBFC7E5).copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                VolumeDotsControl(
                                    currentVolume = currentVolume,
                                    onVolumeChange = { newVol ->
                                        currentVolume = newVol
                                        exoPlayer.volume = newVol
                                    }
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = currentScreen != ScreenState.PLAYER && musicList.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 32.dp, bottom = 32.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(musicList[selectedIndex].albumArtUri)
                                        .crossfade(true)
                                        .size(100)
                                        .build(),
                                    contentDescription = "Mini Player Shortcut",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        .clickable {
                                            currentScreen = ScreenState.PLAYER
                                        }
                                )
                            }
                        }
                    }

                    iPodClickWheel(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp),
                        wheelColor = if (currentTheme == "Industrial Yellow") Color(0xFF0D0D0D) else Color.White.copy(alpha = 0.12f),
                        centerButtonColor = if (currentTheme == "Industrial Yellow") Color(0xFFFFD100) else Color.White.copy(alpha = 0.18f),
                        iconColor = if (currentTheme == "Industrial Yellow") Color(0xFFFFD100) else Color(0xFFE8EDF7).copy(alpha = 0.9f),
                        onScrollDown = {
                            when (currentScreen) {
                                ScreenState.MENU -> {
                                    if (selectedMenuIndex < menuItems.size - 1) selectedMenuIndex++
                                }
                                ScreenState.LIBRARY -> {
                                    if (musicList.isNotEmpty() && selectedIndex < musicList.size - 1) {
                                        selectedIndex++
                                        coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                                    }
                                }
                                ScreenState.PLAYER -> {}
                                ScreenState.SETTINGS -> {}
                            }
                        },
                        onScrollUp = {
                            when (currentScreen) {
                                ScreenState.MENU -> {
                                    if (selectedMenuIndex > 0) selectedMenuIndex--
                                }
                                ScreenState.LIBRARY -> {
                                    if (musicList.isNotEmpty() && selectedIndex > 0) {
                                        selectedIndex--
                                        coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                                    }
                                }
                                ScreenState.PLAYER -> {}
                                ScreenState.SETTINGS -> {}
                            }
                        },
                        onCenterClick = {
                            when (currentScreen) {
                                ScreenState.MENU -> {
                                    val selectedOption = menuItems[selectedMenuIndex]
                                    if (selectedOption == "Library") {
                                        currentScreen = ScreenState.LIBRARY
                                    } else if (selectedOption == "Settings") {
                                        currentScreen = ScreenState.SETTINGS
                                    }
                                }
                                ScreenState.SETTINGS -> {
                                    isParallaxEnabled = !isParallaxEnabled
                                }
                                ScreenState.LIBRARY -> {
                                    if (musicList.isNotEmpty()) {
                                        currentScreen = ScreenState.PLAYER
                                        val currentSong = musicList[selectedIndex]
                                        exoPlayer.setMediaItem(MediaItem.fromUri(currentSong.uri))
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    }
                                }
                                ScreenState.PLAYER -> {
                                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                }
                            }
                        },
                        onMenuClick = {
                            when (currentScreen) {
                                ScreenState.PLAYER -> currentScreen = ScreenState.LIBRARY
                                ScreenState.LIBRARY, ScreenState.SETTINGS -> currentScreen = ScreenState.MENU
                                ScreenState.MENU -> {}
                            }
                        },
                        onPlayPauseClick = {
                            if (musicList.isNotEmpty()) {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    if (exoPlayer.playbackState == Player.STATE_IDLE) {
                                        exoPlayer.setMediaItem(MediaItem.fromUri(musicList[selectedIndex].uri))
                                        exoPlayer.prepare()
                                    }
                                    exoPlayer.play()
                                }
                            }
                        },
                        onPrevClick = {
                            if (musicList.isNotEmpty() && selectedIndex > 0) {
                                selectedIndex--
                                val newSong = musicList[selectedIndex]
                                exoPlayer.setMediaItem(MediaItem.fromUri(newSong.uri))
                                exoPlayer.prepare()
                                exoPlayer.play()

                                coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                            }
                        },
                        onNextClick = {
                            if (musicList.isNotEmpty() && selectedIndex < musicList.size - 1) {
                                selectedIndex++
                                val newSong = musicList[selectedIndex]
                                exoPlayer.setMediaItem(MediaItem.fromUri(newSong.uri))
                                exoPlayer.prepare()
                                exoPlayer.play()

                                coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                            }
                        }
                    )
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}