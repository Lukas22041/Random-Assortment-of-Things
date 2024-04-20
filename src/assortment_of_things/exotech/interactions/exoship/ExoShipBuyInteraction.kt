package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoData
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.ExoshipIntel
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.campaign.impl.items.*
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.LunaCommons

class ExoShipBuyInteraction(var exoDialog: ExoshipInteractions, var data: ExoData) {

    var faction = exoDialog.faction

    var shipData = ExoUtils.getExoshipData(exoDialog.interactionTarget)


    companion object {
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

        fun unlockExoIntel(textPanel: TextPanelAPI?, important: Boolean) {
            for (exoship in ExoUtils.getExoData().exoships) {
                var intel = ExoshipIntel(exoship)
                Global.getSector().intelManager.addIntel(intel)

                if (important) {
                    intel.isImportant = true
                }

                if (textPanel != null) {
                    Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)
                }
            }
        }
    }

    fun buyTech() {

        var tradeCargo = Global.getFactory().createCargo(true)
        var stationCargo = shipData.cargo
        var playerCargo = Global.getSector().playerFleet.cargo

        stationCargo.sort()

        if (!data.hasPartnership) {
            tradeCargo.addSpecial(SpecialItemData("rat_exo_deed", ""), 1f)
        }

        for (stack in stationCargo.stacksCopy) {
            tradeCargo.addFromStack(stack)
        }




        var width = 310f

        exoDialog.dialog.showCargoPickerDialog("Purchase Items", "Confirm", "Cancel", true, width, tradeCargo, object :
            CargoPickerListener {
            override fun pickedCargo(cargo: CargoAPI) {
                if (cargo.isEmpty) return


                val tokens = computeValue(cargo)

                if (tokens > data.tokens && tokens != 0f) {
                    exoDialog.textPanel.addPara("Not enough tokens to finish transaction.", Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
                    return
                }

                var hasDeed = false

                //Remove from trader inventory
                cargo.sort()
                for (stack in cargo.stacksCopy) {
                    stationCargo.removeItems(stack.type, stack.data, stack.size)
                    if (stack.isCommodityStack) {
                        AddRemoveCommodity.addCommodityGainText(stack.commodityId, stack.size.toInt(), exoDialog.textPanel)
                    }
                    if (stack.isSpecialStack) {
                        AddRemoveCommodity.addItemGainText(stack.specialDataIfSpecial, stack.size.toInt(), exoDialog.textPanel)
                    }

                    if (stack.isSpecialStack && stack.specialItemSpecIfSpecial.id == "rat_exo_deed") {
                        hasDeed = true
                    }
                    else {
                        playerCargo.addFromStack(stack)
                    }
                }


                if (tokens > 0) {
                    data.tokens -= tokens
                    var tokenString = Misc.getWithDGS(tokens) + Strings.C

                    exoDialog.textPanel.setFontSmallInsignia()
                    exoDialog.textPanel.addPara("Lost $tokenString tokens", Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
                    exoDialog.textPanel.setFontInsignia()
                }


                Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1.0f, 1.0f)

                if (hasDeed) {

                    deedDialog()
                }
                else {
                    exoDialog.clearOptions()
                    exoDialog.populateOptions()
                }
            }

            override fun cancelledCargoSelection() {

            }

            override fun recreateTextPanel(element: TooltipMakerAPI?, cargo: CargoAPI?, pickedUp: CargoStackAPI?, pickedUpFromSource: Boolean, combined: CargoAPI) {
                val tokens = computeValue(combined)

                val pad = 3f
                val opad = 10f

                element!!.setParaFontOrbitron()
                element.addPara(Misc.ucFirst("${faction.displayName} Trade"), faction.getBaseUIColor(), 1f)
                element.setParaFontDefault()
                element.addImage(faction.logo, width * 1f, 0f)
                element.addSpacer(10f)
                element.addPara("Buy items with Exo-Tech tokens. These tokens can only be acquired through trade with the faction. All trades have a 100 to 1 credit conversion. ",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "100 to 1")
                element.addSpacer(10f)

                element.addPara("The faction is only willing to trade a small section of its sortiment with you, meaning that each exoships available sortiment wont ever restock.",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "wont ever restock")

                element.addSpacer(10f)

                var tokensString = Misc.getWithDGS(tokens) + Strings.C
                var currentTokensString = Misc.getWithDGS(data.tokens) + Strings.C
                element.addPara("You currently have ${currentTokensString} tokens. Confirming your selection would cost you $tokensString tokens.", 0f,
                    Misc.getTextColor(), Misc.getHighlightColor(), "${currentTokensString}", "$tokensString")

                if (tokens > data.tokens && tokens != 0f) {
                    element.addSpacer(10f)
                    element.addNegativePara("You do not have enough tokens. Confirming the transaction will cancel it.")
                }
            }
        })
    }

    fun deedDialog() {
        exoDialog.clearOptions()

        exoDialog.visualPanel.showPersonInfo(data.commPerson)

        exoDialog.textPanel.addPara("\"It appears you are worth keeping around afterall\", says ${data.commPerson.nameString} after both of you finish your transaction. " +
                "\"I'l be interested in seeing what you may come to offer to us in the future. In the meantime we will make sure to keep our offers of interest to you aswell.")

        exoDialog.textPanel.addPara("If you want to request certain Exo-Tech grade equipment for your fleet, contact me through the network as usual. " +
                "Do not expect to have it easier now though, all future trades will still require additional tokens.\"")

        /*for (exoship in data.exoships) {
            var intel = ExoshipIntel(exoship)
            Global.getSector().intelManager.addIntel(intel)
            Global.getSector().intelManager.addIntelToTextPanel(intel, exoDialog.textPanel)
        }*/
        unlockExoIntel(exoDialog.textPanel, false)

        //Unlock Background
        LunaCommons.set("assortment_of_things", "rat_exo_start", true)

        exoDialog.createOption("Back") {
            exoDialog.clearOptions()
            exoDialog.populateOptions()
        }

        data.hasPartnership = true
    }



    fun computeValue(cargo: CargoAPI): Float {

        var value = 0f

        for (stack in cargo.stacksCopy) {
            if (stack.isCommodityStack) {
                var spec = stack.resourceIfResource

                value += spec.basePrice * stack.size
            }

            if (stack.isSpecialStack) {
                var spec = stack.specialItemSpecIfSpecial
                var plugin = stack.plugin

                value += getSpecialItemPrice(stack) * stack.size
            }
        }

        return value * ExoshipInteractions.tokenConversionRatio
    }

    fun getSpecialItemPrice(stack: CargoStackAPI) : Float {
        var spec = stack.specialItemSpecIfSpecial ?: return 0f
        var plugin = stack.plugin
        try {
            var value = plugin.getPrice(exoDialog.interactionTarget.market, null).toFloat()
            return value
        } catch (e: Throwable) {
            return spec.basePrice
        }
    }

}