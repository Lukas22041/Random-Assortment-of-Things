package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnHitEffectPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lwjgl.util.vector.Vector2f

class SpatialInstabilityOnHit : OnHitEffectPlugin {
    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?,  point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        if (target !is ShipAPI) return
        if (shieldHit) return

        if (!target.hasListenerOfClass(SpatialInstabilityScript::class.java)) {
            target.addListener(SpatialInstabilityScript(target))
        }

        var listener = target.getListeners(SpatialInstabilityScript::class.java).firstOrNull()
        if (listener !is SpatialInstabilityScript) return

        if (listener.stacks.size < 30) {
            listener.stacks.add(SpatialInstabilityScript.InstabilityStack(3f))
        }

    }
}