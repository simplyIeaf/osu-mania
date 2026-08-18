package com.leaf.osumania.storage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.leaf.osumania.engine.GameConstants

class SettingsStore {

    var scrollSpeed: Float = 20f
    var hitPositionOffset: Float = 130f
    var noteOffset: Float = 0f
    var noteScale: Float = 0.8f
    var laneWidthAdjustment: Float = 0f
    var laneSpacing: Float = 0f
    var skinStyle: GameConstants.SkinStyle = GameConstants.SkinStyle.CIRCLE
    var hue: Float = 212f
    var backgroundDim: Float = 0.75f
    var backgroundVideo: Boolean = true
    var stageOpacity: Float = 0.5f
    var receptorOpacity: Float = 1f
    var receptorLighting: Boolean = true
    var performanceMode: Boolean = false
    var upscroll: Boolean = false
    var showScore: Boolean = true
    var showCombo: Boolean = true
    var showAccuracy: Boolean = true
    var showJudgement: Boolean = true
    var show300g: Boolean = true
    var showHealthBar: Boolean = true
    var showErrorBar: Boolean = true
    var errorBarScale: Float = 1f
    var showFps: Boolean = false
    var kpsCounter: String = "left"
    var judgementCounter: String = "left"
    var progressDisplay: String = "bar"
    var musicVolume: Float = 1f
    var sfxVolume: Float = 0.4f
    var audioOffset: Float = 0f
    var ignoreBeatmapHitsounds: Boolean = false
    var darkerHoldNotes: Boolean = true
    var stageHudYPosition: Float = 0.5f
    var keyCount: Int = 4
    var keybinds: MutableMap<Int, MutableList<IntArray>> = mutableMapOf()
    var stagePosition: Float = 0.5f
    var stageWidth: Float = 0.5f
    var stageXOffset: Float = 0f

    private val prefs: Preferences by lazy {
        Gdx.app.getPreferences("OsuManiaSettings")
    }

    init {
        initDefaultKeybinds()
    }

    private fun initDefaultKeybinds() {
        keybinds[4] = mutableListOf(
            intArrayOf(68, 0),
            intArrayOf(70, 0),
            intArrayOf(74, 0),
            intArrayOf(75, 0)
        )
        keybinds[5] = mutableListOf(
            intArrayOf(68, 0),
            intArrayOf(70, 0),
            intArrayOf(32, 0),
            intArrayOf(74, 0),
            intArrayOf(75, 0)
        )
        keybinds[6] = mutableListOf(
            intArrayOf(83, 0),
            intArrayOf(68, 0),
            intArrayOf(70, 0),
            intArrayOf(74, 0),
            intArrayOf(75, 0),
            intArrayOf(76, 0)
        )
        keybinds[7] = mutableListOf(
            intArrayOf(65, 0),
            intArrayOf(83, 0),
            intArrayOf(68, 0),
            intArrayOf(32, 0),
            intArrayOf(74, 0),
            intArrayOf(75, 0),
            intArrayOf(76, 0)
        )
        keybinds[8] = mutableListOf(
            intArrayOf(49, 0),
            intArrayOf(50, 0),
            intArrayOf(51, 0),
            intArrayOf(52, 0),
            intArrayOf(55, 0),
            intArrayOf(56, 0),
            intArrayOf(57, 0),
            intArrayOf(48, 0)
        )
        keybinds[9] = mutableListOf(
            intArrayOf(49, 0),
            intArrayOf(50, 0),
            intArrayOf(51, 0),
            intArrayOf(52, 0),
            intArrayOf(53, 0),
            intArrayOf(55, 0),
            intArrayOf(56, 0),
            intArrayOf(57, 0),
            intArrayOf(48, 0)
        )
        keybinds[10] = mutableListOf(
            intArrayOf(49, 0),
            intArrayOf(50, 0),
            intArrayOf(51, 0),
            intArrayOf(52, 0),
            intArrayOf(53, 0),
            intArrayOf(54, 0),
            intArrayOf(55, 0),
            intArrayOf(56, 0),
            intArrayOf(57, 0),
            intArrayOf(48, 0)
        )
        keybinds[18] = mutableListOf(
            intArrayOf(49, 0),
            intArrayOf(50, 0),
            intArrayOf(51, 0),
            intArrayOf(52, 0),
            intArrayOf(53, 0),
            intArrayOf(54, 0),
            intArrayOf(55, 0),
            intArrayOf(56, 0),
            intArrayOf(57, 0),
            intArrayOf(48, 0),
            intArrayOf(81, 0),
            intArrayOf(87, 0),
            intArrayOf(69, 0),
            intArrayOf(82, 0),
            intArrayOf(84, 0),
            intArrayOf(89, 0),
            intArrayOf(85, 0),
            intArrayOf(73, 0)
        )
    }

