package com.leaf.osumania.api

import com.badlogic.gdx.Net
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import java.io.StringReader

data class ApiBeatmapSet(
    val id: Int = 0,
    val artist: String = "",
    val artistUnicode: String = "",
    val creator: String = "",
    val title: String = "",
    val titleUnicode: String = "",
    val nsfw: Boolean = false,
    val playCount: Int = 0,
    val favouriteCount: Int = 0,
    val rating: Float = 0f,
    val status: String = "ranked",
    val genreId: Int = 0,
    val languageId: Int = 0,
    val beatmaps: MutableList<ApiBeatmap> = mutableListOf()
)

data class ApiBeatmap(
    val id: Int = 0,
    val beatmapsetId: Int = 0,
    val version: String = "",
    val difficultyRating: Float = 0f,
    val cs: Float = 4f,
    val od: Float = 5f,
    val drain: Float = 5f,
    val totalLength: Float = 0f,
    val bpm: Float = 0f,
    val countCircles: Int = 0,
    val countSliders: Int = 0
)

class OsuApiClient {
    private var accessToken: String = ""
    private var tokenExpiry: Long = 0L
    private val baseUrl = "https://osu.ppy.sh/api/v2"
    private val tokenUrl = "https://osu.ppy.sh/oauth/token"

    var clientId: String = ""
    var clientSecret: String = ""
    var apiProxyUrl: String? = null

    fun authenticate(callback: (Boolean) -> Unit) {
        if (System.currentTimeMillis() < tokenExpiry - 60000) {
            callback(true)
            return
        }
        val net = com.badlogic.gdx.Gdx.net
        val request = Net.HttpRequest().apply {
            method = Net.HttpMethods.POST
            url = tokenUrl
            setContent("client_id=$clientId&client_secret=$clientSecret&grant_type=client_credentials&scope=public")
            header("Content-Type", "application/x-www-form-urlencoded")
        }
        net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(response: Net.HttpResponse) {
                val json = JsonReader().read(StringReader(response.resultAsString))
                accessToken = json.getString("access_token")
                tokenExpiry = System.currentTimeMillis() + json.getLong("expires_in") * 1000
                callback(true)
            }
            override fun failed(t: Throwable) { callback(false) }
            override fun cancelled() { callback(false) }
        })
    }

    fun searchBeatmaps(query: String, cursor: String? = null, callback: (List<ApiBeatmapSet>, String?) -> Unit) {
        val url = "${apiProxyUrl ?: baseUrl}/beatmapsets/search?q=$query&m=3${if (cursor != null) "&cursor_string=$cursor" else ""}"
        makeApiRequest(url) { json ->
            val sets = mutableListOf<ApiBeatmapSet>()
            json.get("beatmapsets")?.forEach { setJson ->
                val set = ApiBeatmapSet(
                    id = setJson.getInt("id"),
                    artist = setJson.getString("artist"),
                    artistUnicode = setJson.getString("artist_unicode"),
                    creator = setJson.getString("creator"),
                    title = setJson.getString("title"),
                    titleUnicode = setJson.getString("title_unicode"),
                    nsfw = setJson.getBoolean("nsfw"),
                    status = setJson.getString("status"),
                    genreId = setJson.getInt("genre_id"),
                    languageId = setJson.getInt("language_id")
                )
                setJson.get("beatmaps")?.forEach { bmJson ->
                    set.beatmaps.add(ApiBeatmap(
                        id = bmJson.getInt("id"),
                        beatmapsetId = bmJson.getInt("beatmapset_id"),
                        version = bmJson.getString("version"),
                        difficultyRating = bmJson.getFloat("difficulty_rating"),
                        cs = bmJson.getFloat("cs"),
                        od = bmJson.getFloat("accuracy"),
                        drain = bmJson.getFloat("drain"),
                        totalLength = bmJson.getFloat("total_length"),
                        bpm = bmJson.getFloat("bpm"),
                        countCircles = bmJson.getInt("count_circles"),
                        countSliders = bmJson.getInt("count_sliders")
                    ))
                }
                sets.add(set)
            }
            val nextCursor = json.getString("cursor_string", null)
            callback(sets, nextCursor)
        }
    }

    fun downloadBeatmap(setId: Int, callback: (ByteArray?) -> Unit) {
        val providers = listOf(
            "https://catboy.best/d/$setId",
            "https://api.nerinyan.moe/d/$setId",
            "https://dl.sayobot.cn/beatmaps/download/$setId"
        )
        tryDownload(providers, 0, callback)
    }

    private fun tryDownload(providers: List<String>, index: Int, callback: (ByteArray?) -> Unit) {
        if (index >= providers.size) { callback(null); return }
        val net = com.badlogic.gdx.Gdx.net
        val request = Net.HttpRequest().apply {
            method = Net.HttpMethods.GET
            url = providers[index]
        }
        net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(response: Net.HttpResponse) {
                val bytes = response.result?.readBytes() ?: ByteArray(0)
                if (bytes.size > 1000) callback(bytes) else tryDownload(providers, index + 1, callback)
            }
            override fun failed(t: Throwable) { tryDownload(providers, index + 1, callback) }
            override fun cancelled() { callback(null) }
        })
    }

    private fun makeApiRequest(url: String, callback: (JsonValue) -> Unit) {
        val net = com.badlogic.gdx.Gdx.net
        val request = Net.HttpRequest().apply {
            method = Net.HttpMethods.GET
            this.url = url
            header("Authorization", "Bearer $accessToken")
            header("Accept", "application/json")
        }
        net.sendHttpRequest(request, object : Net.HttpResponseListener {
            override fun handleHttpResponse(response: Net.HttpResponse) {
                val json = JsonReader().read(StringReader(response.resultAsString))
                callback(json)
            }
            override fun failed(t: Throwable) {}
            override fun cancelled() {}
        })
    }

    fun getDownloadUrl(setId: Int): String = "https://catboy.best/d/$setId"
    fun getPreviewUrl(setId: Int): String = "https://b.ppy.sh/preview/$setId.mp3"
    fun getCoverUrl(setId: Int): String = "https://assets.ppy.sh/beatmaps/$setId/covers/list.jpg"
}
