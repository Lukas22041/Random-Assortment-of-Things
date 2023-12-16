package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class SpecialisedTrainingFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Constructs both training halls and fields to improve your crews readiness for fleet operations. Decreases the minimum crew requirement of all ships in the fleet by 15%%", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "minimum crew","15%")
    }

    override fun advance(amount: Float) {

    }

    override fun unapply() {

    }
}