    fun save() {
        prefs.putFloat("scrollSpeed", scrollSpeed)
        prefs.putFloat("hitPositionOffset", hitPositionOffset)
        prefs.putFloat("noteOffset", noteOffset)
        prefs.putFloat("noteScale", noteScale)
        prefs.putFloat("laneWidthAdjustment", laneWidthAdjustment)
        prefs.putFloat("laneSpacing", laneSpacing)
        prefs.putString("skinStyle", skinStyle.name)
        prefs.putFloat("hue", hue)
        prefs.putFloat("backgroundDim", backgroundDim)
        prefs.putBoolean("backgroundVideo", backgroundVideo)
        prefs.putFloat("stageOpacity", stageOpacity)
        prefs.putFloat("receptorOpacity", receptorOpacity)
        prefs.putBoolean("receptorLighting", receptorLighting)
        prefs.putBoolean("performanceMode", performanceMode)
        prefs.putBoolean("upscroll", upscroll)
        prefs.putBoolean("showScore", showScore)
        prefs.putBoolean("showCombo", showCombo)
        prefs.putBoolean("showAccuracy", showAccuracy)
        prefs.putBoolean("showJudgement", showJudgement)
        prefs.putBoolean("show300g", show300g)
        prefs.putBoolean("showHealthBar", showHealthBar)
        prefs.putBoolean("showErrorBar", showErrorBar)
        prefs.putFloat("errorBarScale", errorBarScale)
        prefs.putBoolean("showFps", showFps)
        prefs.putString("kpsCounter", kpsCounter)
        prefs.putString("judgementCounter", judgementCounter)
        prefs.putString("progressDisplay", progressDisplay)
        prefs.putFloat("musicVolume", musicVolume)
        prefs.putFloat("sfxVolume", sfxVolume)
        prefs.putFloat("audioOffset", audioOffset)
        prefs.putBoolean("ignoreBeatmapHitsounds", ignoreBeatmapHitsounds)
        prefs.putBoolean("darkerHoldNotes", darkerHoldNotes)
        prefs.putFloat("stageHudYPosition", stageHudYPosition)
        prefs.putInteger("keyCount", keyCount)
        prefs.putFloat("stagePosition", stagePosition)
        prefs.putFloat("stageWidth", stageWidth)
        prefs.putFloat("stageXOffset", stageXOffset)

        for ((kc, binds) in keybinds) {
            val sb = StringBuilder()
            for (bind in binds) {
                sb.append("${bind[0]}:${bind[1]};")
            }
            prefs.putString("keybinds_$kc", sb.toString())
        }

        prefs.flush()
    }

