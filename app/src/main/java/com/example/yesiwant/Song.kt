package com.example.yesiwant

import android.net.Uri


data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val durationFormatted: String,
    val uri: Uri,
    val albumArtUri: Uri
)