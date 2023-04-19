package assortment_of_things.campaign.abilities

import assortment_of_things.campaign.world.PocketDimensionSystem
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.isPlayerInHyperspace

class PocketDimensionAbility : BaseDurationAbility() {

    var lastPosition: SectorEntityToken? = null

    override fun activateImpl() {
        if (Global.getSector().isPlayerInHyperspace()) return

        var station = Global.getSector().getEntityById("rat_pocket_entity")

        if (station == null)
        {
            station = PocketDimensionSystem().generate()
        }

        var player = Global.getSector().playerFleet

        if (lastPosition == null)
        {
            var currentSystem = Global.getSector().playerFleet.starSystem
            var pos = currentSystem.addCustomEntity("rat_pocket_back", "", "rat_empty_entity", Factions.NEUTRAL)

            var pocketSystem = station.starSystem
            pocketSystem.location.set(currentSystem.location)

            pos.setLocation(player.location.x, player.location.y)

            lastPosition = pos

            Global.getSector().doHyperspaceTransition(player, player, JumpPointAPI.JumpDestination(station, ""), activationDays)
        }
        else
        {
            var previousSystem = lastPosition!!.starSystem

            Global.getSector().doHyperspaceTransition(player, player, JumpPointAPI.JumpDestination(lastPosition, ""), activationDays)

            previousSystem.removeEntity(lastPosition)
            lastPosition = null

        }
    }

    override fun applyEffect(amount: Float, level: Float) {

    }

    override fun deactivateImpl() {
        if (Global.getSector().isPlayerInHyperspace())
        {
            setCooldownLeft(0.5f)
            return
        }

        if (lastPosition == null)
        {
            setCooldownLeft(10f)
        }
        else
        {
            setCooldownLeft(0.5f)

        }

    }

    override fun cleanupImpl() {

    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        tooltip!!.addPara("Pocket Dimension", 0f, Misc.getBrightPlayerColor(), Misc.getBrightPlayerColor())
        tooltip.addSpacer(8f)

        tooltip.addPara("Jumps in to a different Dimension, in to a system with some abandoned structure.", 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Activate the ablity again to jump back. Afterwards the ability will enter a 10 day cooldown.", 0f)

        if (Global.getSector().isPlayerInHyperspace())
        {
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Can not be used in Hyperspace", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }
}