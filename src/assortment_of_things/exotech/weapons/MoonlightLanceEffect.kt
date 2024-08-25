package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import org.lwjgl.util.vector.Vector2f

class MoonlightLanceEffect : BeamEffectPlugin, DamageDealtModifier {

    var currentTarget: CombatEntityAPI? = null

    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI) {

        var target = beam.damageTarget

        if (target is ShipAPI) {
            var listener = target.getListeners(MoonlightLanceListener::class.java).firstOrNull()

            if (listener != null) {
                target.removeListener(listener)
            }

            target.addListener(MoonlightLanceListener(target, 0.25f))
        }


        if (!beam.source.hasListenerOfClass(this::class.java)) {
            beam.source.addListener(this)
        }

    }

    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?,  damage: DamageAPI?,  point: Vector2f?, shieldHit: Boolean): String? {
        if (!shieldHit) return null
        if (param !is BeamAPI) return null
        if (param.weapon?.spec?.weaponId != "rat_moonlight_lance") return null
        if (target !is ShipAPI) return null
        if (damage!!.isForceHardFlux) return null
        if (!target.hasListenerOfClass(StardustLanceListener::class.java)) return null

        var dps = damage!!.dpsDuration
        if (dps <= 0) return null

        //Apply extra damage
        val dam = (damage.damage * damage.dpsDuration) * 0.33f
        Global.getCombatEngine()!!.applyDamage(target, point, dam, damage.type, 0f, false, false, null)

        //Reduce damage dealt
        damage.modifier.modifyMult("rat_moonlight_lance", 1/1.33f)

        return "rat_moonlight_lance"
    }
}

class MoonlightLanceListener(var target: ShipAPI, var time: Float) : AdvanceableListener {
    override fun advance(amount: Float) {
        time -= 1 * amount
        if (time < 0) {
            target.removeListener(this)
        }
    }
}