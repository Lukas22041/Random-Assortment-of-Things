package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class QualityControlFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Holds the necessary tools to improve the average quality standard across the entire fleet. This reduces the amount of required monthly supplies by a whole 20%%/15%%/10%%/5%% based on the hull-size of each ship.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "monthly supplies","20%", "15%", "10%", "5%")
    }

    override fun advance(amount: Float) {

    }

    override fun unapply() {

    }
}