package assortment_of_things.abyss.items

import assortment_of_things.scripts.AtMarketListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SecondaryShipsystemRemoverItem : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }

    override fun render(x: Float, y: Float, w: Float, h: Float, alphaMult: Float, glowMult: Float, renderer: SpecialItemRendererAPI) {
        val cx = x + w / 2f
        val cy = y + h / 2f
        val blX = cx - 30f
        val blY = cy - 15f
        val tlX = cx - 20f
        val tlY = cy + 26f
        val trX = cx + 23f
        val trY = cy + 26f
        val brX = cx + 15f
        val brY = cy - 18f
        Global.getSettings().loadTexture("graphics/icons/abilities/clear.png")
        val sprite = Global.getSettings().getSprite("graphics/icons/abilities/clear.png")
        val mult = 1f
        sprite.alphaMult = alphaMult * mult
        sprite.setNormalBlend()
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        if (glowMult > 0) {
            sprite.alphaMult = alphaMult * glowMult * 0.5f * mult
            sprite.setAdditiveBlend()
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        }
        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false)

    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 30000
    }

    override fun getName(): String? {
        return "Alteration Detacher"
    }

    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()
    }
    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 10f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)
        tooltip.addPara("Can be used to remove a hull alteration from a ship. The removed alteration is destroyed and is not recovered in the process.", 0f, Misc.getTextColor(), Misc.getHighlightColor())

        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?
        if (marketListener != null && !marketListener.atMarket)
        {
            tooltip.addSpacer(5f)
            tooltip.addPara("Can only be used while docked at a colony", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        var stats = Global.getSector().playerPerson.stats

        var listener = object : FleetMemberPickerListener {
            override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                if (!members.isNullOrEmpty())
                {
                    var choice = members.get(0)


                    var mods = choice.variant.hullMods.map { Global.getSettings().getHullModSpec(it) }.filter { it.hasTag("rat_alteration") }

                    for (mod in mods)
                    {
                        choice.variant.removeMod(mod!!.id)
                        choice.variant.removePermaMod(mod!!.id)

                        Global.getSector().campaignUI.messageDisplay.addMessage("Removed ${mod!!.displayName} from ${choice.hullSpec.hullName}")

                    }


                    Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)

                    var cargo = Global.getSector().playerFleet.cargo
                   // stack.subtract(1f)


                   /* if (stack.cargoSpace == 0f)
                    {
                        Global.getSector().playerFleet.cargo.removeStack(stack)
                    }*/
                }
                else
                {
                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_remover", null), 1f)
                }
            }

            override fun cancelledFleetMemberPicking() {
                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_remover", null), 1f)
            }

        }
        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?

        if (marketListener != null)
        {
            if (marketListener.atMarket)
            {
                if (Global.getSector().campaignUI.currentInteractionDialog == null) return

                var choices = Global.getSector().playerFleet.fleetData.membersListCopy.filter { it.variant.hullMods.any { Global.getSettings().getHullModSpec(it).hasTag("rat_alteration") } }

                Global.getSector().campaignUI.currentInteractionDialog.showFleetMemberPickerDialog("Choose a ship", "Confirm", "Cancel", 10, 10, 64f,
                    true, false, choices, listener)

                return
            }
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_remover", null), 1f)
    }
}