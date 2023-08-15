package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssalSeraphsGrace : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        var isInAbyss = false
        if (Global.getSector() != null && Global.getSector().playerFleet != null) {
            if (Global.getSector().playerFleet.containingLocation != null)
            {
                if (Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) {
                    isInAbyss = true
                }
            }
        }


        if (isInAbyss) {
            stats!!.maxSpeed.modifyFlat(id, 15f)
            stats!!.fluxDissipation.modifyFlat(id, 100f)
            stats!!.fluxCapacity.modifyFlat(id, 500f)
        }
        else {
            stats!!.maxSpeed.modifyFlat(id, 5f)
            stats!!.fluxDissipation.modifyFlat(id, 25f)
            stats!!.fluxCapacity.modifyFlat(id, 100f)
        }

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var isInAbyss = false
        if (Global.getSector() != null && Global.getSector().playerFleet != null) {
            if (Global.getSector().playerFleet.containingLocation != null)
            {
                if (Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) {
                    isInAbyss = true
                }
            }
        }

        var abyssColor = Misc.getGrayColor()
        var sectorColor = Misc.getHighlightColor()

        if (isInAbyss) {
            abyssColor = Misc.getHighlightColor()
            sectorColor = Misc.getGrayColor()
        }

        tooltip!!.addSpacer(5f)

        tooltip.addPara("This hull comes with a unique apparatus that allows it enhance the ship through flux residue absorbed from the enviroment. " +
                "\n\nThis effect is much stronger while the ship is moving through the abyss.", 0f)

        tooltip.addSpacer(10f)

        val col1W = 150f
        val colW = ((width - col1W - 12f) / 2f).toInt().toFloat()

        var entries = arrayOf<Any>("Stat", col1W, "In Sector", colW, "In Abyss", colW)

        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 20f, true, true,
            *entries)


        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Saving Grace Stacks",
            Alignment.MID, sectorColor, "10",
            Alignment.MID, abyssColor, "25",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Max Speed",
            Alignment.MID, sectorColor, "+5",
            Alignment.MID, abyssColor, "+15",
            )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Flux Dissipation",
            Alignment.MID, sectorColor, "+25",
            Alignment.MID, abyssColor, "+100",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Flux Capacity",
            Alignment.MID, sectorColor, "+100",
            Alignment.MID, abyssColor, "+500",
        )

        tooltip.addTable("", 0, 0f)

        tooltip.addSpacer(15f)

        tooltip.addSectionHeading("Saving Grace", Alignment.MID, 0f)

        tooltip.addSpacer(5f)

        tooltip.addPara("Every 100 units of armor or hull damage taken grant the ship a stack of Saving Grace. The amount of stacks cant exceed the maximum mentioned in the stat grid above. " +
                "\n\n" +
                "Each stack provides the ship with a 1%% increase in weapon rate of fire/flux dissipation/damage reduction and will dissipate after 10 seconds.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "100", "armor or hull", "Saving Grace", "1%", "rate of fire", "flux dissipation", "damage reduction", "10")
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}