package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.artifacts.BaseArtifactPlugin
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.Automated
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCUtils
import second_in_command.scripts.AutomatedShipsManager

class ComputationalMatrix : BaseArtifactPlugin() {

    companion object {
        var points = 90f
        var isSiCEnabled = Global.getSettings().modManager.isModEnabled("second_in_command")
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {

        if (!isSiCEnabled) {
            tooltip.addPara("All automated ships that are affected by the combat readiness penalty get an additional 15%% to their maximum CR.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "automated ships", "combat readiness penalty", "15%")
        } else {

            var manager = AutomatedShipsManager.get()
            var provided = points.toInt()
            var maximum = manager.getMaximumDP().toInt()
            var used = manager.getUsedDP()
            tooltip.addPara("+$provided automated ship points", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$provided")

            tooltip.addSpacer(10f)

            tooltip.addPara("Having more than zero automated ship points enables the recovery of automated ships. Automated ships can only be captained by AI cores.", 0f, Misc.getTextColor(), Misc.getHighlightColor())

            tooltip.addSpacer(10f)

            /*if (ArtifactUtils.getActiveArtifact()?.id == "computational_matrix") {
                maximum += provided
            }*/

            tooltip.addPara("Your fleet currently has a maximum of $maximum automated ship points.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$maximum")
        }


    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        if (stats.variant.hasHullMod(HullMods.AUTOMATED)) {
            if (!isSiCEnabled) {
                if (!Automated.isAutomatedNoPenalty(stats))
                {
                    stats.maxCombatReadiness.modifyFlat(id, 0.15f, "Computation Matrix")
                }
            }
        }
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {
        if (isSiCEnabled) {
            Global.getSector().playerPerson.stats.dynamic.getMod("sc_auto_dp").modifyFlat(id+"_external", points, "Computational Matrix Artifact")
        }
    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {
        if (isSiCEnabled) {
            Global.getSector().playerPerson.stats.dynamic.getMod("sc_auto_dp").unmodify(id+"_external")
        }
    }

}