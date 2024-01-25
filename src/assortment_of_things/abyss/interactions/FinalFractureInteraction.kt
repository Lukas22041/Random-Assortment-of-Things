package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.JumpPointAPI

class FinalFractureInteraction : RATInteractionPlugin() {
    override fun init() {
        textPanel.addPara("You approach the fracture, for a while it has been become incredibly tough for your fleet to advance through the ever densening matter.")

        textPanel.addPara("This fracture appears different however. Unlike others, the matter around it appears still, it appears that we are reaching the singularity of this place. Caution is adviced before moving onwards.")

        createOption("Move through the fracture") {
            var plugin = interactionTarget.customPlugin
            if (plugin is AbyssalFracture)  {
                if (plugin.connectedEntity != null) {
                    Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.connectedEntity, ""), 0.01f)
                    interactionTarget.removeTag("rat_final_fracture")
                    closeDialog()
                }
            }
        }

        addLeaveOption()
    }

}