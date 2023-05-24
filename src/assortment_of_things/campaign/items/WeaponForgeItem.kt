package assortment_of_things.campaign.items

import assortment_of_things.modular_weapons.ui.WeaponCraftingUIMain
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import lunalib.lunaExtensions.openLunaCustomPanel

class WeaponForgeItem : BaseSpecialItemPlugin() {


    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }

    override fun performRightClickAction() {

    }
}