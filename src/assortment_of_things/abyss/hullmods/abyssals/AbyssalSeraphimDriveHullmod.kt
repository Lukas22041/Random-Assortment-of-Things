package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement

class AbyssalSeraphimDriveHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.sensorProfile.modifyMult(id, 0.5f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The ships drivesystem is connected to a unique type of phase-coil. It enables the ship to enter phase-space while being affected by abyssal phenonema. \n\n" +
                "Stacks of \"Saving Grace\" from the \"Seraphs Grace\" hullmod affect the amount of stress the ships coils can take. The speed reduction from phase coil stress is reduced by up to 50%% at 30 stacks.\n\n" +
                "All deployed fighters are also interconnected to this shipsystem and are forced in to phase-space whenever the ship is aswell. \n\n" +
                "The ships sensor profile is decreased by 50%%. ",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "Saving Grace", "Seraphs Grace", "speed reduction", "50%", "30", "deployed fighters", "50%")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }
    }

    override fun getDisplaySortOrder(): Int {
        return 0
    }


    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}