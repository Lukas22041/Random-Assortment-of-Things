package assortment_of_things.campaign.abilities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.isPlayerInHyperspace
import org.lwjgl.input.Keyboard

class SingularityJumpAbility : BaseDurationAbility() {

    var lastPosition: SectorEntityToken? = null



    override fun activateImpl() {

        var key = "\$rat_abyss_token"
        var lastAbyssKey = "\$rat_last_abyss_location"
        var memory = Global.getSector().memoryWithoutUpdate

        var hyperspaceToken: SectorEntityToken? = null
        if (!memory.contains(key))
        {
            hyperspaceToken = Global.getSector().hyperspace.createToken(0f, 0f)
            Global.getSector().hyperspace.addEntity(hyperspaceToken)
        }
        else
        {
            hyperspaceToken = memory.get(key) as SectorEntityToken
        }

        var lastAbyssToken: SectorEntityToken? = null
        if (!memory.contains(lastAbyssKey))
        {
            lastAbyssToken =  AbyssUtils.getAllAbyssSystems().find { it.baseName == "Midnight" }!!.createToken(0f, 0f)
            AbyssUtils.getAllAbyssSystems().find { it.baseName == "Midnight" }!!.addEntity(lastAbyssToken)
            memory.set(lastAbyssKey, lastAbyssToken)
        }
        else
        {
            lastAbyssToken = memory.get(lastAbyssKey) as SectorEntityToken
        }

        var player = Global.getSector().playerFleet
        if (player.isInHyperspace)
        {
            hyperspaceToken!!.location.set(player.location.x, player.location.y)
            memory.set(key, hyperspaceToken)

            var token = memory.get("\$rat_abyss_midnight_token") as SectorEntityToken?


            for (system in Global.getSector().starSystems.filter { it.hasTag(AbyssUtils.SYSTEM_TAG) })
            {
                system.location.set(player.location.x, player.location.y)
            }

            var visual = player.containingLocation.addCustomEntity("rat_fracture_visual", "", "rat_abyss_fracture_jumpvisual",Factions.NEUTRAL)
            visual.location.set(player.location)

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && AbyssalDepthsEventIntel.get()!!.isStageActive(AbyssalDepthsEventIntel.Stage.RETURNAL))
            {
                var visual2 = lastAbyssToken!!.containingLocation.addCustomEntity("rat_fracture_visual", "", "rat_abyss_fracture_jumpvisual",Factions.NEUTRAL)
                visual2.location.set(lastAbyssToken!!.location)
                Global.getSector().doHyperspaceTransition(player, visual, JumpPointAPI.JumpDestination(lastAbyssToken, ""), 2f)
            }
            else
            {
                var visual2 = token!!.containingLocation.addCustomEntity("rat_fracture_visual", "", "rat_abyss_fracture_jumpvisual",Factions.NEUTRAL)
                visual2.location.set(token.location)
                Global.getSector().doHyperspaceTransition(player, visual, JumpPointAPI.JumpDestination(token, ""), 2f)
            }
        }
        if (player.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
        {
            var lastSystem = lastAbyssToken!!.containingLocation
            lastSystem.removeEntity(lastAbyssToken)

            lastAbyssToken = player.containingLocation.createToken(0f, 0f)
            player.containingLocation.addEntity(lastAbyssToken)
            lastAbyssToken.location.set(player.location.x, player.location.y)
            memory.set(lastAbyssKey, lastAbyssToken)

            var visual = player.containingLocation.addCustomEntity("rat_fracture_visual", "", "rat_abyss_fracture_jumpvisual",Factions.NEUTRAL)
            visual.location.set(player.location)
            var visual2 = hyperspaceToken!!.containingLocation.addCustomEntity("rat_fracture_visual", "", "rat_abyss_fracture_jumpvisual",Factions.NEUTRAL)
            visual2.location.set(hyperspaceToken.location)

            Global.getSector().doHyperspaceTransition(player, visual, JumpPointAPI.JumpDestination(hyperspaceToken, ""), 2f)

        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun getSpriteName(): String {
        var path = "graphics/icons/abilities/rat_singularity_jump.png"
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && AbyssalDepthsEventIntel.get() != null && AbyssalDepthsEventIntel.get()!!.isStageActive(AbyssalDepthsEventIntel.Stage.RETURNAL)) {
            path = "graphics/icons/abilities/rat_singularity_jump_returnal.png"
        }

        return path
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

        tooltip.addPara("Destabilizes the surrounding Hyperspace, opening a rift towards the \"Abyss\".", 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("Activating it within the abyss returns the fleet back to hyperspace.", 0f)
        tooltip.addSpacer(5f)

        if (AbyssalDepthsEventIntel.get() != null && AbyssalDepthsEventIntel.get()!!.isStageActive(AbyssalDepthsEventIntel.Stage.RETURNAL))
        {
            tooltip.addPara("Holding L-CTRL while in hyperspace instead moves the fleet back to the location it last left the abyss from.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "L-CTRL")
        }

        if (!Global.getSector().isPlayerInHyperspace() && !Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
        {
            tooltip.addSpacer(5f)
            tooltip.addPara("Can only be used in Hyperspace or in the Abyss", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }
}