package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class HyperspaceBeaconFacility : BaseSettlementFacility() {

    var maxDistanceLY = 25
    var maxMod = 0.3

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Generates live positioning and topological hyperspace data, used to traverse it more efficiently. " +
                "Decreases the fuel useage in hyperspace by 30%%, but this effect diminishes with distance from the settlement. At a distance of $maxDistanceLY light-years none of the effect remains.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "fuel useage", "30%", "$maxDistanceLY")
    }

    override fun advance(amount: Float) {
        var playerfleet = Global.getSector().playerFleet ?: return
        var distance = Misc.getDistanceLY(playerfleet.locationInHyperspace, settlement.primaryPlanet.locationInHyperspace)
        var level = distance / maxDistanceLY

        level = 1 - level
        level = MathUtils.clamp(level, 0f, 1f)

        playerfleet.stats.fuelUseHyperMult.modifyMult("rat_hyperspace_beacon", 1f - (0.3f * level))
    }

    override fun unapply() {
        var playerfleet = Global.getSector().playerFleet ?: return

        playerfleet.stats.fuelUseHyperMult.unmodifyMult("rat_hyperspace_beacon")
    }
}