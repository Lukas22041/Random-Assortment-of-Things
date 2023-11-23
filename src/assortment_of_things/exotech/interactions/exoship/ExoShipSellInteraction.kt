package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoData
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.impl.items.*
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ExoShipSellInteraction(var exoDialog: ExoshipInteractions, var data: ExoData) {

    var faction = exoDialog.faction

    fun tradeInTech() {
        var tradeCargo = Global.getFactory().createCargo(true)
        var playerCargo = Global.getSector().playerFleet.cargo



        for (stack in playerCargo.stacksCopy) {
            if (!isRareTech(stack)) continue
            tradeCargo.addFromStack(stack)
        }
        tradeCargo.sort()

        var width = 310f

        exoDialog.dialog.showCargoPickerDialog("Select Tech to turn in", "Confirm", "Cancel", true, width, tradeCargo, object :
            CargoPickerListener {
            override fun pickedCargo(cargo: CargoAPI) {
                if (cargo.isEmpty) return

                //Remove from player inventory
                cargo.sort()
                for (stack in cargo.stacksCopy) {
                    playerCargo.removeItems(stack.type, stack.data, stack.size)
                    if (stack.isCommodityStack) {
                        AddRemoveCommodity.addCommodityLossText(stack.commodityId, stack.size.toInt(), exoDialog.textPanel)
                    }
                    if (stack.isSpecialStack) {
                        AddRemoveCommodity.addItemLossText(stack.specialDataIfSpecial, stack.size.toInt(), exoDialog.textPanel)
                    }
                }

                val tokens = computeValue(cargo)

                if (tokens > 0) {
                    data.tokens += tokens
                    var tokenString = Misc.getWithDGS(tokens) + Strings.C

                    exoDialog.textPanel.setFontSmallInsignia()
                    exoDialog.textPanel.addPara("Gained $tokenString tokens", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())
                    exoDialog.textPanel.setFontInsignia()
                }

                exoDialog.clearOptions()
                exoDialog.populateOptions()
                Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1.0f, 1.0f)
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
                element.addPara("Trade in rare tech to acquire Exo-Tech tokens. Those tokens can not be used anywhere else than at Exo-Tech facilities. Different types of tech have different conversions: ",0f)
                element.addSpacer(10f)
                element.beginGridFlipped(width, 1, 40f, 10f)
                element.addToGrid(0, 0, "Colony Equipment", "${(ExoshipInteractions.colonyEquipmentMod * 100).toInt()}%")
                element.addToGrid(0, 1, "Blueprints", "${(ExoshipInteractions.blueprintMod * 100).toInt()}%")
                element.addToGrid(0, 2, "AI Cores", "${(ExoshipInteractions.aiCoreMod * 100).toInt()}%")
                element.addToGrid(0, 3, "Alteration", "${(ExoshipInteractions.alterationMod * 100).toInt()}%")
                element.addGrid(pad)

                element.addSpacer(10f)

                var tokensString = Misc.getWithDGS(tokens) + Strings.C
                var currentTokensString = Misc.getWithDGS(data.tokens) + Strings.C
                element.addPara("All trades have a conversion rate of 100 to 1. If you turn in the selected items, you will receive $tokensString tokens. You currently have ${currentTokensString} tokens.", 0f,
                    Misc.getTextColor(), Misc.getHighlightColor(), "100 to 1", "$tokensString", "${currentTokensString}")
            }

        })
    }

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
    }

    fun computeValue(cargo: CargoAPI): Float {

        var value = 0f

        for (stack in cargo.stacksCopy) {
            if (stack.isCommodityStack) {
                var spec = stack.resourceIfResource

                if (spec.hasTag("ai_core") || spec.demandClass == "ai_cores") {
                    value += spec.basePrice * stack.size * ExoshipInteractions.aiCoreMod
                    continue
                }
            }

            if (stack.isSpecialStack) {
                var spec = stack.specialItemSpecIfSpecial
                var plugin = stack.plugin

                if (spec.id == "rat_alteration_install") {
                    value += getSpecialItemPrice(stack) * stack.size * ExoshipInteractions.alterationMod
                }


                if (plugin is MultiBlueprintItemPlugin || plugin is ShipBlueprintItemPlugin || plugin is WeaponBlueprintItemPlugin
                    || plugin is FighterBlueprintItemPlugin ||plugin is IndustryBlueprintItemPlugin) {
                    value += getSpecialItemPrice(stack) * stack.size * ExoshipInteractions.blueprintMod
                    continue
                }

                if (ItemEffectsRepo.ITEM_EFFECTS.contains(spec.id)) {
                    value += getSpecialItemPrice(stack) * stack.size * ExoshipInteractions.colonyEquipmentMod
                    continue
                }
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