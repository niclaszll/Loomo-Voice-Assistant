package com.kp.loomo.features.speech

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.google.protobuf.ByteString
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private const val TAG = "Audio"

/**
 * Emits microphone audio using a background [ScheduledExecutorService].
 */
internal class AudioEmitter {

    private var mAudioRecorder: AudioRecord? = null
    private var mAudioExecutor: ScheduledExecutorService? = null
    private lateinit var mBuffer: ByteArray

    /** Start streaming  */
    fun start(
        encoding: Int = AudioFormat.ENCODING_PCM_16BIT,
        channel: Int = AudioFormat.CHANNEL_IN_MONO,
        sampleRate: Int = 16000,
        subscriber: (ByteString) -> Unit
    ) {
        mAudioExecutor = Executors.newSingleThreadScheduledExecutor()

        mBuffer = ByteArray(2 * AudioRecord.getMinBufferSize(sampleRate, channel, encoding))

        // create and configure recorder
        // Note: ensure settings are matching the speech recognition config
        mAudioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channel,
            encoding,
            mBuffer.size)

        // start recording
        Log.d(TAG, "Recording audio with buffer size of: ${mBuffer.size} bytes")
        mAudioRecorder!!.startRecording()

        // stream bytes as they become available in chunks equal to the buffer size
        mAudioExecutor!!.scheduleAtFixedRate({
            // read audio data
            val read = mAudioRecorder!!.read(
                mBuffer, 0, mBuffer.size)

            // send next chunk
            if (read > 0) {
                subscriber(ByteString.copyFrom(mBuffer, 0, read))
            }
        }, 0, 10, TimeUnit.MILLISECONDS)
    }

    /** Stop Streaming  */
    fun stop() {
        // stop events
        mAudioExecutor?.shutdown()
        mAudioExecutor = null

        // stop recording
        mAudioRecorder?.stop()
        mAudioRecorder?.release()
        mAudioRecorder = null
    }
}