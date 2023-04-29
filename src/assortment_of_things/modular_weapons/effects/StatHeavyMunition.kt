package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatHeavyMunition : ModularWeaponEffect() {
    override fun getName(): String {
        return "Heavy Munition"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the weapons damage and emp damage by 25%. Also doubles the projectiles width and length.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.damagePerShot.addMult(getName(), 1.2f)
        stats.empDamage.addMult(getName(), 1.2f)

        stats.projectileWidth.addMult(getName(), 2f)
        stats.projectileLength.addMult(getName(), 2f)
    }
}