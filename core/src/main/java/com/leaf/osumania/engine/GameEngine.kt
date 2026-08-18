package com.leaf.osumania.engine

import com.leaf.osumania.beatmap.BeatmapData
import com.leaf.osumania.beatmap.HoldData
import com.leaf.osumania.beatmap.TapData
import com.leaf.osumania.beatmap.TimingPoint
import com.leaf.osumania.mods.ModManager
import com.leaf.osumania.mods.Mods
import com.leaf.osumania.storage.SettingsStore
import com.leaf.osumania.systems.AudioSystem
import com.leaf.osumania.systems.HealthSystem
import com.leaf.osumania.systems.InputSystem
import com.leaf.osumania.systems.ReplayPlayer
import com.leaf.osumania.systems.ReplayRecorder
import com.leaf.osumania.systems.ScoreSystem

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
    var progress: Float = 0f
        private set
    var countdownActive: Boolean = false
    var countdownValue: Float = 0f

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

    var columnIndices = IntArray(0)
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

    private var songStartTime: Float = 0f
    private var pauseOffset: Float = 0f
    private var unpauseTimeRemaining: Float = 0f

    val judgementCounts: Map<Int, Int>
        get() = scoreSystem.let {
            mapOf(
                320 to it.getJudgementCount(320),
                300 to it.getJudgementCount(300),
                200 to it.getJudgementCount(200),
                100 to it.getJudgementCount(100),
                50 to it.getJudgementCount(50),
                0 to it.getJudgementCount(0)
            )
        }

    fun init(beatmap: BeatmapData, settings: SettingsStore) {
        beatmapData = beatmap
        keyCount = beatmap.difficulty.keyCount
        scrollSpeed = settings.scrollSpeed
        hitPositionOffset = settings.hitPositionOffset
        backgroundDim = settings.backgroundDim
        stageOpacity = settings.stageOpacity
        performanceMode = settings.performanceMode
        upscroll = settings.upscroll
        constantSpeed = settings.constantSpeed
        playbackRate = settings.scrollSpeed.let { 1f }

        val sw = com.badlogic.gdx.Gdx.graphics.width.toFloat()
        val sh = com.badlogic.gdx.Gdx.graphics.height.toFloat()

        playfield = Playfield(
            screenWidth = sw,
            screenHeight = sh,
            keyCount = keyCount,
            hitPositionOffset = hitPositionOffset,
            stagePosition = settings.stagePosition,
            noteOffset = settings.noteOffset,
            noteScale = settings.noteScale,
            laneWidthAdjustment = settings.laneWidthAdjustment,
            laneSpacing = settings.laneSpacing,
            upscroll = upscroll,
            skinStyle = settings.skinStyle
        )
        playfield.stageXOffset = settings.stageXOffset
        playfield.stageScale = settings.stageWidth
        playfield.recalculate()

        scoreSystem = ScoreSystem()
        healthSystem = HealthSystem()
        inputSystem = InputSystem(keyCount)
        audioSystem = AudioSystem()
        modManager = ModManager()

        columnIndices = IntArray(keyCount) { 0 }
        columns.clear()
        for (i in 0 until keyCount) {
            columns.add(mutableListOf())
        }

        hitObjects.clear()
        holdObjects.clear()

        for (ho in beatmap.hitObjects) {
            val column: Int
            val time: Float
            val endTime: Float
            val isHold: Boolean

            when (ho) {
                is TapData -> {
                    column = ho.column.coerceIn(0, keyCount - 1)
                    time = ho.time
                    endTime = ho.endTime
                    isHold = ho.isHoldHead
                }
                is HoldData -> {
                    column = ho.column.coerceIn(0, keyCount - 1)
                    time = ho.time
                    endTime = ho.endTime
                    isHold = true
                }
                else -> continue
            }

            if (isHold) {
                val holdData = HoldData(
                    column = column,
                    time = time,
                    endTime = endTime
                )
                holdObjects.add(holdData)
                columns[column].add(holdData)
            } else {
                val tapData = TapData(
                    column = column,
                    time = time
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
        songStartTime = beatmapData.delay
        pauseOffset = 0f
        progress = 0f

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

        if (beatmapData.endTime > beatmapData.startTime) {
            progress = ((timeElapsed - beatmapData.startTime) / (beatmapData.endTime - beatmapData.startTime)).coerceIn(0f, 1f)
        }

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
                    scoreSystem.miss()
                } else {
                    note.hit = true
                    combo++
                    if (combo > maxCombo) maxCombo = combo
                    scoreSystem.hit(judgement, combo)
                    healthSystem.hit(judgement)
                }
            }
            is HoldData -> {
                delta = timeElapsed - note.time
                judgement = determineJudgement(delta)
                if (judgement == 0) {
                    note.missed = true
                    combo = 0
                    healthSystem.miss()
                    audioSystem.playMissSound()
                    scoreSystem.miss()
                } else {
                    note.hit = true
                    combo++
                    if (combo > maxCombo) maxCombo = combo
                    scoreSystem.hit(judgement, combo)
                    healthSystem.hit(judgement)
                }
            }
        }

        score = scoreSystem.score.toInt()
        accuracy = scoreSystem.accuracy * 100f
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

    fun getObjectOffset(timeElapsed: Float, noteTime: Float): Float {
        return (timeElapsed - noteTime) * scrollSpeed / 1000f * playfield.screenHeight
    }

    fun getMissWindow(): Float = GameConstants.MISS_WINDOW

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
                        if (kotlin.math.abs(delta) <= 157f) {
                            columnIndices[column] = i
                            return note
                        }
                    }
                }
                is HoldData -> {
                    if (!note.hit && !note.missed) {
                        val delta = timeElapsed - note.time
                        if (kotlin.math.abs(delta) <= 157f) {
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
                val isMissed = when (note) {
                    is TapData -> !note.hit && !note.missed && timeElapsed - note.time > 157f
                    is HoldData -> !note.hit && !note.missed && timeElapsed - note.time > 157f
                    else -> false
                }
                if (isMissed) {
                    when (note) {
                        is TapData -> note.missed = true
                        is HoldData -> note.missed = true
                    }
                    columnIndices[c] = i + 1
                    combo = 0
                    healthSystem.miss()
                    scoreSystem.miss()
                    accuracy = scoreSystem.accuracy * 100f
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
            return TimingPoint(0f, 0f, scrollSpeed = scrollSpeed)
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
