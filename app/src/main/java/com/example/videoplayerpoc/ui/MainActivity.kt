package com.example.videoplayerpoc.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
                                val intent = Intent(Intent.ACTION_PICK, null)
                                intent.type = "video/*"
                                resultLauncher.launch(intent)
                            }) {
                            Text(text = "Upload New Video")
                        }
                    }
                }
            }
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data

                // For null safety, make sure data is not null
                if (data == null) {
                    Log.d("TAG", "Data returned is null")
                    return@registerForActivityResult
                }

                val videoUri: Uri? = data.data
                videoUri?.let {
                    val videoPath = parsePath(videoUri)
                    startActivity(ExoPlayerActivity.create(this@MainActivity, uriPath = videoPath))
                }
            }
        }

    private fun parsePath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = this
            .contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close() // Make sure you close cursor after use

            path
        } else null
    }
}