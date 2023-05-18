package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.impl.combat.BreachOnHitEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.coreui.x
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicFakeBeam
import org.magiclib.util.MagicLensFlare


class OnHitBreach : ModularWeaponEffect() {
    override fun getName(): String {
        return "Breach"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Deals an additional 20% of the weapons damage to armor. This damage is not reduced by armor.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Onhit
    }

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)
        var data = ModularWeaponLoader.getData(projectile!!.weapon.id)

        if (!shieldHit && target is ShipAPI) {
            BreachOnHitEffect.dealArmorDamage(projectile,
                target as ShipAPI?,
                point,
                data.damagePerShot.modifiedValue * 0.20f)
        }
    }

}