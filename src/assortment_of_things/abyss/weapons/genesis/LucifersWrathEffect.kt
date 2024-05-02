package assortment_of_things.abyss.weapons.genesis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.BeamEffectPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.combat.TemporalShellStats

class LucifersWrathEffect : BeamEffectPlugin {
    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI?) {


        val target = beam!!.damageTarget
        if (target is ShipAPI && beam!!.brightness >= 0.1f && beam!!.weapon != null) {
            if (!target.hasListenerOfClass(LucifersWrathScript::class.java)) {
                target.addListener(LucifersWrathScript(target))
            }

            val hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.to)
            var script = target.getListeners(LucifersWrathScript::class.java).first()
            script.timer = script.maxTimer
            script.hitShield = hitShield
        }
    }

}

class LucifersWrathScript(var target: ShipAPI) : AdvanceableListener {

    var maxTimer = 0.3f
    var timer = 0.3f

    var hitShield = false

    var id = "rat_lucifers_wrath" + target.id

    override fun advance(amount: Float) {
        var stats = target.mutableStats

        var effectLevel = timer / maxTimer
        var player = Global.getCombatEngine().playerShip == target

        var mult = 0f

        if (timer >= 0f) {
            timer -= 1 * amount

            mult = 0.15f
            if (hitShield) mult = 0.10f
        }

        val shipTimeMult = 1f - (mult * effectLevel)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
//			if (ship.areAnyEnemiesInRange()) {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			} else {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 2f / shipTimeMult);
//			}
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }
    }
}