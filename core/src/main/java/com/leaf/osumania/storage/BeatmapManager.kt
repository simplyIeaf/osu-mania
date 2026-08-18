package com.leaf.osumania.storage

import com.leaf.osumania.api.ApiBeatmapSet
import com.leaf.osumania.api.OsuApiClient
import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.beatmap.BeatmapParser
import com.leaf.osumania.beatmap.OszLoader

class BeatmapManager(
    private val apiClient: OsuApiClient,
    private val dbHelper: DatabaseHelper
) {
    private val localBeatmaps = mutableMapOf<String, BeatmapData>()
    private val downloadedSets = mutableMapOf<Int, ByteArray>()

    fun loadFromOsz(bytes: ByteArray): BeatmapData? {
        return try {
            val oszLoader = OszLoader()
            val oszData = oszLoader.load(bytes) ?: return null
            val parser = BeatmapParser()
            parser.parse(oszData)
        } catch (e: Exception) {
            null
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
        val beatmap = loadFromOsz(bytes) ?: return null
        localBeatmaps[beatmap.md5Hash] = beatmap
        return beatmap
    }
}
