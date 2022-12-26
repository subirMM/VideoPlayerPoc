package com.example.videoplayerpoc.ui

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.videoplayerpoc.ui.theme.VideoPlayerComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val videoUri: Uri =
        Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoPlayerComposeTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(modifier = Modifier.aspectRatio(1.3f)) {
                            AndroidView(
                                factory = { context ->
                                    val mediaController = MediaController(context)
                                    val videoView = VideoView(context)
                                    videoView.setVideoURI(videoUri)
                                    videoView.setMediaController(mediaController)

                                    mediaController.setAnchorView(videoView)
                                    mediaController.setMediaPlayer(videoView)
                                    videoView.also { it.start() }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Button(
                            onClick = {
                                startActivity(ExoPlayerActivity.create(this@MainActivity))
                            }) {
                            Text(text = "Play Video on ExoPlayer")
                        }

                        Button(
                            onClick = {

                            }) {
                            Text(text = "Upload New Video")
                        }
                    }
                }
            }
        }
    }
}