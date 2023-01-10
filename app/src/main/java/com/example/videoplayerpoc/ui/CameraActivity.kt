package com.example.videoplayerpoc.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.videoplayerpoc.databinding.ActivityCustomCameraBinding
import com.example.videoplayerpoc.util.CustomVideoRecorder
import com.example.videoplayerpoc.util.PermissionUtils.arePermissionsGranted
import com.example.videoplayerpoc.util.PermissionUtils.isPermissionDenied
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private var binding: ActivityCustomCameraBinding? = null

    @Inject lateinit var customCameraHelper: CustomVideoRecorder

    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var nonGrantedPermission = 0
            permissions.entries.map { entry ->
                if(!entry.value) {
                    nonGrantedPermission++
                }
            }
            if (nonGrantedPermission == 0) {
                setupCamera()
            } else {
                finish()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (arePermissionsGranted(this, REQUIRED_PERMISSIONS)) {
                setupCamera()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupListeners()
        checkRequiredPermissions()
    }

    private fun setupListeners() {
        binding?.apply {
            cameraCaptureButton.setOnClickListener {
                customCameraHelper.startRecording(this@CameraActivity)
            }
        }
    }

    private fun checkRequiredPermissions() {
        when {
            arePermissionsGranted(this, REQUIRED_PERMISSIONS) -> setupCamera()
            isPermissionDenied(this, Manifest.permission.CAMERA) -> requestPermissions()
            else -> requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestMultiplePermission.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun setupCamera() {
        binding?.apply {
            customCameraHelper.startCameraPreview(this@CameraActivity, previewView, FILE_NAME,
                object : CustomVideoRecorder.VideoRecorderEventListener {

                    override fun onVideoCapture(capturedVideoUri: Uri) {
                        val resultIntent = Intent()
                        resultIntent.putExtra(CAPTURED_VIDEO_URI, capturedVideoUri.toString())
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                })
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val CAPTURED_VIDEO_URI = "CAPTURED_VIDEO_URI"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        private lateinit var FILE_PATH: String
        private lateinit var FILE_NAME: String

        @JvmStatic
        fun create(context: Context, filePath: String, fileName: String): Intent {
            FILE_PATH = filePath
            FILE_NAME = fileName
            return Intent(context, CameraActivity::class.java)
        }
    }
}