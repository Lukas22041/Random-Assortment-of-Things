package assortment_of_things.misc

import assortment_of_things.RATCampaignPlugin
import assortment_of_things.abyss.skills.PrimordialCoreSkill
import assortment_of_things.abyss.skills.SeraphCoreSkill
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import assortment_of_things.exotech.skills.ExoProcessorSkill
import assortment_of_things.relics.skills.HyperlinkSkill
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.campaign.ui.trade.CargoItemStack
import com.fs.starfarer.ui.impl.StandardTooltipV2
import com.fs.state.AppDriver
import java.util.*

class AICoreTooltipScript : EveryFrameScript {

    var cores = mapOf(
        "rat_chronos_core" to "rat_core_time",
        "rat_cosmos_core" to "rat_core_space",
        "rat_seraph_core"  to "rat_core_seraph",
        "rat_primordial_core"  to "rat_core_primordial",
        "rat_neuro_core"  to "rat_hyperlink",
        "rat_exo_processor"  to "rat_exo_processor",
    )

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {

        if (!Global.getSector().isPaused) return

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI ?: return

        var tooltip = screenPanel.getChildrenCopy().find { it is StandardTooltipV2 } as UIPanelAPI? ?: return

        if (ReflectionUtils.hasVariableOfType(CargoItemStack::class.java, tooltip))
        {
            //if (tooltip == lastTooltip) return



            var stack = ReflectionUtils.get(null, tooltip, CargoItemStack::class.java) as CargoStackAPI? ?: return

            var tooltip = tooltip as TooltipMakerAPI

            var commodityId = stack.commodityId

            if (!cores.containsKey(commodityId)) return

            var commoditySpec = Global.getSettings().getCommoditySpec(commodityId)

            var panel = tooltip.getChildrenCopy().first() as UIPanelAPI? ?: return

            var first = panel.getChildrenCopy().first()
            if (first.position.x >= 20000) return //Dont modify twice

            var valuePara = panel.getChildrenCopy().filter { it is LabelAPI }.last()

            //panel.clearChildren()

            for (child in panel.getChildrenCopy()) {
                child.position.inTL(100000f, 100000f)
            }

            //tooltip.heightSoFar = 0f
            //tooltip.position.setSize(tooltip.position.width, 0f)

            var custom = Global.getSettings().createCustom(600f, 600f, null)
            tooltip.addCustom(custom, 0f)
            custom.position.inTL(0f, 0f)

            var element = custom.createUIElement(600f, 0f, false)
            custom.addUIElement(element)
            element.position.inTL(0f, 0f)

            var x = tooltip.position.x
            var y = tooltip.position.y
            var w = tooltip.position.width
            var h = tooltip.position.height



            //Tooltip
            var skillSpec = Global.getSettings().getSkillSpec(cores.get(commoditySpec.id))

            var name = commoditySpec.name
            var plugin = RATCampaignPlugin().pickAICoreOfficerPlugin(commoditySpec.id)!!.plugin

            var corePerson = plugin.createPerson(commoditySpec.id, Factions.NEUTRAL, Random())
            var pointsMult = corePerson.memoryWithoutUpdate.getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT).toInt()

            var desc = Global.getSettings().getDescription(commoditySpec.id, Description.Type.RESOURCE)

            var img = element!!.beginImageWithText(corePerson.portraitSprite, 96f)
            img!!.addTitle(name)

            img.addPara(desc.text1, 0f)

            img.addSpacer(5f)

            img.addPara("Level: ${corePerson.stats.level}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Level")
            img.addPara("Automated Points Multiplier: ${(corePerson.memoryWithoutUpdate.get(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT) as Float).toInt()}x", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "Automated Points Multiplier")

            element.addImageWithText(0f)

            element.addSpacer(10f)
            element.addSectionHeading("Unique Skill: ${skillSpec.name}", Alignment.MID, 0f)
            element.addSpacer(10f)

            when (skillSpec.id) {
                "rat_core_time" -> TimeCoreSkill().createCustomDescription(null, null, element, element.widthSoFar)
                "rat_core_space" -> SpaceCoreSkill().createCustomDescription(null, null, element, element.widthSoFar)
                "rat_core_seraph" -> SeraphCoreSkill().createCustomDescription(null, null, element, element.widthSoFar)
                "rat_core_primordial" -> PrimordialCoreSkill().createCustomDescription(null, null, element, element.widthSoFar)
                "rat_hyperlink" -> HyperlinkSkill().createCustomDescription(null, null, element, element.widthSoFar)
                "rat_exo_processor" -> ExoProcessorSkill().createCustomDescription(null, null, element, element.widthSoFar)
            }


         /*   element.addSpacer(10f)
            var valueText = "Base value: ${Misc.getDGSCredits(commoditySpec.basePrice)} per unit"
            if (valuePara != null) {
                var value = valuePara as LabelAPI
                valueText = value.text
            }
            element.addPara(valueText, 0f)*/





            valuePara.position.inTL(5f, element.heightSoFar+5)




            //panel.position.setSize(element.position.width+15, element.heightSoFar+10)
            tooltip.position.setSize(element.position.width+13, element.heightSoFar+35)
            tooltip.heightSoFar = element.heightSoFar

            var height = y - (tooltip.position.height-h)
            if (height <= 25) height = 25f //Otherwise may go outside of the screen if the item is viewed from below.

            //ReflectionUtils.invoke("sizeChanged", tooltip, element.position.width+13, element.heightSoFar+10)
            tooltip.position.inBL(x - (tooltip.position.width-w), height)


            var codexTooltip = tooltip.getChildrenCopy().find { it is LabelAPI } as LabelAPI?
            codexTooltip?.position?.inTL(15f, tooltip.position.height+3)


          /*  tooltip.addPara("Test")
            var test = ""*/



        }

    }

}