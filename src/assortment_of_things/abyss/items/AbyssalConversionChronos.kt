package assortment_of_things.abyss.items

import assortment_of_things.misc.baseOrModSpec
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

        tooltip.addPara("Legacy Item that has been removed.", 0f)

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return false
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }




}