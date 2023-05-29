package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.combat.BreachOnHitEffect
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f


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
        tooltip.addPara("Deals an additional 20%% of the weapons damage to armor. This damage is not reduced by armor.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "20%", "not reduced")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.METALS, 10f, this)

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