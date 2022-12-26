package com.example.videoplayerpoc.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.videoplayerpoc.model.VideoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExoPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val player: Player
): ViewModel() {
    private val videoUris = savedStateHandle.getStateFlow(VIDEO_URIS, emptyList<Uri>())

    val videoItems = videoUris.map { uris ->
        val count = uris.size
        uris.map { uri ->
            VideoItem(
                contentUri = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = "video $count"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        player.prepare()
        player.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun addVideoUri(uri: Uri) {
        savedStateHandle[VIDEO_URIS] = videoUris.value + uri
        player.addMediaItem(MediaItem.fromUri(uri))
        if(player.mediaItemCount == 1) {
            playVideo(uri)
        }
    }

    private fun playVideo(uri: Uri) {
        player.setMediaItem(
            videoItems.value.find { it.contentUri == uri }?.mediaItem ?: return
        )
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    companion object {
        const val VIDEO_URIS = "video uris"
    }
}