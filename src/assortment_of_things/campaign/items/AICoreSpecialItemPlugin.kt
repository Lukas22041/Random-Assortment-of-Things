package assortment_of_things.campaign.items

import assortment_of_things.RATCampaignPlugin
import assortment_of_things.abyss.skills.PrimordialCoreSkill
import assortment_of_things.abyss.skills.SeraphCoreSkill
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import assortment_of_things.exotech.skills.ExoProcessorSkill
import assortment_of_things.relics.skills.HyperlinkSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*

class AICoreSpecialItemPlugin : BaseSpecialItemPlugin() {

    lateinit var commoditySpec: CommoditySpecAPI
    lateinit var plugin: AICoreOfficerPlugin

    companion object {
        var cores = mapOf(
            "rat_chronos_core" to "rat_core_time",
            "rat_cosmos_core" to "rat_core_space",
            "rat_seraph_core"  to "rat_core_seraph",
            "rat_primordial_core"  to "rat_core_primordial",
            "rat_neuro_core"  to "rat_hyperlink",
            "rat_exo_processor"  to "rat_exo_processor",
        )
    }

    override fun init(stack: CargoStackAPI) {
        super.init(stack)

        var data = stack.specialDataIfSpecial.data

        if (!cores.keys.contains(data)) {
            data = "rat_chronos_core"
            stack.specialDataIfSpecial.data = data
        }

        commoditySpec = Global.getSettings().getCommoditySpec(data)
        plugin = RATCampaignPlugin().pickAICoreOfficerPlugin(commoditySpec.id)!!.plugin

    }

    override fun render(x: Float, y: Float, w: Float, h: Float, alphaMult: Float, glowMult: Float, renderer: SpecialItemPlugin.SpecialItemRendererAPI?) {
        var centerX = x+w/2
        var centerY = y+h/2

        var sprite = Global.getSettings().getSprite(commoditySpec!!.iconName)
        sprite.setNormalBlend()
        sprite.alphaMult = alphaMult
        sprite.setSize(w - 20, h - 20)
        sprite.renderAtCenter(centerX, centerY)

        if (glowMult > 0) {
            sprite.setAdditiveBlend()
            sprite.alphaMult = alphaMult * glowMult * 0.5f
            sprite.setSize(w - 20, h - 20)
            sprite.renderAtCenter(centerX, centerY)
        }
    }

    override fun getName(): String {
        return commoditySpec.name
    }

    override fun getTooltipWidth(): Float {
        return 500f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        val opad = 10f

        var skillSpec = Global.getSettings().getSkillSpec(cores.get(commoditySpec.id))


        val design = designType
        Misc.addDesignTypePara(tooltip, design, opad)

        var corePerson = plugin.createPerson(commoditySpec.id, Factions.NEUTRAL, Random())
        var pointsMult = corePerson.memoryWithoutUpdate.getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT).toInt()

        var desc = Global.getSettings().getDescription(commoditySpec.id, Description.Type.RESOURCE)

        var img = tooltip!!.beginImageWithText(corePerson.portraitSprite, 96f)
        img!!.addTitle(name)

        img.addPara(desc.text1, 0f)

        img.addSpacer(5f)

        img.addPara("Level: ${corePerson.stats.level}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Level")
        img.addPara("Automated Points Multiplier: ${(corePerson.memoryWithoutUpdate.get(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT) as Float).toInt()}x", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Points Multiplier")

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Unique Skill: ${skillSpec.name}", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        when (skillSpec.id) {
            "rat_core_time" -> TimeCoreSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
            "rat_core_space" -> SpaceCoreSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
            "rat_core_seraph" -> SeraphCoreSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
            "rat_core_primordial" -> PrimordialCoreSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
            "rat_hyperlink" -> HyperlinkSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
            "rat_exo_processor" -> ExoProcessorSkill().createCustomDescription(null, null, tooltip, tooltip.widthSoFar)
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource)
    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        val base = commoditySpec.basePrice
        return base.toInt()
    }




}