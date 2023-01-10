package com.example.videoplayerpoc.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CustomVideoRecorder {

    private var camera: Camera? = null

    private var videoCapture: VideoCapture<Recorder>? = null

    private var cameraProvider: ProcessCameraProvider? = null

    private var videoCaptureResult: VideoRecorderEventListener? = null

    private var activityContext: Context? = null

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var currentRecording: Recording? = null

    private var fileName: String? = null

    private fun stopPreview() {
        // Unbind use cases before rebinding
        cameraProvider?.unbindAll()
    }

    fun startCameraPreview(
        context: Context,
        cameraPreview: PreviewView,
        fileName: String,
        videoCaptureResult: VideoRecorderEventListener
    ) {
        this.activityContext = context
        this.videoCaptureResult = videoCaptureResult
        this.fileName = fileName

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        // listening for data from the camera
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()

            //Create the VideoCapture UseCase and make it available to use
            //in other parts of the application
            videoCapture = VideoCapture.withOutput(recorder)

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(cameraPreview.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraPreview.post {
                stopPreview()

                // Attach the surfaceProvider to the preview use case to start preview
                preview.setSurfaceProvider(cameraPreview.surfaceProvider)

                cameraProvider?.unbindAll()
                // Bind the preview use case and other needed user cases to a lifecycle
                camera = cameraProvider?.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(context: Context) {
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = fileName
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            currentRecording = videoCapture?.output
                ?.prepareRecording(context, mediaStoreOutput)
                ?.withAudioEnabled()
                ?.start(cameraExecutor, captureListener)
        }
    }

    private fun stopRecording() {
        currentRecording?.stop()
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        val durationInNanos: Long = event.recordingStats.recordedDurationNanos
        val durationInSeconds: Double = durationInNanos / 1000 / 1000 / 1000.0
        if (durationInSeconds >= 10) {
            if (currentRecording != null) {
                stopRecording()
            }
        }
        if (event is VideoRecordEvent.Finalize) {
            // display the captured video
            val videoUri = event.outputResults.outputUri
            videoCaptureResult?.onVideoCapture(videoUri)
        }
    }

    interface VideoRecorderEventListener {

        fun onVideoCapture(capturedVideoUri: Uri)
    }
}