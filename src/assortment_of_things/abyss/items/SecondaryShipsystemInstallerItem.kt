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
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.getCurrSpecialMods
import java.awt.Color

class SecondaryShipsystemInstallerItem : BaseSpecialItemPlugin() {

    var hullmodSpec: HullModSpecAPI? = null
    var hullmod: BaseHullMod? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)

        var data = stack.specialDataIfSpecial.data

        var mods = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") }

        if (data == null || !mods.map { it.id }.contains(data) )
        {
            var mod = mods.random().id
            stack.specialDataIfSpecial.data = mod
            data = mod
        }
        hullmodSpec = Global.getSettings().getHullModSpec(data)

        var loader =  Global.getSettings().scriptClassLoader
        var claz = loader.loadClass(hullmodSpec!!.effectClass)
        hullmod = claz.newInstance() as BaseHullMod
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
        val sprite = Global.getSettings().getSprite(hullmodSpec!!.spriteName)
        val known = Global.getSector().playerPerson.stats.hasSkill(hullmodSpec!!.id)
        val mult = 1f
        sprite.alphaMult = alphaMult * mult
        sprite.setNormalBlend()
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        if (glowMult > 0) {
            sprite.alphaMult = alphaMult * glowMult * 0.5f * mult
            sprite.setAdditiveBlend()
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        }
        if (known) {
            renderer.renderBGWithCorners(Color.black,
                blX,
                blY,
                tlX,
                tlY,
                trX,
                trY,
                brX,
                brY,
                alphaMult * 0.5f,
                0f,
                false)
        }
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
        return if (hullmodSpec != null) {
           hullmodSpec!!.displayName + " - Hull Alteration"
        } else super.getName()
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

        hullmod!!.addPostDescriptionSection(tooltip, ShipAPI.HullSize.FRIGATE, null, tooltipWidth, false)
        tooltip.addSpacer(5f)

        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?
        if (marketListener != null && !marketListener.atMarket)
        {
            tooltip.addPara("Can only be installed while docked at a colony", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
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

                    if (choice.variant.source != VariantSource.REFIT)
                    {
                        var variant = choice.variant.clone();
                        variant.originalVariant = null;
                        variant.hullVariantId = Misc.genUID()
                        variant.source = VariantSource.REFIT
                        choice.setVariant(variant, false, true)
                    }
                    choice.variant.addPermaMod(hullmodSpec!!.id, true)
                    choice.updateStats()

                    Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
                    Global.getSector().campaignUI.messageDisplay.addMessage("Installed ${hullmodSpec!!.displayName} in to ${choice.hullSpec.hullName}")

                    var cargo = Global.getSector().playerFleet.cargo

                }
                else
                {
                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", hullmodSpec!!.id), 1f)
                }
            }

            override fun cancelledFleetMemberPicking() {
                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", hullmodSpec!!.id), 1f)
            }

        }
        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?

        if (marketListener != null)
        {
            if (marketListener.atMarket)
            {
                if (Global.getSector().campaignUI.currentInteractionDialog == null) return

                var choices = Global.getSector().playerFleet.fleetData.membersListCopy.filter { it.variant.hullMods.none { Global.getSettings().getHullModSpec(it).hasTag("rat_alteration") } }

                var maxSmods = Global.getSettings().settingsJSON.get("maxPermanentHullmods") as Int
                choices = choices.filter { it.variant.sMods.size < it.stats.dynamic.getValue(Stats.MAX_PERMANENT_HULLMODS_MOD, maxSmods.toFloat())}

                Global.getSector().campaignUI.currentInteractionDialog.showFleetMemberPickerDialog("Choose a ship", "Confirm", "Cancel", 10, 10, 64f,
                    true, false, choices, listener)

                return
            }
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", hullmodSpec!!.id), 1f)
    }



}