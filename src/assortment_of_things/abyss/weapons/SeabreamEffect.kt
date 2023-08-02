package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import org.lwjgl.util.vector.Vector2f

class SeabreamEffect : BeamEffectPlugin, DamageDealtModifier {
    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI) {

        if (!beam.source.hasListenerOfClass(this::class.java)) {
            beam.source.addListener(this)
        }
    }

    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?,  damage: DamageAPI?,  point: Vector2f?, shieldHit: Boolean): String? {
        if (param !is BeamAPI) return null
        if (target !is ShipAPI) return null
        if (param.weapon.id != "rat_seabream") return null

        var dps = damage!!.dpsDuration
        if (dps <= 0) return null

        val dam = (damage.damage * damage.dpsDuration) * 0.2f
        var engine = Global.getCombatEngine()

        engine!!.applyDamage(target, point, dam, DamageType.FRAGMENTATION, 0f, false, true, null)

        return null
    }
}