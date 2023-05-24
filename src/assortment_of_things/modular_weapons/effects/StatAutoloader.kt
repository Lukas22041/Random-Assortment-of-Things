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

    override fun getResourceCost(data: SectorWeaponData) {

    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.ammoPerSecond.modifyFlat(getName(), 1f)
        stats.maxAmmo.modifyFlat(getName(), 20)
        stats.reloadSize.modifyFlat(getName(), 1f)

        stats.burstDelay.modifyMult(getName(), 0.75f)
        stats.chargeDown.modifyMult(getName(), 0.75f)
        stats.chargeUp.modifyMult(getName(), 0.75f)
    }
}