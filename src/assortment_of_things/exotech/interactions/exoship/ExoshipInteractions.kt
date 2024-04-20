package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoShipData
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.impl.items.*
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f


class ExoshipInteractions : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()
    var person = data.commPerson

    lateinit var faction: FactionAPI
    lateinit var exoshipData: ExoShipData

    companion object {
        var colonyEquipmentMod = 0.85f
        var blueprintMod = 0.50f
        var aiCoreMod = 0.25f
        var alterationMod = 0.25f

        var tokenConversionRatio = 0.01f
        var shipExtraCost = 1.25f
    }




    override fun init() {
        exoshipData = ExoUtils.getExoshipData(interactionTarget)
        faction = interactionTarget.faction

        textPanel.addPara("You enter the vicinity of the Exoship \"${interactionTarget.market.name}\".")

        if (faction.relToPlayer.isHostile) {
            textPanel.addPara("As the fleet attempts to close in, multiple weapon systems go online. It appears due to your reputation with the faction, any attempt at communication is being denied by default.")

            addLeaveOption()
            return
        }


        var firstInteraction = data.interactedWithExoship

        if (!firstInteraction) {
            firstInteraction()
            return
        }

        if (!data.hasPartnership) {
            textPanel.addPara("${data.commPerson.nameString}, the chief of external communications, accepts the comm request, waiting to see if you have anything of worth in store for them. The call seems to have some delay in its transmissions, making it unclear if she is currently on the exoship or not.")
        }
        else {
            textPanel.addPara("${data.commPerson.nameString}, the chief of external communications, accepts the comm request, she appears to look forward to what good fortune you will bring. Despite your partnership, you are still not allowed access to the core of the station.")
        }




        populateOptions()






    }

    fun populateOptions() {

        visualPanel.showPersonInfo(person)

        var hasTech = false
        for (stack in Global.getSector().playerFleet.cargo.stacksCopy) {
            if (ExoShipSellInteraction.isRareTech(stack)) {
                hasTech = true
                break
            }
        }

        var currentClass = this

        createOption("Trade in tech") {
            ExoShipSellInteraction(currentClass, data).tradeInTech()
        }
        if (!hasTech) {
            optionPanel.setEnabled("Trade in tech", false)
            optionPanel.setTooltip("Trade in tech", "You dont have anything the faction seeks.")
        }

        createOption("Exchange tokens") {
            ExoShipBuyInteraction(currentClass, data).buyTech()
        }

        var current = this
        createOption("Request exo-tech equipment") {
            dialog.showCustomProductionPicker(ExoshipCustomProduction(current))
        }
        var productionTooltip = "Allows requesting ships, weapons and fighters from the coorperation with tokens. "
        if (!data.hasPartnership) {
            productionTooltip += "Requires the acquisition of the \"partnership deed\" to be accessed. "
            optionPanel.setEnabled("Request exo-tech equipment", false)
        }
        optionPanel.setTooltip("Request exo-tech equipment", productionTooltip)

        optionPanel.setShortcut("Use the local facilities to refit your fleet", Keyboard.KEY_R, false, false, false, false)

        if (Global.getSettings().isDevMode) {
            createOption("(Devmode) Force Move") {

                //exoshipData.daysBetweenMoves = 0f
                exoshipData.lastMoveTimestamp = Global.getSector().clock.timestamp
                exoshipData.daysBetweenMoves = 0.5f

                /*var ship = interactionTarget
                ship.orbit = null
                ship.velocity.set(Vector2f(0f, 0f))
                ship.addTag(Tags.NON_CLICKABLE)

                var system = Global.getSector().starSystems.filter { it.planets.any { planet -> !planet.isStar } && it != ship.starSystem }.random()
                var planet = system.planets.filter { !it.isStar }.random()

                Global.getSector().addScript(ExoshipMoveScript(ship, planet))*/

                closeDialog()
            }
        }


        addLeaveOption()
    }








    fun firstInteraction() {

        textPanel.addPara("A rare sight within the current cycle, a colossal machine that had once dominated smaller factions and communities within the early post-domain era sector. " +
                "Originaly constructed for logistical purposes in fringe sectors, but when the gate network eventualy failed the company that designed and constructed the exoships, going by \"Exo-Tech\", found new use for them. ")


        textPanel.addPara("Their ability to quickly traverse between systems without requiring direct hyperspace access made them an unstopable force, however due to their limited production count, larger factions were able to set a quick end to the companies efforts. " +
                "After a brutal loss, their efforts have been relocated to defending the remaining domain-grade technology that they have acquired from their hauls, having left the core-worlds influence since.")

        textPanel.addPara("Added \"Exo-Tech\" to faction dictionary in the intel tab.", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

        createOption("Continue") {
            clearOptions()

            textPanel.addPara("As you close in further, an immediate comm-request rolls in. At the same point time, your fleet detects the \"stations\" weapon systems heating up.")

            textPanel.addPara("\"State the purpose of your approach\", says the sudden and harsh voice emitting from the holo-call. \"Do so with speed, or expect repurcussions\".")

            visualPanel.showPersonInfo(person)

            createOption("\"I want to trade\"") {
                clearOptions()

                textPanel.addPara("\"We do not trade with simple outsiders, that is if you dont have any wares to show your worth. ")

                endFirstInteraction()


            }

            createOption("\"I want to enter a commission\"") {
                clearOptions()

                textPanel.addPara("\"If you havent been part of the company at it inception, or have proof of connection to a member, you wont find your way in to a commission. " +
                        "However, if you have the rare wares to show it, we may settle on a special relationship.")

                endFirstInteraction()

            }
        }
    }

    fun endFirstInteraction() {

        textPanel.addPara("Most wares are of little importance to our operations, however we are in constant search for rare technologies, like colony equipment, blueprints and ai cores. " +
                "Provide them to us and we pay you back in our own tokens. A currency that is localy used within our communities, it will be of little worth anywhere else, but we may be able to provide you with some of our own technologies in exchange for them. ",
        Misc.getTextColor(), Misc.getHighlightColor(), "rare technologies", "colony equipment", "blueprints", "ai cores", "tokens")


        textPanel.addPara("We may consider a deeper partnership based on what you can bring to the table.\"")

        createOption("Consider your options") {
            clearOptions()
            data.interactedWithExoship = true
            populateOptions()
        }
    }
}