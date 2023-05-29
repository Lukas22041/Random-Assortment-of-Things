package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


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
        tooltip.addPara("Everytime a modifier related RNG check fails (i.e, rng chance for an EMP to spawn), it will re-attempt the check once more, basicly improving \"luck\".", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "re-attempt", "luck")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.RARE_METALS, 10f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Passive
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.rngAttempts.modifyFlat(getName(), 1)
    }

}