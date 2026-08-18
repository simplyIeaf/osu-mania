package com.leaf.osumania.beatmap

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

data class OszResult(
    val osuFiles: List<Pair<String, ByteArray>>,
    val audioFiles: Map<String, ByteArray>,
    val imageFiles: Map<String, ByteArray>,
    val otherFiles: Map<String, ByteArray>
)

fun loadOsz(bytes: ByteArray): OszResult {
    val osuFiles = mutableListOf<Pair<String, ByteArray>>()
    val audioFiles = mutableMapOf<String, ByteArray>()
    val imageFiles = mutableMapOf<String, ByteArray>()
    val otherFiles = mutableMapOf<String, ByteArray>()

    val audioExtensions = setOf("mp3", "ogg", "wav")
    val imageExtensions = setOf("jpg", "jpeg", "png")

    val zis = ZipInputStream(ByteArrayInputStream(bytes))
    var entry = zis.nextEntry

    while (entry != null) {
        if (!entry.isDirectory) {
            val name = entry.name
            val lowerName = name.lowercase()
            val extension = lowerName.substringAfterLast('.', "")
            val data = zis.readBytes()

            when {
                lowerName.endsWith(".osu") -> {
                    osuFiles.add(name to data)
                }
                extension in audioExtensions -> {
                    audioFiles[name] = data
                }
                extension in imageExtensions -> {
                    imageFiles[name] = data
                }
                else -> {
                    otherFiles[name] = data
                }
            }
        }
        entry = zis.nextEntry
    }

    zis.close()

    return OszResult(osuFiles, audioFiles, imageFiles, otherFiles)
}
