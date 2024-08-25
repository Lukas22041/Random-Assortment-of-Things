package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import org.lwjgl.util.vector.Vector2f

class StardustLanceEffect : BeamEffectPlugin, DamageDealtModifier {


    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI) {

        var target = beam.damageTarget

        if (target is ShipAPI) {
            var listener = target.getListeners(StardustLanceListener::class.java).firstOrNull()

            if (listener != null) {
                target.removeListener(listener)
            }

            target.addListener(StardustLanceListener(target, 0.25f))
        }


        if (!beam.source.hasListenerOfClass(this::class.java)) {
            beam.source.addListener(this)
        }

    }

    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?,  damage: DamageAPI?,  point: Vector2f?, shieldHit: Boolean): String? {
        if (param !is BeamAPI) return null
        if (param.weapon?.spec?.weaponId != "rat_stardust_lance") return null
        if (target !is ShipAPI) return null
        if (!target.hasListenerOfClass(MoonlightLanceListener::class.java)) return null

        var dps = damage!!.dpsDuration
        if (dps <= 0) return null

        damage.modifier.modifyMult("rat_stardust_lance", 1.2f)

        return "rat_stardust_lance"
    }
}

class StardustLanceListener(var target: ShipAPI, var time: Float) : AdvanceableListener {
    override fun advance(amount: Float) {
        time -= 1 * amount
        if (time < 0) {
            target.removeListener(this)
        }
    }
}