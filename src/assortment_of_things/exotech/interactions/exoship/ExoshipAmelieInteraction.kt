package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.event.DonatedItemFactor
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.impl.items.*
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import second_in_command.SCUtils
import second_in_command.specs.SCOfficer

class ExoshipAmelieInteraction(var original: ExoshipInteractionPlugin) : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {
        talkToAmelie()
    }

    //Amelie
    fun talkToAmelie() {
        visualPanel.showPersonInfo(data.amelie)

        textPanel.addPara("\"Welcome Back. I assume you've brought some good news?\"")

        if (Global.getSettings().modManager.isModEnabled("second_in_command") && !ExoUtils.getExoData().claimedExotechOfficer) {
            textPanel.addPara("\"On that note, i have someone that may be of use to you.\"")
        }


        populateAmelieDialog()
    }

    fun populateAmelieDialog() {
        createOption("Transfer unique items.") {
            donateItems()
        }

        if (Global.getSettings().modManager.isModEnabled("second_in_command") && !ExoUtils.getExoData().claimedExotechOfficer) {
            createOption("Talk about the potential hire") {
                clearOptions()

                var person = Global.getSector().getFaction("rat_exotech").createRandomPerson(FullName.Gender.MALE)
                var officer = SCOfficer(person, "rat_exotech")
                officer.person.portraitSprite = "graphics/portraits/rat_exo_exec.png"
                officer.increaseLevel(1)

                visualPanel.showSecondPerson(officer.person)

                textPanel.addPara("\"Someone from my unit just finished the last task i have given them. " +
                        "At the moment I do not require ${officer.person.hisOrHer} particular skills for any of my current tasks, so I want ${officer.person.himOrHer} transferred over to your fleet to assist you with your own work.\n\n" +
                        "" +
                        "${officer.person.heOrShe.capitalize()} is an executive officer specializing within the fleets own doctrine, ${officer.person.heOrShe} should be of use if you plan to replicate our fleet structure, or if you were to try something similar.\"",
                Misc.getTextColor(), Misc.getHighlightColor(), "executive officer")

                SCUtils.showSkillOverview(dialog, officer)

                textPanel.addPara("\"Don't worry, ${officer.person.hisOrHer} salary will continue to be paid from our side. I hope ${officer.person.heOrShe} will be of good use to you. \"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "executive officer")

                SCUtils.getPlayerData().addOfficerToFleet(officer)
                SCUtils.getPlayerData().setOfficerInEmptySlotIfAvailable(officer)
                dialog.textPanel.addParagraph("${person.nameString} (level ${officer.getCurrentLevel()}) has joined your fleet",  Misc.getPositiveHighlightColor())

                ExoUtils.getExoData().claimedExotechOfficer = true

                createOption("Back") {
                    clearOptions()
                    visualPanel.hideSecondPerson()
                    populateAmelieDialog()
                }
            }
        }


        createOption("Ask her some questions.") {
            clearOptions()
            textPanel.addPara("\"What do you want to know?\"")
            populateAmelieTalk()
        }

        createOption("Back") {
            clearOptions()

            dialog.plugin = original
            original.populateOptions()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }


    //Amelie Talk
    fun populateAmelieTalk() {

        createOption("How did you become a fleet commander?") {
            clearOptions()

            textPanel.addPara("\"It's not that interesting of a story. Most of us are forced to be enrolled in the resource or patrol department after coming of age. I didn't want to be locked into grunt work, so I've gone with the patrol department. " +
                    "But even there, after my initial period of service was over, I already had enough of it and looked towards more rewarding positions, so I've joined the station's academy and pretty much just progressed further from there.")

            textPanel.addPara("Can't say I look back to the enrollment fondly, yet it likely shaped my view of everything.\"")

            addBackOptionForAmelieTalk()

        }

        createOption("About Xander") {
            clearOptions()

            textPanel.addPara("\"Oh, him? I honestly can't say we know much about each other, though he probably does know a lot more about me, " +
                    "with his occupation and all, but nonetheless I think well of him. He does not share the same aspiration as myself, at least not to the same extent - but he does his job well despite that. ")

            textPanel.addPara("It is calming to not have to worry about a cooperator stabbing your back to reach their own goals.\" she says with an exhausted expression.")

            addBackOptionForAmelieTalk()
        }


        createOption("Back") {
            clearOptions()
            populateAmelieDialog()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }




    fun addBackOptionForAmelieTalk() {
        createOption("Back") {
            clearOptions()
            populateAmelieTalk()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    //Donation Mechanic
    fun donateItems() {
        var tradeCargo = Global.getFactory().createCargo(true)
        var playerCargo = Global.getSector().playerFleet.cargo



        for (stack in playerCargo.stacksCopy) {
            if (!isRareTech(stack)) continue
            tradeCargo.addFromStack(stack)
        }
        tradeCargo.sort()

        var width = 310f

        dialog.showCargoPickerDialog("Select Tech to turn in", "Confirm", "Cancel", true, width, tradeCargo, object :
            CargoPickerListener {
            override fun pickedCargo(cargo: CargoAPI) {
                if (cargo.isEmpty) return



                //Remove from player inventory
                cargo.sort()
                for (stack in cargo.stacksCopy) {
                    playerCargo.removeItems(stack.type, stack.data, stack.size)
                    if (stack.isCommodityStack) {
                        AddRemoveCommodity.addCommodityLossText(stack.commodityId, stack.size.toInt(), textPanel)
                    }
                    if (stack.isSpecialStack) {
                        AddRemoveCommodity.addItemLossText(stack.specialDataIfSpecial, stack.size.toInt(), textPanel)
                    }
                }

                val influence = computeValue(cargo)
                Misc.adjustRep(data.amelie, (influence * 0.50f) * 0.001f, textPanel)


                if (influence > 0) {
                    DonatedItemFactor(influence.toInt(), dialog)
                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1.0f, 1.0f)
                }

                clearOptions()
                populateAmelieDialog()
            }

            override fun cancelledCargoSelection() {

            }

            override fun recreateTextPanel(element: TooltipMakerAPI?, cargo: CargoAPI?, pickedUp: CargoStackAPI?, pickedUpFromSource: Boolean, combined: CargoAPI) {
                val influence = computeValue(combined)

                val pad = 3f
                val opad = 10f

                element!!.setParaFontOrbitron()
                element.addPara(Misc.ucFirst("${interactionTarget.faction.displayName} Trade"), interactionTarget.faction.getBaseUIColor(), 1f)
                element.setParaFontDefault()
                element.addImage(interactionTarget.faction.logo, width * 1f, 0f)
                element.addSpacer(10f)
                element.addPara("Trade in rare tech to improve Amelie's standing within the faction. Different types of items have different values of importance, and thus some are less effective to trade in. Those values are: ",0f)
                element.addSpacer(10f)
                element.beginGridFlipped(width, 1, 40f, 10f)
                element.addToGrid(0, 0, "Colony Equipment", "${(data.colonyEquipmentPriceMod * 100).toInt()}%")
                element.addToGrid(0, 1, "Blueprints", "${(data.blueprintPriceMod * 100).toInt()}%")
                element.addToGrid(0, 2, "AI Cores", "${(data.aiCorePriceMod * 100).toInt()}%")
                element.addToGrid(0, 3, "Alteration", "${(data.alterationPriceMod * 100).toInt()}%")
                element.addGrid(pad)

                element.addSpacer(10f)

                var influenceString = influence.toInt().toString()
                var maxInfluenceString = data.maximumInfluenceRequired.toInt().toString()

                element.addPara("All trades have a conversion rate of 1000 to 1. If you turn in the selected items, Amelie's standing will improve by a value of $influenceString. The maximum required influence is $maxInfluenceString.", 0f,
                    Misc.getTextColor(), Misc.getHighlightColor(), "1000 to 1", "$influenceString", "${maxInfluenceString}")

                element.addSpacer(10f)

                element.addPara("A single item can not provide more than ${data.influenceCapPeritem.toInt()} influence at once.",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "${data.influenceCapPeritem.toInt()}")

            }

        })
    }

    fun isRareTech(stack: CargoStackAPI) : Boolean {

        if (stack.isCommodityStack) {
            var spec = stack.resourceIfResource
            if (spec.hasTag("mission_item")) return false

            if (spec.hasTag("ai_core") || spec.demandClass == "ai_cores") return true
        }

        if (stack.isSpecialStack) {
            var spec = stack.specialItemSpecIfSpecial
            var plugin = stack.plugin
            if (spec.hasTag("mission_item")) return false

            if (spec.hasTag("mission_item")) return false

            if (spec.id == "rat_alteration_install") return true

            if (plugin is MultiBlueprintItemPlugin || plugin is ShipBlueprintItemPlugin || plugin is WeaponBlueprintItemPlugin
                || plugin is FighterBlueprintItemPlugin ||plugin is IndustryBlueprintItemPlugin) return true

            if (ItemEffectsRepo.ITEM_EFFECTS.contains(spec.id)) return true

        }

        return false
    }

    fun computeValue(cargo: CargoAPI): Float {

        var value = 0f
        var cap = data.influenceCapPeritem * 1000f

        for (stack in cargo.stacksCopy) {
            if (stack.isCommodityStack) {
                var spec = stack.resourceIfResource

                if (spec.hasTag("ai_core") || spec.demandClass == "ai_cores") {
                    value += (spec.basePrice * data.aiCorePriceMod).coerceAtMost(cap) * stack.size
                    continue
                }
            }

            if (stack.isSpecialStack) {
                var spec = stack.specialItemSpecIfSpecial
                var plugin = stack.plugin

                if (spec.id == "rat_alteration_install") {
                    value += (getSpecialItemPrice(stack) * data.alterationPriceMod).coerceAtMost(cap) * stack.size
                }

                if (plugin is MultiBlueprintItemPlugin || plugin is ShipBlueprintItemPlugin || plugin is WeaponBlueprintItemPlugin
                    || plugin is FighterBlueprintItemPlugin ||plugin is IndustryBlueprintItemPlugin) {
                    value += (getSpecialItemPrice(stack)* data.blueprintPriceMod).coerceAtMost(cap) * stack.size
                    continue
                }

                if (ItemEffectsRepo.ITEM_EFFECTS.contains(spec.id)) {
                    value += (getSpecialItemPrice(stack) * data.colonyEquipmentPriceMod).coerceAtMost(cap) * stack.size
                    continue
                }
            }
        }

        return value * data.conversionRatio
    }

    fun getSpecialItemPrice(stack: CargoStackAPI) : Float {
        var spec = stack.specialItemSpecIfSpecial ?: return 0f
        var plugin = stack.plugin
        try {
            var value = plugin.getPrice(interactionTarget.market, null).toFloat()
            return value
        } catch (e: Throwable) {
            return spec.basePrice
        }
    }

}