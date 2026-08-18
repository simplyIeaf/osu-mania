package com.leaf.osumania.beatmap

import com.badlogic.gdx.utils.JsonValue
import com.leaf.osumania.api.ApiBeatmapSet
import com.leaf.osumania.api.OsuApiClient
import com.leaf.osumania.storage.DatabaseHelper

class BeatmapManager(
    private val apiClient: OsuApiClient,
    private val dbHelper: DatabaseHelper
) {
    private val localBeatmaps = mutableMapOf<String, BeatmapData>()
    private val downloadedSets = mutableMapOf<Int, ByteArray>()

    fun loadFromOsz(bytes: ByteArray): Pair<BeatmapData?, ByteArray?> {
        return try {
            val result = loadOsz(bytes)
            if (result.osuFiles.isEmpty()) return Pair(null, null)
            val osuText = String(result.osuFiles.first().second)
            val lines = osuText.lines()
            val beatmap = parseOsuFile(lines)
            val audioBytes = result.audioFiles.values.firstOrNull()
            Pair(beatmap, audioBytes)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    fun searchOnline(query: String, callback: (List<ApiBeatmapSet>) -> Unit) {
        apiClient.searchBeatmaps(query) { sets, _ ->
            callback(sets)
        }
    }

    fun downloadBeatmapSet(setId: Int, callback: (ByteArray?) -> Unit) {
        val cached = dbHelper.getCachedBeatmapSet(setId.toString())
        if (cached != null) {
            downloadedSets[setId] = cached
            callback(cached)
            return
        }
        apiClient.downloadBeatmap(setId) { bytes ->
            if (bytes != null) {
                downloadedSets[setId] = bytes
                dbHelper.cacheBeatmapSet(setId.toString(), bytes)
            }
            callback(bytes)
        }
    }

    fun getLocalBeatmaps(): List<BeatmapData> {
        return localBeatmaps.values.toList()
    }

    fun getBeatmapByHash(hash: String): BeatmapData? {
        return localBeatmaps[hash]
    }

    fun importOszFile(bytes: ByteArray): BeatmapData? {
        val result = loadFromOsz(bytes).first ?: return null
        localBeatmaps[result.beatmapHash.ifEmpty { result.metadata.title }] = result
        return result
    }
}