    fun load() {
        scrollSpeed = prefs.getFloat("scrollSpeed", 20f)
        hitPositionOffset = prefs.getFloat("hitPositionOffset", 130f)
        noteOffset = prefs.getFloat("noteOffset", 0f)
        noteScale = prefs.getFloat("noteScale", 0.8f)
        laneWidthAdjustment = prefs.getFloat("laneWidthAdjustment", 0f)
        laneSpacing = prefs.getFloat("laneSpacing", 0f)
        skinStyle = try {
            GameConstants.SkinStyle.valueOf(prefs.getString("skinStyle", "CIRCLE"))
        } catch (_: Exception) {
            GameConstants.SkinStyle.CIRCLE
        }
        hue = prefs.getFloat("hue", 212f)
        backgroundDim = prefs.getFloat("backgroundDim", 0.75f)
        backgroundVideo = prefs.getBoolean("backgroundVideo", true)
        stageOpacity = prefs.getFloat("stageOpacity", 0.5f)
        receptorOpacity = prefs.getFloat("receptorOpacity", 1f)
        receptorLighting = prefs.getBoolean("receptorLighting", true)
        performanceMode = prefs.getBoolean("performanceMode", false)
        upscroll = prefs.getBoolean("upscroll", false)
        showScore = prefs.getBoolean("showScore", true)
        showCombo = prefs.getBoolean("showCombo", true)
        showAccuracy = prefs.getBoolean("showAccuracy", true)
        showJudgement = prefs.getBoolean("showJudgement", true)
        show300g = prefs.getBoolean("show300g", true)
        showHealthBar = prefs.getBoolean("showHealthBar", true)
        showErrorBar = prefs.getBoolean("showErrorBar", true)
        errorBarScale = prefs.getFloat("errorBarScale", 1f)
        showFps = prefs.getBoolean("showFps", false)
        kpsCounter = prefs.getString("kpsCounter", "left")
        judgementCounter = prefs.getString("judgementCounter", "left")
        progressDisplay = prefs.getString("progressDisplay", "bar")
        musicVolume = prefs.getFloat("musicVolume", 1f)
        sfxVolume = prefs.getFloat("sfxVolume", 0.4f)
        audioOffset = prefs.getFloat("audioOffset", 0f)
        ignoreBeatmapHitsounds = prefs.getBoolean("ignoreBeatmapHitsounds", false)
        darkerHoldNotes = prefs.getBoolean("darkerHoldNotes", true)
        stageHudYPosition = prefs.getFloat("stageHudYPosition", 0.5f)
        keyCount = prefs.getInteger("keyCount", 4)
        stagePosition = prefs.getFloat("stagePosition", 0.5f)
        stageWidth = prefs.getFloat("stageWidth", 0.5f)
        stageXOffset = prefs.getFloat("stageXOffset", 0f)

        for (kc in listOf(4, 5, 6, 7, 8, 9, 10, 18)) {
            val raw = prefs.getString("keybinds_$kc", null) ?: continue
            val binds = mutableListOf<IntArray>()
            val entries = raw.split(";")
            for (entry in entries) {
                if (entry.isBlank()) continue
                val parts = entry.split(":")
                if (parts.size >= 2) {
                    try {
                        binds.add(intArrayOf(parts[0].trim().toInt(), parts[1].trim().toInt()))
                    } catch (_: NumberFormatException) {
                    }
                }
            }
            if (binds.isNotEmpty()) {
                keybinds[kc] = binds
            }
        }
    }

    fun reset() {
        scrollSpeed = 20f
        hitPositionOffset = 130f
        noteOffset = 0f
        noteScale = 0.8f
        laneWidthAdjustment = 0f
        laneSpacing = 0f
        skinStyle = GameConstants.SkinStyle.CIRCLE
        hue = 212f
        backgroundDim = 0.75f
        backgroundVideo = true
        stageOpacity = 0.5f
        receptorOpacity = 1f
        receptorLighting = true
        performanceMode = false
        upscroll = false
        showScore = true
        showCombo = true
        showAccuracy = true
        showJudgement = true
        show300g = true
        showHealthBar = true
        showErrorBar = true
        errorBarScale = 1f
        showFps = false
        kpsCounter = "left"
        judgementCounter = "left"
        progressDisplay = "bar"
        musicVolume = 1f
        sfxVolume = 0.4f
        audioOffset = 0f
        ignoreBeatmapHitsounds = false
        darkerHoldNotes = true
        stageHudYPosition = 0.5f
        keyCount = 4
        stagePosition = 0.5f
        stageWidth = 0.5f
        stageXOffset = 0f
        initDefaultKeybinds()
    }

