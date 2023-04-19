package assortment_of_things.campaign.items

import assortment_of_things.campaign.skills.PocketDimensionSkill
import assortment_of_things.campaign.skills.QualityOverQuantitySkill
import assortment_of_things.campaign.skills.TemporalAssaultSkill
import assortment_of_things.campaign.skills.util.SkillManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.characters.CustomSkillDescription
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SkillProviderItem : BaseSpecialItemPlugin() {

    var skillSpec: SkillSpecAPI? = null
    var skillDescription: CustomSkillDescription? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)
        skillSpec = Global.getSettings().getSkillSpec(stack.specialDataIfSpecial.data)

        skillDescription = when(skillSpec!!.id)
        {
            "rat_qoq" -> QualityOverQuantitySkill()
            "rat_temp_assault" -> TemporalAssaultSkill()
            "rat_pocket" -> PocketDimensionSkill()
            else -> null
        }
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
        val sprite = Global.getSettings().getSprite(skillSpec!!.spriteName)
        val known = Global.getSector().playerPerson.stats.hasSkill(skillSpec!!.id)
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
        if (skillSpec != null) {
            val base = super.getPrice(market, submarket).toFloat()
            return (base * itemPriceMult).toInt()
        }
        return super.getPrice(market, submarket)
    }

    override fun getName(): String? {
        return if (skillSpec != null) {
            skillSpec!!.name + ""
        } else super.getName()
    }

    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()

        skillSpec
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
        val industryId = stack.specialDataIfSpecial.data
        val known = Global.getSector().playerPerson.stats.hasSkill(skillSpec!!.id)

        tooltip.addSectionHeading("Info", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Upon rightclicking, the training data within this chip is used to permanently learn a new skill. This skill can not be re-assigned after learning it.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "rightclicking", "training data", "permanently", "can not","re-assigned")

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("Skill: ${skillSpec!!.name}", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        skillDescription!!.createCustomDescription(null, skillSpec, tooltip, tooltipWidth)

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("Other Data", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        addCostLabel(tooltip, opad, transferHandler, stackSource)
        if (known) {
            tooltip.addPara("Already known", g, opad)
        }/* else {
            tooltip.addPara("Right-click to permanently learn this skill", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }*/
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return !Global.getSector().playerPerson.stats.hasSkill(skillSpec!!.id)
    }

    override fun performRightClickAction() {
        var stats = Global.getSector().playerPerson.stats

        if (stats.hasSkill(skillSpec!!.id))
        {
            Global.getSector().campaignUI.messageDisplay.addMessage("" + skillSpec!!.name + ": skill already known")
        }
        else
        {

            Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
            stats.setSkillLevel(skillSpec!!.id, 1f)
            Global.getSector().campaignUI.messageDisplay.addMessage("Acquired skill: " + skillSpec!!.name + "")

            Global.getSector().playerPerson.memoryWithoutUpdate.set("\$rat_learned_${skillSpec!!.id}", true)

            SkillManager.update()
        }
    }
}