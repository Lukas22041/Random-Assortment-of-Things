package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class SpecialisedTrainingFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Constructs both training halls and fields to improve your crews readiness for fleet operations. Decreases the minimum crew requirement of all ships in the fleet by 15%%. " +
                "\n\n" +
                "Also produces some crew and marines per month. Does not produce any if the settlement currently holds more than 250 of them.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "minimum crew","15%", "crew", "marines", "250")
    }

    override fun advance(amount: Float) {

    }

    override fun unapply() {

    }


    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        var cargo = Global.getFactory().createCargo(true)

        addCrew(current, cargo)
        addMarines(current, cargo)

        return cargo
    }

    fun addCrew(current: CargoAPI, cargo: CargoAPI) {
        var max = 250f

        var amount = MathUtils.getRandomNumberInRange(30f, 80f)

        var inCargo = current.getCommodityQuantity(Commodities.CREW)
        var availableSpace = max - inCargo
        availableSpace = MathUtils.clamp(availableSpace, 0f, max)
        var toAdd = MathUtils.clamp(amount, 0f, availableSpace)
        toAdd = MathUtils.clamp(toAdd, 0f, max)
        cargo.addCommodity(Commodities.CREW, toAdd)
    }

    fun addMarines(current: CargoAPI, cargo: CargoAPI) {
        var max = 250f

        var amount = MathUtils.getRandomNumberInRange(20f, 50f)

        var inCargo = current.getCommodityQuantity(Commodities.MARINES)
        var availableSpace = max - inCargo
        availableSpace = MathUtils.clamp(availableSpace, 0f, max)
        var toAdd = MathUtils.clamp(amount, 0f, availableSpace)
        toAdd = MathUtils.clamp(toAdd, 0f, max)
        cargo.addCommodity(Commodities.MARINES, toAdd)
    }
}