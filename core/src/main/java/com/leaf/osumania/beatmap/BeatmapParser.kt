package com.leaf.osumania.beatmap

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.abs

fun parseHitWindows(od: Float): HitWindows {
    val w320 = if (od <= 5f) 22.4f - 0.6f * od else 24.9f - 1.1f * od
    val w300 = 64f - 3f * od
    val w200 = 97f - 3f * od
    val w100 = 127f - 3f * od
    val w50 = 151f - 3f * od
    val w0 = 188f - 3f * od
    return HitWindows(w320, w300, w200, w100, w50, w0)
}

fun getSectionLines(lines: List<String>, sectionName: String): List<String> {
    val sectionTag = "[$sectionName]"
    val result = mutableListOf<String>()
    var found = false
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed == sectionTag) {
            found = true
            continue
        }
        if (found && trimmed.startsWith("[") && trimmed.endsWith("]")) {
            break
        }
        if (found && trimmed.isNotEmpty()) {
            result.add(trimmed)
        }
    }
    return result
}

fun getLineValue(lines: List<String>, key: String, default: String = ""): String {
    for (line in lines) {
        val trimmed = line.trim()
        val idx = trimmed.indexOf(':')
        if (idx > 0) {
            val k = trimmed.substring(0, idx).trim()
            val v = trimmed.substring(idx + 1).trim()
            if (k.equals(key, ignoreCase = true)) {
                return v
            }
        }
    }
    return default
}

