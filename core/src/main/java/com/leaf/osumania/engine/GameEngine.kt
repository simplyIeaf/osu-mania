package com.leaf.osumania.engine

import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.beatmap.TimingPoint
import com.leaf.osumania.scoring.ScoreSystem
import com.leaf.osumania.health.HealthSystem
import com.leaf.osumania.input.InputSystem
import com.leaf.osumania.audio.AudioSystem
import com.leaf.osumania.mods.ModManager
import com.leaf.osumania.replay.ReplayRecorder
import com.leaf.osumania.replay.ReplayPlayer
import com.leaf.osumania.settings.GameSettings
import com.leaf.osumania.objects.TapData
import com.leaf.osumania.objects.HoldData

class GameEngine {
    var state: GameState = GameState.LOADING
        private set
    var timeElapsed: Float = 0f
        private set
    var combo: Int = 0
        private set
    var maxCombo: Int = 0
        private set
    var score: Int = 0
        private set
    var health: Float = GameConstants.MAX_HEALTH
        private set
    var accuracy: Float = 100f
        private set
    var currentTimingPointIndex: Int = 0
        private set

    lateinit var scoreSystem: ScoreSystem
        private set
    lateinit var healthSystem: HealthSystem
        private set
    lateinit var inputSystem: InputSystem
        private set
    lateinit var audioSystem: AudioSystem
        private set
    lateinit var playfield: Playfield
        private set
    lateinit var beatmapData: BeatmapData
        private set
    lateinit var modManager: ModManager
        private set
    var replayRecorder: ReplayRecorder? = null
        private set
    var replayPlayer: ReplayPlayer? = null
        private set

    var hitObjects: MutableList<TapData> = mutableListOf()
        private set
    var holdObjects: MutableList<HoldData> = mutableListOf()
        private set
    var columns: MutableList<MutableList<Any>> = mutableListOf()
        private set

    var scrollSpeed: Float = 1f
    var hitPositionOffset: Float = 50f
    var backgroundDim: Float = 0.8f
    var stageOpacity: Float = 0.6f
    var performanceMode: Boolean = false
    var keyCount: Int = 4
    var upscroll: Boolean = false
    var constantSpeed: Boolean = false
    var playbackRate: Float = 1f
    var allowNegativeOffset: Boolean = false

    private var columnIndices: IntArray = intArrayOf()
    private var songStartTime: Float = 0f
    private var pauseOffset: Float = 0f
    private var unpauseTimeRemaining: Float = 0f
    private var breakTime: Boolean = false
    private var failThreshold: Float = 0.5f

    fun init(beatmap: BeatmapData, settings: GameSettings) {
        beatmapData = beatmap
        keyCount = settings.keyCount
        scrollSpeed = settings.scrollSpeed
        hitPositionOffset = settings.hitPositionOffset
        backgroundDim = settings.backgroundDim
        stageOpacity = settings.stageOpacity
        performanceMode = settings.performanceMode
        upscroll = settings.upscroll
        constantSpeed = settings.constantSpeed
        playbackRate = settings.playbackRate

        playfield = Playfield(
            screenWidth = settings.screenWidth,
            screenHeight = settings.screenHeight,
            keyCount = keyCount,
            hitPositionOffset = hitPositionOffset,
            upscroll = upscroll,
            skinStyle = settings.skinStyle,
            laneWidthAdjustment = settings.laneWidthAdjustment,
            laneSpacing = settings.laneSpacing
        )
        playfield.recalculate()

        scoreSystem = ScoreSystem()
        healthSystem = HealthSystem()
        inputSystem = InputSystem(keyCount)
        audioSystem = AudioSystem()
        modManager = ModManager(settings.enabledMods)

        columnIndices = IntArray(keyCount) { 0 }
        columns.clear()
        for (i in 0 until keyCount) {
            columns.add(mutableListOf())
        }

        hitObjects.clear()
        holdObjects.clear()

        for (ho in beatmap.hitObjects) {
            val column = ho.column.coerceIn(0, keyCount - 1)
            if (ho.isHold) {
                val holdData = HoldData(
                    column = column,
                    time = ho.time,
                    endTime = ho.endTime,
                    hit = false,
                    released = false,
                    missed = false
                )
                holdObjects.add(holdData)
                columns[column].add(holdData)
            } else {
                val tapData = TapData(
                    column = column,
                    time = ho.time,
                    hit = false,
                    missed = false
                )
                hitObjects.add(tapData)
                columns[column].add(tapData)
            }
        }

        scoreSystem.reset()
        healthSystem.reset()
        combo = 0
        maxCombo = 0
        score = 0
        health = GameConstants.MAX_HEALTH
        accuracy = 100f
        currentTimingPointIndex = 0
        timeElapsed = 0f
        songStartTime = beatmapData.audioLeadIn
        pauseOffset = 0f
        breakTime = false

        state = GameState.WAIT
    }

