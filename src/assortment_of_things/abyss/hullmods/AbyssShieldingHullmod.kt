package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssShieldingHullmod : BaseHullMod() {

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addSpacer(5f)
        tooltip.addPara("A hullmod that doesnt appear to be in its best shape anymore, it seems to have been in decay for dozens of cycles.", 0f)
        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("Effect", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Increases the maximum amount of abyssal shielding that the fleet can store. It is increased by 20/30/50/100 units based on hullsize.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "abyssal shielding", "20/30/50/100")
    }


}