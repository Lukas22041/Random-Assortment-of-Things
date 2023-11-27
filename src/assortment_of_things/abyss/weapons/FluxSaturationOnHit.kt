package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnHitEffectPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lwjgl.util.vector.Vector2f

class FluxSaturationOnHit : OnHitEffectPlugin {
    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?,  point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        if (target !is ShipAPI) return

        if (!target.hasListenerOfClass(FluxSaturationScript::class.java)) {
            target.addListener(FluxSaturationScript(target))
        }

        var listener = target.getListeners(FluxSaturationScript::class.java).firstOrNull()
        if (listener !is FluxSaturationScript) return

        if (listener.stacks.size < 20) {
            listener.stacks.add(FluxSaturationScript.SaturationStack(5f))
        }
    }
}