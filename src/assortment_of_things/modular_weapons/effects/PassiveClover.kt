package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class PassiveClover : ModularWeaponEffect() {
    override fun getName(): String {
        return "57 Leaf Clover "
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Everytime a modifier related RNG check fails (i.e, rng chance for an EMP to spawn), it will re-attempt the check once more, basicly improving \"luck\".", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Passive
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.rngAttempts.modifyFlat(getName(), 1)
    }

}