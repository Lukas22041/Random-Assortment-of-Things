package assortment_of_things.exotech.industry

import assortment_of_things.campaign.industries.BaseConsumeableIndustry
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.Industry.AICoreDescriptionMode
import com.fs.starfarer.api.campaign.econ.Industry.ImprovementDescriptionMode
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*

class AsteroidMiningIndustry : BaseConsumeableIndustry() {

    data class ExpeditionFleetResults(val planet: PlanetAPI, val duration: Float, val timestamp: Long, var hasTechmining: Boolean)

    var expeditionResults = ArrayList<ExpeditionFleetResults>()
    var deployTimestamp = Global.getSector().clock.timestamp
    var deploymentIntervalDays = 3f

    override fun apply() {
        updateIncomeAndUpkeep()

        if (isFunctional) {
            getSupply(Commodities.ORE).quantity.modifyFlat("rat_asteroid_0", MathUtils.clamp(market.size + 1f, 1f, 10f), currentName)
            getSupply(Commodities.RARE_ORE).quantity.modifyFlat("rat_asteroid_1", MathUtils.clamp(market.size - 1f, 1f, 10f), currentName)
            getSupply(Commodities.VOLATILES).quantity.modifyFlat("rat_asteroid_2", MathUtils.clamp(market.size.toFloat() - 2f, 1f, 10f), currentName)
            getSupply(Commodities.ORGANICS).quantity.modifyFlat("rat_asteroid_3", MathUtils.clamp(market.size.toFloat() - 2f, 1f, 10f), currentName)

            getDemand(Commodities.FUEL).quantity.modifyFlat("rat_asteroid", MathUtils.clamp(market.size.toFloat(), 1f, 10f), currentName)
        }
        else {
            getSupply(Commodities.ORE).quantity.unmodifyFlat("rat_asteroid_0")
            getSupply(Commodities.RARE_ORE).quantity.unmodifyFlat("rat_asteroid_1")
            getSupply(Commodities.VOLATILES).quantity.unmodifyFlat("rat_asteroid_2")
            getSupply(Commodities.ORGANICS).quantity.unmodifyFlat("rat_asteroid_3")

            getDemand(Commodities.FUEL).quantity.unmodifyFlat("rat_asteroid")
        }
    }


    override fun unapply() {
        super.unapply()
        Global.getSector().listenerManager.removeListener(this);
    }

    fun getFleets() : ArrayList<CampaignFleetAPI> {
        var fleets = market.memoryWithoutUpdate.get("\$rat_asteroid_fleets") as ArrayList<CampaignFleetAPI>?
        if (fleets == null) {
            fleets = ArrayList<CampaignFleetAPI>()
            market.memoryWithoutUpdate.set("\$rat_asteroid_fleets", fleets)
        }
        return fleets
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, mode: Industry.IndustryTooltipMode?) {
        tooltip!!.addSpacer(10f)
    }

    override fun canInstallAICores(): Boolean {
        return false
    }

    override fun isAvailableToBuild(): Boolean {
        if (market.faction != Global.getSector().playerFaction) return false
        if (getStack() != null) return true
        return false
    }

    override fun showWhenUnavailable(): Boolean {
        return false
    }

}