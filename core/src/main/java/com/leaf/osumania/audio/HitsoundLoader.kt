package com.leaf.osumania.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle

class HitsoundLoader {

    private val sounds = mutableMapOf<String, Sound>()
    private val fallbackSounds = mutableMapOf<String, Sound>()

    fun loadFromAssets(): Map<String, Sound> {
        sounds.clear()

        loadSound("skin-normal-hitwhistle", "skin/sounds/hitsounds/normal-hitwhistle.wav")
        loadSound("skin-normal-hitfinish", "skin/sounds/hitsounds/normal-hitfinish.wav")
        loadSound("skin-normal-hitclap", "skin/sounds/hitsounds/normal-hitclap.wav")
        loadSound("skin-normal-hitnormal", "skin/sounds/hitsounds/normal-hitnormal.wav")
        loadSound("skin-soft-hitwhistle", "skin/sounds/hitsounds/soft-hitwhistle.wav")
        loadSound("skin-soft-hitfinish", "skin/sounds/hitsounds/soft-hitfinish.wav")
        loadSound("skin-soft-hitclap", "skin/sounds/hitsounds/soft-hitclap.wav")
        loadSound("skin-soft-hitnormal", "skin/sounds/hitsounds/soft-hitnormal.wav")
        loadSound("skin-drum-hitwhistle", "skin/sounds/hitsounds/drum-hitwhistle.wav")
        loadSound("skin-drum-hitfinish", "skin/sounds/hitsounds/drum-hitfinish.wav")
        loadSound("skin-drum-hitclap", "skin/sounds/hitsounds/drum-hitclap.wav")
        loadSound("skin-drum-hitnormal", "skin/sounds/hitsounds/drum-hitnormal.wav")

        loadFallbackSound("skin-normal-hitnormal", "sounds/hitnormal.wav")
        loadFallbackSound("skin-normal-hitwhistle", "sounds/hitwhistle.wav")
        loadFallbackSound("skin-normal-hitfinish", "sounds/hitfinish.wav")
        loadFallbackSound("skin-normal-hitclap", "sounds/hitclap.wav")
        loadFallbackSound("skin-soft-hitnormal", "sounds/soft-hitnormal.wav")
        loadFallbackSound("skin-soft-hitwhistle", "sounds/soft-hitwhistle.wav")
        loadFallbackSound("skin-soft-hitfinish", "sounds/soft-hitfinish.wav")
        loadFallbackSound("skin-soft-hitclap", "sounds/soft-hitclap.wav")
        loadFallbackSound("skin-drum-hitnormal", "sounds/drum-hitnormal.wav")
        loadFallbackSound("skin-drum-hitwhistle", "sounds/drum-hitwhistle.wav")
        loadFallbackSound("skin-drum-hitfinish", "sounds/drum-hitfinish.wav")
        loadFallbackSound("skin-drum-hitclap", "sounds/drum-hitclap.wav")

        loadFallbackSound("skin-normal-hitnormal", "sounds/normal-hitnormal.wav")
        loadFallbackSound("skin-normal-hitwhistle", "sounds/normal-hitwhistle.wav")
        loadFallbackSound("skin-normal-hitfinish", "sounds/normal-hitfinish.wav")
        loadFallbackSound("skin-normal-hitclap", "sounds/normal-hitclap.wav")
        loadFallbackSound("skin-soft-hitnormal", "sounds/soft-hitnormal.wav")
        loadFallbackSound("skin-soft-hitwhistle", "sounds/soft-hitwhistle.wav")
        loadFallbackSound("skin-soft-hitfinish", "sounds/soft-hitfinish.wav")
        loadFallbackSound("skin-soft-hitclap", "sounds/soft-hitclap.wav")
        loadFallbackSound("skin-drum-hitnormal", "sounds/drum-hitnormal.wav")
        loadFallbackSound("skin-drum-hitwhistle", "sounds/drum-hitwhistle.wav")
        loadFallbackSound("skin-drum-hitfinish", "sounds/drum-hitfinish.wav")
        loadFallbackSound("skin-drum-hitclap", "sounds/drum-hitclap.wav")

        return sounds.toMap()
    }

    fun getHitsoundKey(sampleSet: String, sample: String): String {
        val set = sampleSet.lowercase().trim()
        val smp = sample.lowercase().trim()
        return "skin-$set-hit$smp"
    }

    fun getSound(key: String): Sound? {
        return sounds[key] ?: fallbackSounds[key]
    }

    private fun loadSound(key: String, path: String) {
        if (sounds.containsKey(key)) return
        try {
            val handle: FileHandle = Gdx.files.internal(path)
            if (handle.exists()) {
                sounds[key] = Gdx.audio.newSound(handle)
            }
        } catch (_: Exception) {
        }
    }

    private fun loadFallbackSound(key: String, path: String) {
        if (sounds.containsKey(key)) return
        if (fallbackSounds.containsKey(key)) return
        try {
            val handle: FileHandle = Gdx.files.internal(path)
            if (handle.exists()) {
                fallbackSounds[key] = Gdx.audio.newSound(handle)
            }
        } catch (_: Exception) {
        }
    }

    fun dispose() {
        for (sound in sounds.values) {
            sound.dispose()
        }
        sounds.clear()
        for (sound in fallbackSounds.values) {
            sound.dispose()
        }
        fallbackSounds.clear()
    }
}
