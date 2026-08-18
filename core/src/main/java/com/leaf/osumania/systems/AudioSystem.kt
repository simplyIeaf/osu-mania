package com.leaf.osumania.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem

class AudioSystem : Disposable {
    var song: Music? = null; private set
    private val hitsounds = mutableMapOf<String, Sound>()
    var musicVolume: Float = 0.8f
    var sfxVolume: Float = 0.8f
    private var songPosition: Float = 0f

    fun loadSong(bytes: ByteArray) {
        song?.stop()
        song?.dispose()

        val tempFile = java.io.File.createTempFile("osumania_song", ".wav")
        tempFile.deleteOnExit()

        try {
            val inputStream = ByteArrayInputStream(bytes)
            val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
            val ais = AudioSystem.getAudioInputStream(
                javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    audioInputStream.format.sampleRate,
                    16,
                    audioInputStream.format.channels,
                    audioInputStream.format.channels * 2,
                    audioInputStream.format.sampleRate,
                    false
                ),
                audioInputStream
            )
            val bufferedOutput = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (ais.read(buffer).also { bytesRead = it } != -1) {
                bufferedOutput.write(buffer, 0, bytesRead)
            }
            val pcmData = bufferedOutput.toByteArray()
            val sampleRate = audioInputStream.format.sampleRate.toInt()
            val channels = audioInputStream.format.channels

            tempFile.writeBytes(pcmData)
        } catch (e: Exception) {
            tempFile.writeBytes(bytes)
        }

        song = Gdx.audio.newMusic(Gdx.files.absolute(tempFile.absolutePath))
    }

    fun playSong(rate: Float = 1f) {
        song?.let {
            it.volume = musicVolume
            it.play()
            it.setPosition(songPosition)
        }
    }

    fun pauseSong() {
        song?.let {
            songPosition = it.position
            it.pause()
        }
    }

    fun stopSong() {
        song?.stop()
        songPosition = 0f
    }

    fun seek(position: Float): Float {
        song?.let {
            it.setPosition(position)
            songPosition = position
        }
        return songPosition
    }

    fun isPlaying(): Boolean = song?.isPlaying ?: false

    fun playHitsound(set: String, sample: String, volume: Float) {
        val key = "${set}_${sample}"
        val sound = hitsounds[key]
        sound?.play(sfxVolume * volume)
    }

    fun registerHitsound(key: String, sound: Sound) {
        hitsounds[key] = sound
    }

    fun setMusicVolume(vol: Float) {
        musicVolume = vol
        song?.volume = vol
    }

    fun setSfxVolume(vol: Float) {
        sfxVolume = vol
    }

    override fun dispose() {
        song?.dispose()
        song = null
        hitsounds.values.forEach { it.dispose() }
        hitsounds.clear()
    }
}
