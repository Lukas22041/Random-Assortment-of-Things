package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssSiphonHullmod : BaseHullMod()  {

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
        tooltip.addPara("Enables the fleet to siphon shielding matter from supercharged fog within the abyss. The amount is 5/10/20/50 per day based on hullsize.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "supercharged fog", "5/10/20/50")
    }

}