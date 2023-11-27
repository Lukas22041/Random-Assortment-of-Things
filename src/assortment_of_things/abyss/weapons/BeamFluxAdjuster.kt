package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import org.lwjgl.util.vector.Vector2f

class BeamFluxAdjuster : BeamEffectPlugin, DamageDealtModifier {
    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI) {

        if (!beam.source.hasListenerOfClass(this::class.java)) {
            beam.source.addListener(this)
        }
    }

    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?,  damage: DamageAPI?,  point: Vector2f?, shieldHit: Boolean): String? {
        if (param !is BeamAPI) return null
        if (target !is ShipAPI) return null
        if (param.weapon.id != "rat_asterias" && param.weapon.id != "rat_asterina") return null

        if (!shieldHit) return null

        var modID = "RAT_BeamFluxAdjust"

        var mod = 1.333f - (0.666f * target.fluxLevel)
        damage!!.modifier.modifyMult(modID, mod)

        return modID
    }
}