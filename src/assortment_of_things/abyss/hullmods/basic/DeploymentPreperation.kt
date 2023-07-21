package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.combat.ShipAPI.HullSize

class DeploymentPreperation : BaseAlteration() {


    var modID = "rat_preperation"
    var deploymentModID = "deployment_points_mod"

    var dp = mapOf(HullSize.FRIGATE to -1f, HullSize.DESTROYER to -2f, HullSize.CRUISER to -3f, HullSize.CAPITAL_SHIP to -3f)

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.getSuppliesToRecover().modifyFlat(modID, dp.get(hullSize)!!);
        stats.getDynamic().getMod(deploymentModID).modifyFlat(modID, dp.get(hullSize)!!);
        stats.suppliesPerMonth.modifyMult(modID, 1.5f)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        var label = tooltip.addPara("The hull receives more preperation for future deployment, decreasing the deployment cost by 1/2/3/3 points, but increasing the monthly supply useage by 50%%"
            , 0f, Misc.getTextColor(), Misc.getHighlightColor())
        label.setHighlight("deployment cost", "1/2/3/3", "50%")
        label.setHighlightColors(hc, hc, nc)

    }

}