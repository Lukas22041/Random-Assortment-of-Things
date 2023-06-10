package assortment_of_things.campaign.abilities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.campaign.world.PocketDimensionSystem
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.isPlayerInHyperspace

class SingularityJumpAbility : BaseDurationAbility() {

    var lastPosition: SectorEntityToken? = null

    override fun activateImpl() {

        var key = "\$rat_abyss_token"
        var memory = Global.getSector().memoryWithoutUpdate

        var hyperspaceToken: SectorEntityToken? = null
        if (!memory.contains(key))
        {
            hyperspaceToken = Global.getSector().hyperspace.createToken(0f, 0f)
        }
        else
        {
            hyperspaceToken = memory.get(key) as SectorEntityToken
        }

        var player = Global.getSector().playerFleet
        if (player.isInHyperspace)
        {
            hyperspaceToken!!.location.set(player.location.x, player.location.y)
            memory.set(key, hyperspaceToken)

            var token = memory.get("\$rat_abyss_midnight_token") as SectorEntityToken

            token.starSystem.location.set(player.location.x, player.location.y)

            Global.getSector().doHyperspaceTransition(player, player, JumpPointAPI.JumpDestination(token, ""), 0.2f)
        }
        if (player.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
        {
            Global.getSector().doHyperspaceTransition(player, player, JumpPointAPI.JumpDestination(hyperspaceToken, ""), 0.2f)
        }

    }

    override fun applyEffect(amount: Float, level: Float) {

    }

    override fun deactivateImpl() {

    }


    override fun cleanupImpl() {

    }

    override fun hasTooltip(): Boolean {
        return true
    }


    override fun isUsable(): Boolean {
        if (!super.isUsable()) return false
        if (fleet.isInHyperspaceTransition) return false
        return Global.getSector().isPlayerInHyperspace() || Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        tooltip!!.addPara("Singularity Jump", 0f, Misc.getBrightPlayerColor(), Misc.getBrightPlayerColor())
        tooltip.addSpacer(8f)

        tooltip.addPara("Descends in to the depths of hyperspace, the abyss.", 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Sustained stay is impossible without proper shielding. It is adviced to look for a source of it upon arrival, or the fleet will be ejected back in to hyperspace.", 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Activating it within the abyss returns the fleet back to hyperspace.", 0f)

        if (!Global.getSector().isPlayerInHyperspace() && !Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
        {
            tooltip.addSpacer(5f)
            tooltip.addPara("Can only be used in Hyperspace or in the Abyss", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }
}