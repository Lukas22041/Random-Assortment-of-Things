package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

object AbyssalsHullmodDescriptions {

    fun createDescription(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean)
    {
        when (ship!!.hullSpec.hullId) {
            "rat_aboleth" -> abolethDescription(tooltip!!, ship)
        }
    }

    fun getChronosImage(ship: ShipAPI) : String
    {
        var spec = Global.getSettings().getCommoditySpec(RATItems.CHRONOS_CORE)
        var path = ""

        if (!isChronosSelected(ship)) {
            path = spec.iconName.replace(".png", "") + "_gray.png"
        }
        else
        {
            path = spec.iconName
        }
        Global.getSettings().loadTexture(path)

        return path
    }

    fun getCosmosImage(ship: ShipAPI) : String
    {
        var spec = Global.getSettings().getCommoditySpec(RATItems.COSMOS_CORE)
        var path = ""

        if (!isCosmosSelected(ship)) {
            path = spec.iconName.replace(".png", "") + "_gray.png"
        }
        else
        {
            path = spec.iconName
        }
        Global.getSettings().loadTexture(path)

        return path
    }

    fun isChronosSelected(ship: ShipAPI) : Boolean
    {
        if (ship.captain != null)
        {
            if (ship.captain.aiCoreId == RATItems.CHRONOS_CORE)
            {
                return true
            }
        }
        return false
    }

    fun isCosmosSelected(ship: ShipAPI) : Boolean
    {
        if (ship.captain != null)
        {
            if (ship.captain.aiCoreId == RATItems.COSMOS_CORE)
            {
                return true
            }
        }
        return false
    }


    fun abolethDescription(tooltip: TooltipMakerAPI, ship: ShipAPI) {
        var chronosSelected = isChronosSelected(ship)
        var cosmosSelected = isCosmosSelected(ship)

        var chronosColor = Misc.getTextColor()
        if (!chronosSelected) chronosColor = Misc.getGrayColor()

        var cosmosColor = Misc.getTextColor()
        if (!cosmosSelected) cosmosColor = Misc.getGrayColor()

        var chronosImage = tooltip.beginImageWithText(getChronosImage(ship), 64f)
        chronosImage.addPara("Chronos Core\n" +
                "The Chronos Core enables the \"Temporal Flow\" Shipsystem, which temporarily increases the timeflow of the ship.", 0f,
            chronosColor, Misc.getHighlightColor(), "Chronos Core")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText(getCosmosImage(ship), 64f)
        cosmosImage.addPara("Cosmos Core\n" +
                "The Cosmos Core enables the \"Phase Flow\" Shipsystem, which temporarily dives the ship in to p-space.", 0f,
            cosmosColor, Misc.getHighlightColor(), "Cosmos Core")
        tooltip.addImageWithText(0f)
    }

}