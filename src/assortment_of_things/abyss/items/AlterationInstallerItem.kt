package assortment_of_things.abyss.items

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import java.awt.Color
import java.util.*

class AlterationInstallerItem : BaseSpecialItemPlugin() {

    var hullmodSpec: HullModSpecAPI? = null
    var hullmod: BaseAlteration? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)

        var data = stack.specialDataIfSpecial.data

        var allAlterations =  Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") }


        if (data != null && !allAlterations.map { it.id }.contains(data) ) {
            var alterationsWithTag = allAlterations.filter { it.hasTag(data) }

            var key = "\$rat_alteration_random"
            var random = Random(Misc.genRandomSeed())
            if (Global.getSector().memoryWithoutUpdate.contains(key))
            {
                random = Global.getSector().memoryWithoutUpdate.get(key) as Random
            }
            else
            {
                Global.getSector().memoryWithoutUpdate.set(key, random)
            }

            var modSelection = WeightedRandomPicker<HullModSpecAPI>()
            modSelection.random = random

            for (mod in alterationsWithTag)
            {
                modSelection.add(mod, mod.rarity)
            }

            var mod = modSelection.pick().id
            stack.specialDataIfSpecial.data = mod
            data = mod
        }

        if (data == null || !allAlterations.map { it.id }.contains(data) )
        {
            var key = "\$rat_alteration_random"
            var random = Random(Misc.genRandomSeed())
            if (Global.getSector().memoryWithoutUpdate.contains(key))
            {
                random = Global.getSector().memoryWithoutUpdate.get(key) as Random
            }
            else
            {
                Global.getSector().memoryWithoutUpdate.set(key, random)
            }

            var modSelection = WeightedRandomPicker<HullModSpecAPI>()
            modSelection.random = random

            var mods = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && !it.hasTag("rat alteration_no_drop") }

            for (mod in mods)
            {
                modSelection.add(mod, mod.rarity)
            }

            var mod = modSelection.pick().id
            stack.specialDataIfSpecial.data = mod
            data = mod
        }
        hullmodSpec = Global.getSettings().getHullModSpec(data)

        var loader =  Global.getSettings().scriptClassLoader
        var claz = loader.loadClass(hullmodSpec!!.effectClass)
        hullmod = claz.newInstance() as BaseAlteration
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
            return hullmodSpec!!.baseValue.toInt()
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
        val opad = 5f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)
        hullmod!!.addPostDescriptionSection(tooltip, ShipAPI.HullSize.FRIGATE, null, tooltipWidth, false)
        tooltip.addSpacer(5f)

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun createTooltip(tooltip: TooltipMakerAPI,  expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?, useGray: Boolean) {
        val opad = 10f
        tooltip.addTitle(name)
        val design = hullmodSpec!!.manufacturer
        Misc.addDesignTypePara(tooltip, design, opad)

        var c = Misc.getTextColor()
        if (useGray) c = Misc.getGrayColor()
        tooltip.addPara("A hull alteration is a special, consumable hull modification that can be installed through the \"Additional Options\" section at the top of the refit screen.",
            opad, c, Misc.getHighlightColor(), "\"Additional Options\"")

        if (javaClass == BaseSpecialItemPlugin::class.java) {
            addCostLabel(tooltip, opad, transferHandler, stackSource)
        }
    }

    override fun hasRightClickAction(): Boolean {
        return false
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }
}