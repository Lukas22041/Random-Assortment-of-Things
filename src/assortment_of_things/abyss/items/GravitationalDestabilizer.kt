package assortment_of_things.abyss.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class GravitationalDestabilizer : BaseSpecialItemPlugin() {
    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return super.getPrice(market, submarket)
    }

    override fun getDesignType(): String? {
        return null
    }

    override fun createTooltip(tooltip: TooltipMakerAPI,
                               expanded: Boolean,
                               transferHandler: CargoTransferHandlerAPI?,
                               stackSource: Any?) {
        //super.createTooltip(tooltip, expanded, transferHandler, stackSource);
        val pad = 3f
        val opad = 10f
        val small = 5f
        val h = Misc.getHighlightColor()
        val g = Misc.getGrayColor()
        var b = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()
        tooltip.addTitle(name)
        val design = designType
        if (design != null) {
            Misc.addDesignTypePara(tooltip, design, 10f)
        }
        if (!spec.desc.isEmpty()) {
            tooltip.addPara(spec.desc, Misc.getTextColor(), opad)
        }
        addCostLabel(tooltip, opad, transferHandler, stackSource)
        tooltip.addPara("Right-click to integrate the $name with your fleet", b, opad)
    }

    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()
    }

    override fun isTooltipExpandable(): Boolean {
        return false
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        Global.getSoundPlayer().playUISound(getSpec().soundId, 1f, 1f)

        Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_singularity_jump_ability", true, 0f);

        Global.getSector().campaignUI.messageDisplay.addMessage("$name integrated - can use \"Singularity Jump\" ability.") //,
    }
}