package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import kotlin.random.Random


class PassiveDefenseProtocol : ModularWeaponEffect() {
    override fun getName(): String {
        return "Defense Protocol"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Causes the weapon to behave as a Point-Defense weapon.", 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("- Increases fire rate by 50%", 0f)
        tooltip.addPara("- Decrease flux useage by 50%", 0f)
        tooltip.addPara("- Decrease damage by 50%", 0f)
        tooltip.addPara("- Halves the weapons Range.", 0f)
        tooltip.addPara("- Increases weapon turnrate by 100%", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Passive
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.isPD = true

        stats.range.addMult(getName(), 0.5f)

        stats.burstDelay.addMult(getName(), 0.50f)
        stats.chargeDown.addMult(getName(), 0.50f)
        stats.chargeUp.addMult(getName(), 0.50f)

        stats.damagePerShot.addMult(getName(), 0.5f)
        stats.empDamage.addMult(getName(), 0.5f)

        stats.energyPerShot.addMult(getName(), 0.50f)

        stats.turnrate.addMult(getName(), 2f)

    }
}