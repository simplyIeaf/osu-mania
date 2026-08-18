package com.leaf.osumania.skin

import com.leaf.osumania.engine.GameConstants

class SkinManager {
    var currentStyle: GameConstants.SkinStyle = GameConstants.SkinStyle.CIRCLE
    var hue: Float = 212f
    var darkerHoldNotes: Boolean = true
    var judgementSet: String = "osuStable"

    fun getJudgementTextureName(judgement: Int): String {
        val name = when (judgement) {
            320 -> "mania-hit300g"
            300 -> "mania-hit300"
            200 -> "mania-hit200"
            100 -> "mania-hit100"
            50 -> "mania-hit50"
            else -> "mania-hit0"
        }
        return "skin/judgements-$judgementSet/$name.png"
    }
}
