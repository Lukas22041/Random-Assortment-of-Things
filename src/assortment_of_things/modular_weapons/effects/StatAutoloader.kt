package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatAutoloader : ModularWeaponEffect() {
    override fun getName(): String {
        return "Autoloader"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the weapons firerate by 25%. If the weapon uses ammo, increases the maximum ammo count by 20 and increases the ammo recharge rate by 1.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.ammoPerSecond.addFlat(getName(), 1f)
        stats.maxAmmo.addFlat(getName(), 20)
        stats.reloadSize.addFlat(getName(), 1f)

        stats.burstDelay.addMult(getName(), 0.75f)
        stats.chargeDown.addMult(getName(), 0.75f)
        stats.chargeUp.addMult(getName(), 0.75f)
    }
}