fun parseOsuFile(
    lines: List<String>,
    columnCount: Int = 0,
    holdOff: Boolean = false,
    audioOffset: Float = 0f,
    columnMap: IntArray? = null
): BeatmapData {
    val generalLines = getSectionLines(lines, "General")
    val metadataLines = getSectionLines(lines, "Metadata")
    val difficultyLines = getSectionLines(lines, "Difficulty")
    val hitObjectLines = getSectionLines(lines, "HitObjects")
    val timingPointLines = getSectionLines(lines, "TimingPoints")
    val eventLines = getSectionLines(lines, "Events")

    val audioFilename = getLineValue(generalLines, "AudioFilename")
    val previewTime = getLineValue(generalLines, "PreviewTime", "0").toFloatOrNull() ?: 0f
    val mode = getLineValue(generalLines, "Mode", "3").toIntOrNull() ?: 3
    if (mode != 3) {
        return BeatmapData()
    }

    val title = getLineValue(metadataLines, "Title")
    val titleUnicode = getLineValue(metadataLines, "TitleUnicode")
    val artist = getLineValue(metadataLines, "Artist")
    val artistUnicode = getLineValue(metadataLines, "ArtistUnicode")
    val version = getLineValue(metadataLines, "Version")
    val creator = getLineValue(metadataLines, "Creator")
    val beatmapId = getLineValue(metadataLines, "BeatmapID", "0").toIntOrNull() ?: 0
    val beatmapSetId = getLineValue(metadataLines, "BeatmapSetID", "0").toIntOrNull() ?: 0

    val cs = getLineValue(difficultyLines, "CircleSize", "4").toFloatOrNull() ?: 4f
    val od = getLineValue(difficultyLines, "OverallDifficulty", "5").toFloatOrNull() ?: 5f
    val hp = getLineValue(difficultyLines, "HPDrainRate", "5").toFloatOrNull() ?: 5f
    val ks = if (columnCount > 0) columnCount else cs.toInt()

    var backgroundUrl: String? = null
    var videoUrl: String? = null
    for (line in eventLines) {
        val parts = line.split(",")
        if (parts.size >= 3) {
            val type = parts[0].trim().toIntOrNull() ?: continue
            when (type) {
                0 -> {
                    val filename = parts[2].trim().removeSurrounding("\"", "\"")
                    backgroundUrl = filename
                }
                1 -> {
                    val filename = parts[2].trim().removeSurrounding("\"", "\"")
                    videoUrl = filename
                }
            }
        }
    }

    val beatLengths = mutableListOf<Float>()
    val timingPoints = mutableListOf<TimingPoint>()
    var lastUninheritedScrollSpeed = 1f
    var lastUninheritedBeatLength = 1000f

    for (line in timingPointLines) {
        val parts = line.split(",")
        if (parts.size < 8) continue
        val time = parts[0].trim().toFloatOrNull() ?: continue
        val beatLength = parts[1].trim().toFloatOrNull() ?: continue
        val meter = parts[2].trim().toIntOrNull() ?: 4
        val sampleSet = parts[3].trim().toIntOrNull() ?: 1
        val sampleIndex = parts[4].trim().toIntOrNull() ?: 0
        val volume = parts[5].trim().toFloatOrNull() ?: 0.5f
        val uninherited = parts[6].trim().toIntOrNull()?.let { it == 1 } ?: true
        val effects = parts[7].trim().toIntOrNull() ?: 0

        val sampleSetName = when (sampleSet) {
            1 -> "normal"
            2 -> "soft"
            3 -> "drum"
            else -> "normal"
        }

        var scrollSpeed = 1f
        if (uninherited) {
            beatLengths.add(beatLength)
            lastUninheritedBeatLength = beatLength
            scrollSpeed = if (beatLength > 0) 1000f / beatLength else 1f
            lastUninheritedScrollSpeed = scrollSpeed
        } else {
            scrollSpeed = lastUninheritedScrollSpeed * (100f / -beatLength)
        }

        timingPoints.add(
            TimingPoint(
                time = time,
                beatLength = beatLength,
                meter = meter,
                sampleSet = sampleSetName,
                sampleIndex = sampleIndex,
                volume = volume,
                uninherited = uninherited,
                effects = effects,
                scrollSpeed = scrollSpeed
            )
        )
    }

    val taps = mutableListOf<TapData>()
    val holds = mutableListOf<HoldData>()
    var firstNoteTime = Float.MAX_VALUE

    for (line in hitObjectLines) {
        val parts = line.split(",")
        if (parts.size < 6) continue
        val x = parts[0].trim().toFloatOrNull() ?: continue
        val time = parts[1].trim().toFloatOrNull() ?: continue
        val type = parts[3].trim().toIntOrNull() ?: continue
        val hitSoundVal = parts[4].trim().toIntOrNull() ?: 0
        val hitSampleStr = parts[5].trim()

        if (time < firstNoteTime) {
            firstNoteTime = time
        }

        val normal = (hitSoundVal and 1) != 0
        val whistle = (hitSoundVal and 2) != 0
        val finish = (hitSoundVal and 4) != 0
        val clap = (hitSoundVal and 8) != 0
        val hitSound = HitSound(normal, whistle, finish, clap)

        val sampleParts = hitSampleStr.split(":")
        val normalSet = sampleParts.getOrNull(0)?.toIntOrNull() ?: 0
        val additionSet = sampleParts.getOrNull(1)?.toIntOrNull() ?: 0
        val index = sampleParts.getOrNull(2)?.toIntOrNull() ?: 0
        val volume = (sampleParts.getOrNull(3)?.toFloatOrNull() ?: 0.5f) / 100f
        val filename = sampleParts.getOrNull(4) ?: ""
        val hitSample = HitSample(normalSet, additionSet, index, volume, filename)

        var column = floor((x * ks).toDouble() / 512.0).toInt()
        column = column.coerceIn(0, ks - 1)
        if (columnMap != null && column < columnMap.size) {
            column = columnMap[column]
        }

        val isHold = (type and 128) != 0
        var endTime = time
        if (isHold) {
            val endStr = sampleParts.getOrNull(0)
            endTime = endStr?.toFloatOrNull()?.takeIf { it > 0 } ?: time
        }

        taps.add(
            TapData(
                column = column,
                time = time,
                endTime = endTime,
                hitSound = hitSound,
                hitSample = hitSample,
                isHoldHead = isHold
            )
        )

        if (isHold) {
            holds.add(HoldData(column, time, endTime))
        }
    }

    val delay = if (firstNoteTime < Float.MAX_VALUE) max(1000f - firstNoteTime, 0f) else 0f
    val offsetDelay = delay - audioOffset

    val adjustedTaps = taps.map { tap ->
        tap.copy(time = tap.time + offsetDelay, endTime = tap.endTime + offsetDelay)
    }
    val adjustedHolds = holds.map { hold ->
        hold.copy(time = hold.time + offsetDelay, endTime = hold.endTime + offsetDelay)
    }

    val adjustedTimingPoints = timingPoints.map { tp ->
        tp.copy(time = tp.time + offsetDelay)
    }

    val breaks = mutableListOf<Break>()
    val allObjects = mutableListOf<Any>()
    allObjects.addAll(adjustedTaps)
    if (!holdOff) {
        allObjects.addAll(adjustedHolds)
    }

    if (allObjects.isNotEmpty()) {
        val sorted = allObjects.sortedBy { obj ->
            when (obj) {
                is TapData -> obj.time
                is HoldData -> obj.time
                else -> 0f
            }
        }
        val startTime = sorted.first().let {
            when (it) {
                is TapData -> it.time
                is HoldData -> it.time
                else -> 0f
            }
        }
        val endTime = sorted.last().let {
            when (it) {
                is TapData -> max(it.time, it.endTime)
                is HoldData -> it.endTime
                else -> 0f
            }
        }
        val hitWindows = parseHitWindows(od)

        return BeatmapData(
            beatmapId = beatmapId,
            beatmapSetId = beatmapSetId,
            version = version,
            timingPoints = adjustedTimingPoints,
            hitObjects = sorted,
            breaks = breaks,
            startTime = startTime,
            endTime = endTime,
            hitWindows = hitWindows,
            delay = delay,
            metadata = Metadata(title, titleUnicode, artist, artistUnicode, version, creator),
            difficulty = Difficulty(ks, od, hp),
            audioOffset = audioOffset,
            backgroundUrl = backgroundUrl,
            videoUrl = videoUrl
        )
    }

    val hitWindows = parseHitWindows(od)
    return BeatmapData(
        beatmapId = beatmapId,
        beatmapSetId = beatmapSetId,
        version = version,
        timingPoints = adjustedTimingPoints,
        hitObjects = allObjects,
        breaks = breaks,
        startTime = 0f,
        endTime = 0f,
        hitWindows = hitWindows,
        delay = delay,
        metadata = Metadata(title, titleUnicode, artist, artistUnicode, version, creator),
        difficulty = Difficulty(ks, od, hp),
        audioOffset = audioOffset,
        backgroundUrl = backgroundUrl,
        videoUrl = videoUrl
    )
}


