package assortment_of_things.campaign.items

import assortment_of_things.scripts.AtMarketListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI

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