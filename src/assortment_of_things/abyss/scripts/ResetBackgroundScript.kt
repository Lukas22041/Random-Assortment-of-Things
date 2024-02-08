package assortment_of_things.abyss.scripts

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.combat.CombatEngine
import java.awt.Color

class ResetBackgroundScript : EveryFrameScript {

    companion object { var resetBackground = false }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
       /*if (resetBackground && (Global.getCombatEngine()?.isInCampaign == true || Global.getCurrentState() == GameState.TITLE)) {
           CombatEngine.getBackground().color = Color.white
           resetBackground = false
       }*/
    }
}