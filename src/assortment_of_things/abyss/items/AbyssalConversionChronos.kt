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
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class AbyssalConversionChronos : BaseSpecialItemPlugin() {

    var hullmodSpec: HullModSpecAPI? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)

        hullmodSpec = Global.getSettings().getHullModSpec("rat_chronos_conversion")
    }

    override fun render(x: Float, y: Float, w: Float,  h: Float, alphaMult: Float, glowMult: Float, renderer: SpecialItemRendererAPI) {
        var x = x
        var y = y
        var w = w
        var h = h
        var cx = x + w / 2f
        var cy = y + h / 2f
        cx -= 2f
        cy -= 1f
        x = x.toInt().toFloat()
        y = y.toInt().toFloat()
        w = w.toInt().toFloat()
        h = h.toInt().toFloat()
        var mult = 1f

        val sprite: SpriteAPI = Global.getSettings().getSprite(hullmodSpec!!.getSpriteName())
        w = sprite.width * 1.5f + 5
        h = sprite.height * 1.5f
        //		x = cx - w / 2f;
//		y = cy - h / 2f;
        x = cx
        y = cy

        val blX = -23f
        val blY = -10f
        val tlX = -18f
        val tlY = 24f
        val trX = 24f
        val trY = 24f
        val brX = 23f
        val brY = -10f

        sprite.alphaMult = alphaMult * mult
        sprite.setNormalBlend()
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        if (glowMult > 0) {
            sprite.alphaMult = alphaMult * glowMult * 0.5f * mult
            //sprite.renderAtCenter(cx, cy);
            sprite.setAdditiveBlend()
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        }

        //renderer.renderScanlines(sprite, cx, cy, alphaMult);
        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false)
    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        if (hullmodSpec != null) {
            val base = super.getPrice(market, submarket).toFloat()
            return (base * 1f).toInt()
        }
        return super.getPrice(market, submarket)
    }

    override fun getName(): String? {
         return spec.name
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
        tooltip.addPara("Converts an automated abyssal hull into one that is merely AI assisted, with a specialised bridge for a small crew of humans. This is not applicable if the ship has a permanently integrated AI core.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "automated abyssal hull", "bridge")
        tooltip.addSpacer(5f)

        tooltip.addPara("This specific conversion integrates a chronos core into the hull, allowing the usage of the shipsystem related to that core.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "chronos core")
        tooltip.addSpacer(5f)

        tooltip.addPara("This change is permanent and can not be reverted.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
        tooltip.addSpacer(5f)

        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?
        if (marketListener != null && !marketListener.atMarket)
        {
            tooltip.addPara("Can only be used while docked at a colony", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else
        {
            tooltip.addPara("Rightclick to use.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
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

        Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)

        var listener = object : FleetMemberPickerListener {
            override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                if (!members.isNullOrEmpty())
                {
                    var choice = members.get(0)

                    if (choice.variant.source != VariantSource.REFIT)
                    {
                        var variant = choice.variant.clone();
                        variant.originalVariant = null;
                        variant.hullVariantId = Misc.genUID()
                        variant.source = VariantSource.REFIT
                        choice.setVariant(variant, false, true)
                    }
                    choice.variant.addPermaMod(hullmodSpec!!.id)
                    choice.updateStats()

                    Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
                    Global.getSector().campaignUI.messageDisplay.addMessage("Installed ${hullmodSpec!!.displayName} in to ${choice.hullSpec.hullName}")

                    var cargo = Global.getSector().playerFleet.cargo

                }
                else
                {
                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_chronos_integration", null), 1f)
                }
            }

            override fun cancelledFleetMemberPicking() {
                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_chronos_integration", null), 1f)
            }

        }
        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?

        if (marketListener != null)
        {
            if (marketListener.atMarket)
            {
                if (Global.getSector().campaignUI.currentInteractionDialog == null) return

                var choices = Global.getSector().playerFleet.fleetData.membersListCopy
                choices = choices.filter { it.hullSpec.hasTag("rat_abyssals")}
                choices = choices.filter { it.variant.hasHullMod(HullMods.AUTOMATED) }
                choices = choices.filter { !Misc.isUnremovable(it.captain) }

                Global.getSector().campaignUI.currentInteractionDialog.showFleetMemberPickerDialog("Choose a ship", "Confirm", "Cancel", 10, 10, 64f,
                    true, false, choices, listener)

                return
            }
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_chronos_integration", null), 1f)
    }



}