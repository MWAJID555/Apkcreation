package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class FeedbackManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.w("FeedbackManager", "Unable to create ToneGenerator: ${e.message}")
        }
    }

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    fun playClick(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(30)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 35)
        }
    }

    fun playLap(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(50)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_ACK, 60)
        }
    }

    fun playStart(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibratePattern(longArrayOf(0, 30, 40, 60))
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_CDMA_PIP, 80)
        }
    }

    fun playStop(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(70)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_NACK, 90)
        }
    }

    fun playCountdown(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(40)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 100)
        }
    }

    fun playWorkPhase(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibratePattern(longArrayOf(0, 80, 50, 120))
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
        }
    }

    fun playRestPhase(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(90)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_CDMA_LOW_L, 200)
        }
    }

    fun playFinished(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibratePattern(longArrayOf(0, 100, 50, 100, 50, 200))
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_PROMPT, 400)
        }
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.w("FeedbackManager", "Tone playback failed: ${e.message}")
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
