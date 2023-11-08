package assortment_of_things.relics.conditions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.LunaCommons

class RampantMilitaryCore : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {
        var core = market.memoryWithoutUpdate.get("\$rat_military_core_person") as PersonAPI?
        if (core == null) {
            core = Global.getFactory().createPerson() as PersonAPI
            core.setFaction(market.factionId)
            core.name = FullName("Military Core", "", FullName.Gender.ANY)
            core.portraitSprite = "graphics/portraits/cores/rat_military_core.png"

            core.rankId = null
            core.postId = Ranks.POST_ADMINISTRATOR

            core.stats.setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1f)
            core.stats.setSkillLevel(Skills.HYPERCOGNITION, 1f)
            market.memoryWithoutUpdate.set("\$rat_military_core_person", core)
        }

        core.setFaction(market.factionId)
        market.admin = core
        if (market.commDirectory.getEntryForPerson(core) == null) {
            market.commDirectory.addPerson(core)
            market.commDirectory.getEntryForPerson(core)
        }
        market.stability.modifyFlat(id, -1f, condition.name)
        market.stats.dynamic.getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, 0.30f, condition.name)

        market.stats.dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(id, 1f, condition.name)
        market.stats.dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, 1f, condition.name)
        market.stats.dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, 1f, condition.name)
    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
      /*  var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)*/
        var base = 0 + market.size

        tooltip!!.addSpacer(10f)
        tooltip.addPara("Infrastructure left behind on the planet is controlled by an alpha-core with military specialisation. Its socket is located in some unknown location and is likely impossible to track down. AI Inspections have no chance of discovering it. \n\n" +
                "It shows no interest in giving up control, but since its control mechanisms prevent it from influencing the outside world, it is ready to come to a \"mutually beneficial relationship\" with any new inhabitants. " +
                "The core can not be removed." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "The core can not be removed.")
        tooltip.addSpacer(10f)


        tooltip.addPara("+30%% fleet size", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+30%")
        tooltip.addPara("+1 maximum amount of patrol fleets of each size.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+1")
        tooltip.addPara("-1 stability.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "-1")


    }

}