package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import lunalib.lunaDelegates.LunaMemory
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f

class ExoshipInteractions : RATInteractionPlugin() {

    override fun init() {

        var firstInteraction = interactionTarget.memoryWithoutUpdate.get("\$rat_exoship_first")

        if (firstInteraction != true) {
            firstInteraction()
            //return
        }

        //Maybe just done have one, invent a person for commander?
        createOption("Open the comm directory") {
            dialog.showCommDirectoryDialog(interactionTarget.market.commDirectory)
        }

        createOption("Access Cargo Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, interactionTarget) { }
        }
        optionPanel.setShortcut("Access Cargo Storage", Keyboard.KEY_I, false, false, false, false)

        createOption("Inspect your Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, interactionTarget) { }
        }
        optionPanel.setShortcut("Inspect your Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Use the local facilities to refit your fleet") {
            visualPanel.showCore(CoreUITabId.REFIT, interactionTarget) { }
        }
        optionPanel.setShortcut("Use the local facilities to refit your fleet", Keyboard.KEY_R, false, false, false, false)

        /*createOption("Move") {
            var ship = interactionTarget
            ship.orbit = null
            ship.velocity.set(Vector2f(0f, 0f))
            ship.addTag(Tags.NON_CLICKABLE)

            var system = Global.getSector().starSystems.filter { it.planets.any { planet -> !planet.isStar } && it != ship.starSystem }.random()
            var planet = system.planets.filter { !it.isStar }.random()

            Global.getSector().addScript(ExoshipMoveScript(ship, planet))

            closeDialog()
        }*/



        addLeaveOption()
    }

    fun firstInteraction() {
        textPanel.addPara("You enter the vicinity of the Exoship \"${interactionTarget.name.replace("(Exoship)", "")}\".")

        textPanel.addPara("A rare sight within the current cycle, a collosal machine that had once dominated smaller factions and communities within the early post-domain era sector. " +
                "Their ability to quickly traverse between systems without requiring direct hyperspace access made them an unstopable force, however due to their limited production count, larger factions were able to set a quick end to the companies efforts.")

        textPanel.addPara("After a brutal loss, their efforts have been relocated to defending the remaining domain-grade technology that they have acquired from their hauls, having left the core-worlds influence since.")

        createOption("Continue") {
            clearOptions()
        }
    }
}