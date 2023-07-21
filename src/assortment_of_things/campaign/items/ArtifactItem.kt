package assortment_of_things.campaign.items

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.artifacts.ArtifactIntel
import assortment_of_things.artifacts.ArtifactSpec
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin.SpecialItemRendererAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ArtifactItem : BaseSpecialItemPlugin() {

    var plugin: BaseArtifactPlugin? = null
    var artifactSpec: ArtifactSpec? = null

    override fun init(stack: CargoStackAPI) {
        super.init(stack)

        var data = stack.specialDataIfSpecial.data

        artifactSpec = ArtifactUtils.artifacts.find { data == it.id }
        if (artifactSpec == null)
        {
            artifactSpec = ArtifactUtils.artifacts.first()
        }

        plugin = ArtifactUtils.getPlugin(artifactSpec!!)
    }

    override fun render(x: Float, y: Float, w: Float, h: Float, alphaMult: Float, glowMult: Float, renderer: SpecialItemRendererAPI) {

        val sprite = Global.getSettings().getSprite(artifactSpec!!.spritePath)
        val mult = 1f
        sprite.alphaMult = alphaMult * mult
        sprite.setNormalBlend()
        sprite.renderAtCenter(x + w / 2, y + h / 2)
        if (glowMult > 0)
        {
            sprite.alphaMult = alphaMult * glowMult * 0.5f * mult
            sprite.setAdditiveBlend()
            sprite.renderAtCenter(x + w / 2, y + h / 2)
        }
    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {

        return super.getPrice(market, submarket)
    }

    override fun getName(): String? {
        return if (artifactSpec != null) {
           artifactSpec!!.name + ""
        } else super.getName()
    }


    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()
    }

    /*fun addTitleTooltip(tooltip: TooltipMakerAPI?,  expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
       *//* var useGray = true
        if (javaClass == BaseSpecialItemPlugin::class.java) {
            useGray = false
        }
        createTooltip(tooltip, expanded, transferHandler, stackSource, useGray)*//*

        val opad = 10f

       // tooltip!!.addTitle(name)
        tooltip!!.addPara("Artifact: ${artifactSpec!!.name}", 0f, Misc.getBasePlayerColor(), AbyssUtils.ABYSS_COLOR)
        val design = designType
        Misc.addDesignTypePara(tooltip, design, opad)

        if (!spec.desc.isEmpty()) {
            var c = Misc.getGrayColor()
            tooltip.addPara(spec.desc, c, opad)
        }

        if (javaClass == BaseSpecialItemPlugin::class.java) {
            addCostLabel(tooltip, opad, transferHandler, stackSource)
        }
    }*/

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
       // addTitleTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 5f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)

        plugin!!.addDescription(tooltip)

        tooltip.addSpacer(5f)

        var known = ArtifactUtils.getArtifactsInFleet().any { it.id == artifactSpec!!.id }
        if (known)
        {
            tooltip.addPara("This type of artifact has already been added to the fleet.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else
        {
            tooltip.addPara("Rightclick to add to fleet.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        }


        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return !ArtifactUtils.getArtifactsInFleet().any { it.id == artifactSpec!!.id }
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        var stats = Global.getSector().playerPerson.stats

        Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)

        ArtifactUtils.addArtifactToFleet(artifactSpec!!)
        Global.getSector().campaignUI.messageDisplay.addMessage("Added ${artifactSpec!!.name} to the fleet.")

        if (!Global.getSector().intelManager.hasIntelOfClass(ArtifactIntel::class.java))
        {
            Global.getSector().intelManager.addIntel(ArtifactIntel())
        }

    }
}