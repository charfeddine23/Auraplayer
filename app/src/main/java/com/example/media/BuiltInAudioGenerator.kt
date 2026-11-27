package com.example.media

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates real playable ambient harmonic PCM WAV files in app cache for the
 * built-in starter tracks so ExoPlayer + MediaSessionService produce genuine
 * audio output and full OS media banner integration even before scanning local files.
 */
object BuiltInAudioGenerator {

    private const val SAMPLE_RATE = 22050
    private const val DURATION_SECONDS = 24

    suspend fun ensurePlayableUri(context: Context, trackId: String): Uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "aura_built_in_audio").apply {
            if (!exists()) mkdirs()
        }
        val safeName = trackId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outFile = File(dir, "${safeName}_v1.wav")
        if (outFile.exists() && outFile.length() > 44L) {
            return@withContext Uri.fromFile(outFile)
        }

        val totalSamples = SAMPLE_RATE * DURATION_SECONDS
        val chordProgression: List<DoubleArray> = when (trackId) {
            "track_2" -> listOf(
                doubleArrayOf(220.00, 261.63, 329.63, 392.00), // Am7
                doubleArrayOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
                doubleArrayOf(196.00, 246.94, 293.66, 392.00), // G6
                doubleArrayOf(164.81, 246.94, 293.66, 392.00)  // Em7
            )
            "track_3" -> listOf(
                doubleArrayOf(146.83, 220.00, 277.18, 349.23), // Dm(maj7) / Hijaz color
                doubleArrayOf(155.56, 233.08, 293.66, 349.23), // Eb
                doubleArrayOf(130.81, 196.00, 261.63, 329.63), // C
                doubleArrayOf(146.83, 220.00, 293.66, 349.23)  // Dm
            )
            else -> listOf(
                doubleArrayOf(196.00, 246.94, 293.66, 369.99), // Gmaj7
                doubleArrayOf(164.81, 246.94, 293.66, 392.00), // Em7
                doubleArrayOf(130.81, 196.00, 246.94, 329.63), // Cmaj7
                doubleArrayOf(146.83, 220.00, 261.63, 329.63)  // D9sus4
            )
        }

        val dataSize = totalSamples * 2
        val byteRate = SAMPLE_RATE * 2
        val fadeSamples = (SAMPLE_RATE * 0.45).toInt()
        val chordSamples = totalSamples / chordProgression.size
        val twoPi = 2.0 * PI

        BufferedOutputStream(FileOutputStream(outFile), 32768).use { out ->
            // RIFF WAV header (mono, 16-bit PCM)
            out.write(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
            writeIntLe(out, 36 + dataSize)
            out.write(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))
            out.write(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
            writeIntLe(out, 16) // PCM chunk size
            writeShortLe(out, 1) // AudioFormat = PCM
            writeShortLe(out, 1) // NumChannels = 1
            writeIntLe(out, SAMPLE_RATE)
            writeIntLe(out, byteRate)
            writeShortLe(out, 2) // BlockAlign
            writeShortLe(out, 16) // BitsPerSample
            out.write(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
            writeIntLe(out, dataSize)

            val buffer = ByteArray(4096)
            var bufIdx = 0
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE.toDouble()
                val chordIndex = (i / chordSamples).coerceIn(0, chordProgression.lastIndex)
                val chord = chordProgression[chordIndex]
                val localPos = (i % chordSamples).toDouble() / chordSamples.toDouble()

                // Soft crossfade envelope per chord + overall loop fade
                val chordEnv = sin(PI * localPos).coerceIn(0.18, 1.0)
                val globalEnv = when {
                    i < fadeSamples -> i.toDouble() / fadeSamples
                    i > totalSamples - fadeSamples -> (totalSamples - i).toDouble() / fadeSamples
                    else -> 1.0
                }.coerceIn(0.0, 1.0)

                // Warm ambient pad + gentle melodic shimmer
                var sample = 0.0
                for (noteIdx in chord.indices) {
                    val freq = chord[noteIdx]
                    val weight = when (noteIdx) {
                        0 -> 0.34
                        1 -> 0.26
                        2 -> 0.22
                        else -> 0.18
                    }
                    sample += sin(twoPi * freq * t) * weight
                }
                val pulseFreq = chord[(i / (SAMPLE_RATE / 2)) % chord.size] * 2.0
                val pulseEnv = ((sin(twoPi * 2.0 * t) + 1.0) * 0.5)
                sample += sin(twoPi * pulseFreq * t) * 0.09 * pulseEnv

                val pcm = (sample * chordEnv * globalEnv * 9000.0)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                buffer[bufIdx++] = (pcm and 0xFF).toByte()
                buffer[bufIdx++] = ((pcm shr 8) and 0xFF).toByte()
                if (bufIdx >= buffer.size) {
                    out.write(buffer, 0, bufIdx)
                    bufIdx = 0
                }
            }
            if (bufIdx > 0) {
                out.write(buffer, 0, bufIdx)
            }
        }

        Uri.fromFile(outFile)
    }

    private fun writeIntLe(out: BufferedOutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 24) and 0xFF)
    }

    private fun writeShortLe(out: BufferedOutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
    }
}
