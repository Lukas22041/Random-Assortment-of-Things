package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatDampener : ModularWeaponEffect() {
    override fun getName(): String {
        return "Dampener"
    }

    override fun getCost(): Int {
        return 10
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Reduces the Weapons Spread by 50%.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.minSpread.modifyMult(getName(), 0.5f)
        stats.maxSpread.modifyMult(getName(), 0.5f)

    }

}