    fun update(deltaTime: Float) {
        when (state) {
            GameState.PLAY -> updatePlay(deltaTime)
            GameState.UNPAUSE -> updateUnpause(deltaTime)
            GameState.WAIT -> updateWait(deltaTime)
            else -> {}
        }
    }

    private fun updateWait(deltaTime: Float) {
        if (audioSystem.isPlaying) {
            state = GameState.PLAY
        }
    }

    private fun updatePlay(deltaTime: Float) {
        if (audioSystem.isPlaying) {
            timeElapsed = (audioSystem.getCurrentPosition() - songStartTime) * playbackRate - pauseOffset
        } else {
            timeElapsed += deltaTime * 1000f * playbackRate
        }

        currentTimingPointIndex = getCurrentTimingPointIndex()
        updateHolds(deltaTime)
        checkLateMisses()
        checkFail()
        checkFinish()

        if (!performanceMode) {
            healthSystem.passiveDrain(deltaTime, currentTimingPoint())
        }
        health = healthSystem.health
    }

    private fun updateUnpause(deltaTime: Float) {
        unpauseTimeRemaining -= deltaTime * 1000f
        if (unpauseTimeRemaining <= 0f) {
            audioSystem.resume()
            state = GameState.PLAY
        }
    }

    private fun updateHolds(deltaTime: Float) {
        for (hold in holdObjects) {
            if (hold.hit && !hold.released && !hold.missed) {
                if (timeElapsed >= hold.endTime) {
                    hold.released = true
                    scoreSystem.holdComplete()
                }
            }
        }
    }

    fun hit(column: Int) {
        if (state != GameState.PLAY) return
        if (column < 0 || column >= keyCount) return

        inputSystem.hit(column)
        audioSystem.playHitsound(column)

        val note = findCurrentNote(column) ?: return
        noteHit(column, note)
    }

    fun release(column: Int) {
        if (column < 0 || column >= keyCount) return

        inputSystem.release(column)

        for (hold in holdObjects) {
            if (hold.column == column && hold.hit && !hold.released && !hold.missed) {
                if (timeElapsed < hold.endTime - 100f) {
                    hold.released = true
                    hold.missed = true
                    combo = 0
                    healthSystem.miss()
                    audioSystem.playMissSound()
                    replayRecorder?.recordRelease(column, timeElapsed, false)
                }
                break
            }
        }
    }

    private fun noteHit(column: Int, note: Any) {
        val delta: Float
        val judgement: Int

        when (note) {
            is TapData -> {
                delta = timeElapsed - note.time
                judgement = determineJudgement(delta)
                if (judgement == 0) {
                    note.missed = true
                    combo = 0
                    healthSystem.miss()
                    audioSystem.playMissSound()
                } else {
                    note.hit = true
                    combo++
                    if (combo > maxCombo) maxCombo = combo
                    scoreSystem.hit(judgement, combo)
                    healthSystem.hit(judgement)
                    audioSystem.playHitsound(judgement)
                }
                replayRecorder?.recordHit(column, timeElapsed, delta, judgement)
            }
            is HoldData -> {
                delta = timeElapsed - note.time
                judgement = determineJudgement(delta)
                if (judgement == 0) {
                    note.missed = true
                    combo = 0
                    healthSystem.miss()
                    audioSystem.playMissSound()
                } else {
                    note.hit = true
                    combo++
                    if (combo > maxCombo) maxCombo = combo
                    scoreSystem.hit(judgement, combo)
                    healthSystem.hit(judgement)
                    audioSystem.playHitsound(judgement)
                }
                replayRecorder?.recordHit(column, timeElapsed, delta, judgement)
            }
        }

        score = scoreSystem.score
        accuracy = scoreSystem.accuracy
        health = healthSystem.health
    }

    private fun determineJudgement(delta: Float): Int {
        val absDelta = kotlin.math.abs(delta)
        return when {
            absDelta <= 16f -> 320
            absDelta <= 64f -> 300
            absDelta <= 97f -> 200
            absDelta <= 127f -> 100
            absDelta <= 157f -> 50
            else -> 0
        }
    }

