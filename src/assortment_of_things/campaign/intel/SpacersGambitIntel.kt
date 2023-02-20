package assortment_of_things.campaign.intel

import assortment_of_things.campaign.interactions.SpacersGambitInteraction
import assortment_of_things.campaign.submarkets.SpacersGambitBlackmarket
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SpacersGambitIntel(var market: MarketAPI) : BaseIntelPlugin() {

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)

        val c = getTitleColor(mode)
        val tc = getBulletColorForMode(mode)
        info.addPara("Spacers Gambit", c, 0f)

        bullet(info)

        unindent(info)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        val desc = Global.getSettings().getDescription("rat_blackmarketPlanet", Description.Type.CUSTOM)
        info!!.addPara(desc.text1FirstPara, 0f)

        if (SpacersGambitInteraction.initialCostPaid != null && SpacersGambitInteraction.initialCostPaid!!)
        {
            var cargo = (market.getSubmarket("rat_spacersgambit_submarket")?.plugin as SpacersGambitBlackmarket).cargo

            if (cargo != null)
            {
                info.addPara("\nCurrently available wares: ", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
                info.addPara("", 0f)

                for (item in cargo.stacksCopy)
                {
                    if (item.isSpecialStack)
                    {
                        var img = info.beginImageWithText(item.specialItemSpecIfSpecial.iconName, 24f)
                        img.addPara("${item.specialItemSpecIfSpecial.name} x${item.size.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${item.displayName}")
                        info.addImageWithText(0f)
                    }
                    else if (item.isWeaponStack)
                    {
                        var img = info.beginImageWithText(item.weaponSpecIfWeapon.turretSpriteName, 24f)
                        img.addPara("${item.weaponSpecIfWeapon.weaponName} x${item.size.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${item.displayName}")
                        info.addImageWithText(0f)
                    }
                    else if (item.isCommodityStack)
                    {
                        var img = info.beginImageWithText(Global.getSettings().getCommoditySpec(item.commodityId).iconName, 24f)
                        img.addPara("${item.displayName} x${item.size.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${item.displayName}")
                        info.addImageWithText(0f)
                    }

                }
            }
            if (cargo == null || cargo.isEmpty)
            {
                info.addPara("There are currently no available wares ", 0f, Misc.getTextColor(), Misc.getHighlightColor())
            }
        }
        else
        {
            info.addPara("\nYou currently do not have access to its market. To gain access, ${SpacersGambitInteraction.agreedCost} Credits have to be delivered to the planets Administrator.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${SpacersGambitInteraction.agreedCost} Credits")
        }
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "pirate_activity")
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags = super.getIntelTags(map)
        tags.add(Factions.PIRATES)

        return tags
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return market.primaryEntity.starSystem.hyperspaceAnchor
    }

    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun getCommMessageSound(): String? {
        return "ui_discovered_entity"
    }
}