package assortment_of_things.campaign.items

import assortment_of_things.campaign.skills.PocketDimensionSkill
import assortment_of_things.campaign.skills.QualityOverQuantitySkill
import assortment_of_things.campaign.skills.TemporalAssaultSkill
import assortment_of_things.campaign.skills.util.SkillManager
import assortment_of_things.scripts.AtMarketListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SecondaryShipsystemInstallerItem : BaseSpecialItemPlugin() {

    var hullmodSpec: HullModSpecAPI? = null
    var hullmod: BaseHullMod? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)
        hullmodSpec = Global.getSettings().getHullModSpec(stack.specialDataIfSpecial.data)
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
            return (base * itemPriceMult).toInt()
        }
        return super.getPrice(market, submarket)
    }

    override fun getName(): String? {
        return if (hullmodSpec != null) {
            hullmodSpec!!.displayName + " Installer"
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

        tooltip.addSectionHeading("Info", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Upon rightclicking, the training data within this chip is used to permanently learn a new skill. This skill can not be re-assigned after learning it.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "rightclicking", "training data", "permanently", "can not","re-assigned")

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("Skill: ${hullmodSpec!!.displayName}", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        hullmod!!.addPostDescriptionSection(tooltip, ShipAPI.HullSize.FRIGATE, null, tooltipWidth, false)

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("Other Data", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        addCostLabel(tooltip, opad, transferHandler, stackSource)
        /* else {
            tooltip.addPara("Right-click to permanently learn this skill", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }*/
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }

    override fun performRightClickAction() {
        var stats = Global.getSector().playerPerson.stats

        var listener = object : FleetMemberPickerListener {
            override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                if (!members.isNullOrEmpty())
                {
                    var choice = members.get(0)

                    choice.variant.addMod(hullmodSpec!!.id)
                    choice.variant.addPermaMod(hullmodSpec!!.id)

                    Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
                    Global.getSector().campaignUI.messageDisplay.addMessage("Installed ${spec.name} in to ${choice.hullSpec.hullName}")

                    stack.subtract(1f)

                    Global.getSector().playerFleet.cargo.removeStack(stack)
                }
            }

            override fun cancelledFleetMemberPicking() {

            }

        }

        var marketListener = Global.getSector().allListeners.find { it::class.java == AtMarketListener::class.java } as AtMarketListener?

        if (marketListener != null)
        {
            if (marketListener.atMarket)
            {
                if (Global.getSector().campaignUI.currentInteractionDialog == null) return

                var choices = Global.getSector().playerFleet.fleetData.membersListCopy.filter { it.variant.hullMods.none { Global.getSettings().getHullModSpec(it).manufacturer == "Augmenter" } }

                Global.getSector().campaignUI.currentInteractionDialog.showFleetMemberPickerDialog("Test", "Confirm", "Cancel", 10, 10, 64f,
                    true, false, choices, listener)


            }
        }

        /*if (stats.hasSkill(hullmodSpec!!.id))
        {
            Global.getSector().campaignUI.messageDisplay.addMessage("" + hullmodSpec!!.displayName + ": skill already known")
        }
        else
        {

            Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
            stats.setSkillLevel(hullmodSpec!!.id, 1f)
            Global.getSector().campaignUI.messageDisplay.addMessage("Acquired skill: " + hullmodSpec!!.name + "")

            Global.getSector().playerPerson.memoryWithoutUpdate.set("\$rat_learned_${hullmodSpec!!.id}", true)

            SkillManager.update()
        }*/
    }
}