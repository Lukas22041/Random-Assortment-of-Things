package assortment_of_things.campaign.abilities

import assortment_of_things.modular_weapons.ui.WeaponCraftingUIMain
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaProgressBar
import lunalib.lunaExtensions.openLunaCustomPanel

class OpenWeaponCraftingScreenAbility : BaseDurationAbility() {
    override fun activateImpl() {
        Global.getSector().openLunaCustomPanel(WeaponCraftingUIMain())
    }

    override fun applyEffect(amount: Float, level: Float) {

    }

    override fun deactivateImpl() {

    }

    override fun cleanupImpl() {

    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        tooltip!!.addPara("Weapon Forge", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
        tooltip.addSpacer(10f)

        tooltip.addPara("Opens the weapon forge screen.", 0f, Misc.getTextColor(), Misc.getHighlightColor())
    }
}