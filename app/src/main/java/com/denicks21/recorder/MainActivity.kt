package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var startTV: TextView
    lateinit var stopTV: TextView
    lateinit var playTV: TextView
    lateinit var statusTV: TextView
    lateinit var recordText: TextView
    lateinit var playRecording: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    private var isRecording = false
    private var isPlaying = false
    private var hasOneRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordText = findViewById(R.id.recordText)
        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        stopTV = findViewById(R.id.btnStop)
        playTV = findViewById(R.id.btnPlay)
        playRecording = findViewById(R.id.recordingStatus)

        startTV.setOnClickListener {
            if (!isRecording) {
                startRecording()
                startTV.setBackgroundResource(R.drawable.btn_rec_stop_play)
                recordText.text = "Stop Recording"
            } else {
                pauseRecording()
                startTV.setBackgroundResource(R.drawable.btn_rec_start)
                recordText.text = "Start Recording"
                hasOneRecording = true
            }
        }

        stopTV.setOnClickListener {
            pauseRecording()
        }

        playTV.setOnClickListener {
            if (!isPlaying and hasOneRecording) {
                playAudio()
                playRecording.text = "Pause Last Recording"
                playTV.setBackgroundResource(R.drawable.btn_rec_pause)
            } else {
                pausePlaying()
                playRecording.text = "Play Last Recording"
                playTV.setBackgroundResource(R.drawable.btn_rec_play)
            }
        }
    }

    private fun startRecording() {
// Check permissions
        if (CheckPermissions()) {
            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath, "Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath, "Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            statusTV.text = "Recording in progress"
            isRecording = true
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                } else {
                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun CheckPermissions(): Boolean {
        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {
        // Request permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    private fun playAudio() {
        // Use the MediaPlayer class to listen to recorded audio files
        mPlayer = MediaPlayer()
        try {
            // Fetch the source of the mPlayer
            mPlayer!!.setDataSource(mFileName.toString())

            // Fetch the source of the mPlayer
            mPlayer!!.prepare()

            // Start the mPlayer
            mPlayer!!.start()
            statusTV.text = "Listening recording"
            isPlaying = true
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
        }
    }

    private fun pauseRecording() {
        // Stop recording
        if (mFileName == null || !isRecording) {
            // Message
            Toast.makeText(
                getApplicationContext(),
                "Recording not in progress",
                Toast.LENGTH_LONG
            ).show()
        } else {
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            statusTV.text = "Recording interrupted"
            isRecording = false
        }
    }

    private fun pausePlaying() {
        // Stop playing the audio file
        if (isPlaying) {
            mPlayer!!.stop()
            mPlayer!!.release()
            mPlayer = null
            statusTV.text = "Recording stopped"
            isPlaying = false
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}