package assortment_of_things.campaign.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.IndustrySpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ConsumeableIndustryBP : BaseSpecialItemPlugin() {

    var industry: IndustrySpecAPI? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)
        industry = Global.getSettings().getIndustrySpec(stack.specialDataIfSpecial.data)
        if (industry == null) {
            industry = Global.getSettings().getIndustrySpec("rat_expedition_hub")

        }
    }

    override fun render(x: Float, y: Float, w: Float, h: Float,  alphaMult: Float, glowMult: Float, renderer: SpecialItemPlugin.SpecialItemRendererAPI?) {
        super.render(x, y, w, h, alphaMult, glowMult, renderer)

        val cx = x + w / 2f
        val cy = y + h / 2f

        val blX = cx - 25f
        val blY = cy - 14f
        val tlX = cx - 30f
        val tlY = cy + 16f
        val trX = cx + 24f
        val trY = cy + 22f
        val brX = cx + 30f
        val brY = cy - 6f


        val sprite: SpriteAPI = Global.getSettings().getSprite(industry!!.getImageName())
        val mult = 1f

        sprite.alphaMult = alphaMult * mult
        sprite.setNormalBlend()
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)

        if (glowMult > 0) {
            sprite.alphaMult = alphaMult * glowMult * 0.5f * mult
            sprite.setAdditiveBlend()
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)
        }

        renderer!!.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false)
    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        if (industry != null) {
            val base = super.getPrice(market, submarket).toFloat()
            return (base + industry!!.cost * itemPriceMult).toInt()
        }
        return super.getPrice(market, submarket)
    }

    override fun getName(): String? {
        return if (industry != null) {
            industry!!.name + " Forge-Assisted Blueprint"
        } else super.getName()
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 10f
        val small = 5f
        val h = Misc.getHighlightColor()
        val g = Misc.getGrayColor()
        var b = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()
        val industryId = stack.specialDataIfSpecial.data
        val known = Global.getSector().playerFaction.knowsIndustry(industryId)

        tooltip.addSpacer(10f)
        tooltip.addPara("Enables the construction of a ${industry!!.name}, to match the performance specified the provided specialised nanoforge is required to construct and maintain it. Due to this only one such structure can be constructed. The nanoforge is reuseable if the structure were to be demolished.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "${industry!!.name}" ,"one", "reuseable")
        tooltip.addSpacer(10f)

        tooltip.addSectionHeading("${industry!!.name}", Alignment.MID, 0f)

        tooltip.addPara(industry!!.desc, opad)
        addCostLabel(tooltip, opad, transferHandler, stackSource)
    }

}