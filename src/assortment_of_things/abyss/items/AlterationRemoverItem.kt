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

class AlterationRemoverItem : BaseSpecialItemPlugin() {


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


        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return false
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }

}