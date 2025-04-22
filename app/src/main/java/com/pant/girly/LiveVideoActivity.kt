package com.pant.girly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LiveVideoActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var switchAudio: Switch
    private lateinit var switchVideo: Switch
    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnCaptureSnapshot: ImageButton

    private lateinit var mediaRecorder: MediaRecorder
    private var isAudioEnabled = false
    private var isVideoEnabled = false
    private var isRecording = false
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_video)

        previewView = findViewById(R.id.previewView)
        switchAudio = findViewById(R.id.switchAudio)
        switchVideo = findViewById(R.id.switchVideo)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnCaptureSnapshot = findViewById(R.id.btnCaptureSnapshot)

        cameraExecutor = Executors.newSingleThreadExecutor()

        switchAudio.setOnCheckedChangeListener { _, isChecked ->
            isAudioEnabled = isChecked
        }

        switchVideo.setOnCheckedChangeListener { _, isChecked ->
            isVideoEnabled = isChecked
            if (isChecked) startCamera()
        }

        btnStartRecording.setOnClickListener {
            if (isAudioEnabled || isVideoEnabled) {
                if (isVideoEnabled) startCamera()
                startRecording()
                notifyEmergencyContacts("Recording started")
            } else {
                Toast.makeText(this, "Enable Audio or Video first", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopRecording.setOnClickListener {
            stopRecording()
        }

        btnCaptureSnapshot.setOnClickListener {
            if (isVideoEnabled) {
                takePhoto()
            } else {
                Toast.makeText(this, "Enable Video first", Toast.LENGTH_SHORT).show()
            }
        }

        if (!checkPermissions()) {
            requestPermissions()
        }

        // Auto-start if triggered from SOS
        if (intent.getBooleanExtra("startRecording", false)) {
            switchAudio.isChecked = true
            switchVideo.isChecked = true
            isAudioEnabled = true
            isVideoEnabled = true
            startCamera()
            startRecording()
            notifyEmergencyContacts("SOS Activated - Recording started")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("LiveVideo", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        Toast.makeText(this@LiveVideoActivity,
                            "Snapshot captured", Toast.LENGTH_SHORT).show()
                        uploadToFirebase(photoFile)
                        notifyEmergencyContacts("Snapshot captured at ${Date()}")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("LiveVideo", "Photo capture failed: ${exception.message}", exception)
                    runOnUiThread {
                        Toast.makeText(this@LiveVideoActivity,
                            "Capture failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun startRecording() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(externalCacheDir, "recording_$timestamp.mp4")

        mediaRecorder = MediaRecorder().apply {
            if (isAudioEnabled) setAudioSource(MediaRecorder.AudioSource.MIC)
            if (isVideoEnabled) setVideoSource(MediaRecorder.VideoSource.SURFACE)

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            if (isVideoEnabled) setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (isAudioEnabled) setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            setVideoFrameRate(30)
            setVideoSize(1280, 720)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
                isRecording = true
                Toast.makeText(this@LiveVideoActivity, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LiveVideo", "Recording failed: ${e.message}")
                Toast.makeText(this@LiveVideoActivity, "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop()
                mediaRecorder.release()
                isRecording = false
                Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LiveVideo", "Error stopping: ${e.message}")
                Toast.makeText(this, "Stop failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadToFirebase(file: File) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("snapshots/${file.name}")

        imageRef.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                Toast.makeText(this, "Snapshot uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("LiveVideo", "Upload failed", e)
            }
    }

    private fun notifyEmergencyContacts(message: String) {
        // Replace with your actual contacts from Firebase
        val contacts = listOf("9876543210", "9123456780")

        contacts.forEach { number ->
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$number")
                putExtra("sms_body", "Girly Safety Alert:\n$message\nLocation: [LAT],[LONG]")
            }
            startActivity(smsIntent)
        }
    }

    private fun checkPermissions(): Boolean {
        val audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val video = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return audio == PackageManager.PERMISSION_GRANTED &&
                video == PackageManager.PERMISSION_GRANTED &&
                storage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        if (isRecording) stopRecording()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}