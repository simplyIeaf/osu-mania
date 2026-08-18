package com.leaf.osumania.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable

class AudioSystem : Disposable {
    var song: Music? = null; private set
    private val hitsounds = mutableMapOf<String, Sound>()
    var musicVolume: Float = 0.8f
    var sfxVolume: Float = 0.8f
    private var songPosition: Float = 0f
    var isPlaying: Boolean = false; private set

    fun loadSong(bytes: ByteArray) {
        song?.stop()
        song?.dispose()
        val tempFile = java.io.File.createTempFile("osumania_song", ".mp3")
        tempFile.deleteOnExit()
        tempFile.writeBytes(bytes)
        song = Gdx.audio.newMusic(Gdx.files.absolute(tempFile.absolutePath))
    }

    fun loadSong(path: String) {
        song?.stop()
        song?.dispose()
        song = Gdx.audio.newMusic(Gdx.files.internal(path))
    }

    fun playSong(rate: Float = 1f) {
        song?.let {
            it.volume = musicVolume
            it.play()
            it.setPosition(songPosition)
            isPlaying = true
        }
    }

    fun pauseSong() {
        song?.let {
            songPosition = it.position
            it.pause()
        }
        isPlaying = false
    }

    fun stopSong() {
        song?.stop()
        songPosition = 0f
        isPlaying = false
    }

    fun pause() {
        pauseSong()
    }

    fun resume() {
        song?.play()
        isPlaying = true
    }

    fun stop() {
        stopSong()
    }

    fun seekTo(position: Float) {
        song?.setPosition(position)
        songPosition = position
    }

    fun getCurrentPosition(): Float {
        return song?.position ?: songPosition
    }

    fun playColumnHitsound(column: Int) {
        val key = "hit_normal"
        val sound = hitsounds[key]
        sound?.play(sfxVolume)
    }

    fun playJudgementHitsound(judgement: Int) {
        when (judgement) {
            320 -> playNamedHitsound("hitclap")
            300 -> playNamedHitsound("hitnormal")
            200 -> playNamedHitsound("hitwhistle")
            else -> {}
        }
    }

    fun playNamedHitsound(sample: String, volume: Float = 1f) {
        val key = "hit_$sample"
        val sound = hitsounds[key]
        sound?.play(sfxVolume * volume)
    }

    fun playMissSound() {
        val sound = hitsounds["miss"]
        sound?.play(sfxVolume)
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