    fun getSerialized(): String {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"scrollSpeed\": $scrollSpeed,")
        sb.appendLine("  \"hitPositionOffset\": $hitPositionOffset,")
        sb.appendLine("  \"noteOffset\": $noteOffset,")
        sb.appendLine("  \"noteScale\": $noteScale,")
        sb.appendLine("  \"laneWidthAdjustment\": $laneWidthAdjustment,")
        sb.appendLine("  \"laneSpacing\": $laneSpacing,")
        sb.appendLine("  \"skinStyle\": \"${skinStyle.name}\",")
        sb.appendLine("  \"hue\": $hue,")
        sb.appendLine("  \"backgroundDim\": $backgroundDim,")
        sb.appendLine("  \"backgroundVideo\": $backgroundVideo,")
        sb.appendLine("  \"stageOpacity\": $stageOpacity,")
        sb.appendLine("  \"receptorOpacity\": $receptorOpacity,")
        sb.appendLine("  \"receptorLighting\": $receptorLighting,")
        sb.appendLine("  \"performanceMode\": $performanceMode,")
        sb.appendLine("  \"upscroll\": $upscroll,")
        sb.appendLine("  \"showScore\": $showScore,")
        sb.appendLine("  \"showCombo\": $showCombo,")
        sb.appendLine("  \"showAccuracy\": $showAccuracy,")
        sb.appendLine("  \"showJudgement\": $showJudgement,")
        sb.appendLine("  \"show300g\": $show300g,")
        sb.appendLine("  \"showHealthBar\": $showHealthBar,")
        sb.appendLine("  \"showErrorBar\": $showErrorBar,")
        sb.appendLine("  \"errorBarScale\": $errorBarScale,")
        sb.appendLine("  \"showFps\": $showFps,")
        sb.appendLine("  \"kpsCounter\": \"$kpsCounter\",")
        sb.appendLine("  \"judgementCounter\": \"$judgementCounter\",")
        sb.appendLine("  \"progressDisplay\": \"$progressDisplay\",")
        sb.appendLine("  \"musicVolume\": $musicVolume,")
        sb.appendLine("  \"sfxVolume\": $sfxVolume,")
        sb.appendLine("  \"audioOffset\": $audioOffset,")
        sb.appendLine("  \"ignoreBeatmapHitsounds\": $ignoreBeatmapHitsounds,")
        sb.appendLine("  \"darkerHoldNotes\": $darkerHoldNotes,")
        sb.appendLine("  \"stageHudYPosition\": $stageHudYPosition,")
        sb.appendLine("  \"keyCount\": $keyCount,")
        sb.appendLine("  \"stagePosition\": $stagePosition,")
        sb.appendLine("  \"stageWidth\": $stageWidth,")
        sb.appendLine("  \"stageXOffset\": $stageXOffset")
        sb.append("}")
        return sb.toString()
    }

    fun loadFromJson(json: String) {
        try {
            val cleaned = json.trim()
                .removePrefix("{")
                .removeSuffix("}")
                .trim()

            val entries = parseJsonEntries(cleaned)

            scrollSpeed = entries["scrollSpeed"]?.toFloatOrNull() ?: scrollSpeed
            hitPositionOffset = entries["hitPositionOffset"]?.toFloatOrNull() ?: hitPositionOffset
            noteOffset = entries["noteOffset"]?.toFloatOrNull() ?: noteOffset
            noteScale = entries["noteScale"]?.toFloatOrNull() ?: noteScale
            laneWidthAdjustment = entries["laneWidthAdjustment"]?.toFloatOrNull() ?: laneWidthAdjustment
            laneSpacing = entries["laneSpacing"]?.toFloatOrNull() ?: laneSpacing
            hue = entries["hue"]?.toFloatOrNull() ?: hue
            backgroundDim = entries["backgroundDim"]?.toFloatOrNull() ?: backgroundDim
            stageOpacity = entries["stageOpacity"]?.toFloatOrNull() ?: stageOpacity
            receptorOpacity = entries["receptorOpacity"]?.toFloatOrNull() ?: receptorOpacity
            errorBarScale = entries["errorBarScale"]?.toFloatOrNull() ?: errorBarScale
            musicVolume = entries["musicVolume"]?.toFloatOrNull() ?: musicVolume
            sfxVolume = entries["sfxVolume"]?.toFloatOrNull() ?: sfxVolume
            audioOffset = entries["audioOffset"]?.toFloatOrNull() ?: audioOffset
            stageHudYPosition = entries["stageHudYPosition"]?.toFloatOrNull() ?: stageHudYPosition
            stagePosition = entries["stagePosition"]?.toFloatOrNull() ?: stagePosition
            stageWidth = entries["stageWidth"]?.toFloatOrNull() ?: stageWidth
            stageXOffset = entries["stageXOffset"]?.toFloatOrNull() ?: stageXOffset

            entries["skinStyle"]?.let {
                try {
                    skinStyle = GameConstants.SkinStyle.valueOf(it.removeSurrounding("\""))
                } catch (_: Exception) {
                }
            }
            entries["backgroundVideo"]?.let { backgroundVideo = it.toBooleanStrictOrNull() ?: backgroundVideo }
            entries["receptorLighting"]?.let { receptorLighting = it.toBooleanStrictOrNull() ?: receptorLighting }
            entries["performanceMode"]?.let { performanceMode = it.toBooleanStrictOrNull() ?: performanceMode }
            entries["upscroll"]?.let { upscroll = it.toBooleanStrictOrNull() ?: upscroll }
            entries["showScore"]?.let { showScore = it.toBooleanStrictOrNull() ?: showScore }
            entries["showCombo"]?.let { showCombo = it.toBooleanStrictOrNull() ?: showCombo }
            entries["showAccuracy"]?.let { showAccuracy = it.toBooleanStrictOrNull() ?: showAccuracy }
            entries["showJudgement"]?.let { showJudgement = it.toBooleanStrictOrNull() ?: showJudgement }
            entries["show300g"]?.let { show300g = it.toBooleanStrictOrNull() ?: show300g }
            entries["showHealthBar"]?.let { showHealthBar = it.toBooleanStrictOrNull() ?: showHealthBar }
            entries["showErrorBar"]?.let { showErrorBar = it.toBooleanStrictOrNull() ?: showErrorBar }
            entries["showFps"]?.let { showFps = it.toBooleanStrictOrNull() ?: showFps }
            entries["ignoreBeatmapHitsounds"]?.let { ignoreBeatmapHitsounds = it.toBooleanStrictOrNull() ?: ignoreBeatmapHitsounds }
            entries["darkerHoldNotes"]?.let { darkerHoldNotes = it.toBooleanStrictOrNull() ?: darkerHoldNotes }
            entries["kpsCounter"]?.let { kpsCounter = it.removeSurrounding("\"") }
            entries["judgementCounter"]?.let { judgementCounter = it.removeSurrounding("\"") }
            entries["progressDisplay"]?.let { progressDisplay = it.removeSurrounding("\"") }
            entries["keyCount"]?.let { keyCount = it.toIntOrNull() ?: keyCount }
        } catch (_: Exception) {
        }
    }

    private fun parseJsonEntries(json: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val lines = json.split("\n")
        for (line in lines) {
            val trimmed = line.trim().removeSuffix(",").trim()
            if (trimmed.isEmpty()) continue
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx < 0) continue
            val key = trimmed.substring(0, colonIdx).trim().removeSurrounding("\"")
            val value = trimmed.substring(colonIdx + 1).trim()
            result[key] = value
        }
        return result
    }
}
