package com.dutch.composevideoplayer.model

import android.net.Uri
import com.google.android.exoplayer2.MediaItem


data class VideoItem(
    val contentUri: Uri,
    val mediaItem: MediaItem,
    val name: String
)