    private fun findCurrentNote(column: Int): Any? {
        if (column < 0 || column >= columns.size) return null
        val col = columns[column]
        val idx = columnIndices[column]
        for (i in idx until col.size) {
            val note = col[i]
            when (note) {
                is TapData -> {
                    if (!note.hit && !note.missed) {
                        val delta = timeElapsed - note.time
                        if (delta <= 157f) {
                            columnIndices[column] = i
                            return note
                        }
                    }
                }
                is HoldData -> {
                    if (!note.hit && !note.missed) {
                        val delta = timeElapsed - note.time
                        if (delta <= 157f) {
                            columnIndices[column] = i
                            return note
                        }
                    }
                }
            }
        }
        return null
    }

    private fun checkLateMisses() {
        for (c in 0 until keyCount) {
            val col = columns[c]
            val idx = columnIndices[c]
            for (i in idx until col.size) {
                val note = col[i]
                when (note) {
                    is TapData -> {
                        if (!note.hit && !note.missed) {
                            if (timeElapsed - note.time > 157f) {
                                note.missed = true
                                columnIndices[c] = i + 1
                                combo = 0
                                healthSystem.miss()
                                scoreSystem.miss()
                                accuracy = scoreSystem.accuracy
                                replayRecorder?.recordMiss(c, timeElapsed, note.time)
                            }
                        }
                    }
                    is HoldData -> {
                        if (!note.hit && !note.missed) {
                            if (timeElapsed - note.time > 157f) {
                                note.missed = true
                                columnIndices[c] = i + 1
                                combo = 0
                                healthSystem.miss()
                                scoreSystem.miss()
                                accuracy = scoreSystem.accuracy
                                replayRecorder?.recordMiss(c, timeElapsed, note.time)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkFail() {
        if (health <= GameConstants.MIN_HEALTH && !modManager.isNoFail) {
            state = GameState.FAIL
            audioSystem.pause()
        }
    }

    private fun checkFinish() {
        val allDone = columns.all { col ->
            col.all { note ->
                when (note) {
                    is TapData -> note.hit || note.missed
                    is HoldData -> (note.hit && note.released) || note.missed
                    else -> true
                }
            }
        }
        if (allDone && columns.isNotEmpty() && timeElapsed > 0f) {
            state = GameState.FINISH
            audioSystem.stop()
        }
    }

    fun seek(time: Float) {
        timeElapsed = time
        pauseOffset = 0f
        currentTimingPointIndex = 0

        for (i in columnIndices.indices) {
            columnIndices[i] = 0
        }

        for (ho in hitObjects) {
            ho.hit = false
            ho.missed = false
        }
        for (ho in holdObjects) {
            ho.hit = false
            ho.released = false
            ho.missed = false
        }

        scoreSystem.reset()
        healthSystem.reset()
        combo = 0
        maxCombo = 0
        score = 0
        health = GameConstants.MAX_HEALTH
        accuracy = 100f

        audioSystem.seekTo(songStartTime + time / playbackRate)
    }

    fun pause() {
        if (state == GameState.PLAY) {
            audioSystem.pause()
            state = GameState.PAUSE
        }
    }

    fun unpause() {
        if (state == GameState.PAUSE) {
            state = GameState.UNPAUSE
            unpauseTimeRemaining = GameConstants.UNPAUSE_DELAY
        }
    }

    fun retry() {
        seek(0f)
        state = GameState.WAIT
        audioSystem.stop()
    }

    fun quit() {
        audioSystem.stop()
        state = GameState.FINISH
    }

    private fun currentTimingPoint(): TimingPoint {
        if (beatmapData.timingPoints.isEmpty()) {
            return TimingPoint(0f, 0f, scrollSpeed)
        }
        return beatmapData.timingPoints[currentTimingPointIndex.coerceAtMost(beatmapData.timingPoints.size - 1)]
    }

    private fun getCurrentTimingPointIndex(): Int {
        val tps = beatmapData.timingPoints
        if (tps.isEmpty()) return 0
        var idx = currentTimingPointIndex
        while (idx < tps.size - 1 && tps[idx + 1].time <= timeElapsed) {
            idx++
        }
        while (idx > 0 && tps[idx].time > timeElapsed) {
            idx--
        }
        return idx
    }
}
