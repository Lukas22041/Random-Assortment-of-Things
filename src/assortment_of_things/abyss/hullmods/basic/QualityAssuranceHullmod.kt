package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.combat.ShipAPI.HullSize

class QualityAssuranceHullmod : BaseAlteration() {


    var modID = "rat_quality_assurance"

    var deploymentModID = "deployment_points_mod"

    var dp = mapOf(HullSize.FRIGATE to 2f, HullSize.DESTROYER to 3f, HullSize.CRUISER to 4f, HullSize.CAPITAL_SHIP to 6f)
    var armor = mapOf(HullSize.FRIGATE to 100f, HullSize.DESTROYER to 150f, HullSize.CRUISER to 250f, HullSize.CAPITAL_SHIP to 350f)
    var speed = mapOf(HullSize.FRIGATE to 25f, HullSize.DESTROYER to 20f, HullSize.CRUISER to 15f, HullSize.CAPITAL_SHIP to 10f)
    //var diss = mapOf(HullSize.FRIGATE to 50f, HullSize.DESTROYER to 100f, HullSize.CRUISER to 150f, HullSize.CAPITAL_SHIP to 250f)

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)


        /*stats!!.getSuppliesToRecover().modifyFlat(modID, dp.get(hullSize)!!);
        stats.getDynamic().getMod(deploymentModID).modifyFlat(modID, dp.get(hullSize)!!);*/

        stats!!.armorBonus.modifyFlat(modID, armor.get(hullSize)!!)
        stats.maxSpeed.modifyFlat(modID, speed.get(hullSize)!!)
       // stats.fluxDissipation.modifyFlat(modID, diss.get(hullSize)!!)
        stats.fluxDissipation.modifyMult(modID, 1.05f)

       /* stats.ballisticWeaponFluxCostMod.modifyMult(modID, 0.9f)
        stats.energyWeaponFluxCostMod.modifyMult(modID, 0.9f)
        stats.missileWeaponFluxCostMod.modifyMult(modID, 0.9f)*/
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {


        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        var label = tooltip.addPara("The ship receives improved maintenance and uses better performing components.This causes the ship to perform better in combat.\n\n" +
                "The ship gains an additional 100/150/250/350 units of armor. Its max speed is increased by 25/20/15/10 and " +
                "it receives an increase in flux dissipation of 5%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor())
        label.setHighlight("deployment cost", "2/3/4/6", "100/150/250/350", "armor", "max speed", "25/20/15/10","flux dissipation","5%")
        label.setHighlightColors(nc, nc, hc,hc,hc,hc,hc,hc,hc,hc, )

    }

}