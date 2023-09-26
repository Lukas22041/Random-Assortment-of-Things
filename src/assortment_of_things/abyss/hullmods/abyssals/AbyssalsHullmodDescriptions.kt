package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

object AbyssalsHullmodDescriptions {

    fun createDescription(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean)
    {
        when (ship!!.baseOrModSpec().hullId) {
            "rat_merrow" -> merrowDescription(tooltip!!, ship)
            "rat_makara" -> abolethDescription(tooltip!!, ship)
            "rat_chuul" -> chullDescription(tooltip!!, ship)
            "rat_aboleth" -> abolethDescription(tooltip!!, ship)
            "rat_aboleth_m" -> abolethDescription(tooltip!!, ship)
            "rat_morkoth" -> morkothDescription(tooltip!!, ship)
        }
    }

    fun merrowDescription(tooltip: TooltipMakerAPI, ship: ShipAPI)
    {
        var chronosSelected = isChronosSelected(ship)
        var cosmosSelected = isCosmosSelected(ship)

        var chronosColor = Misc.getTextColor()
        if (!chronosSelected) chronosColor = Misc.getGrayColor()

        var cosmosColor = Misc.getTextColor()
        if (!cosmosSelected) cosmosColor = Misc.getGrayColor()

        var chronosImage = tooltip.beginImageWithText(getChronosImage(ship), 64f)
        chronosImage.addPara("Chronos Core\n" +
                "Enables the \"Accelerated Barrels\" Shipsystem, which increases the firerate of all ballistic and energy weapons without increasing flux useage. If a weapon uses ammunition, it also recharges much quicker.", 0f,
            chronosColor, Misc.getHighlightColor(), "Chronos Core")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText(getCosmosImage(ship), 64f)
        cosmosImage.addPara("Cosmos Core\n" +
                "Enables the \"Abyssal Rift\" Shipsystem, which increases the ships flux dissipation and allows the ship to vent hardflux without taking down its shields.", 0f,
            cosmosColor, Misc.getHighlightColor(), "Cosmos Core")
        tooltip.addImageWithText(0f)
    }

    fun chullDescription(tooltip: TooltipMakerAPI, ship: ShipAPI)
    {
        var chronosSelected = isChronosSelected(ship)
        var cosmosSelected = isCosmosSelected(ship)

        var chronosColor = Misc.getTextColor()
        if (!chronosSelected) chronosColor = Misc.getGrayColor()

        var cosmosColor = Misc.getTextColor()
        if (!cosmosSelected) cosmosColor = Misc.getGrayColor()

        var chronosImage = tooltip.beginImageWithText(getChronosImage(ship), 64f)
        chronosImage.addPara("Chronos Core\n" +
                "Enables the \"Temporal Skimmer\" Shipsystem, allowing the ship to teleport in the direction it is travelling. During the teleport the time perceived by the ship is accelerated.", 0f,
            chronosColor, Misc.getHighlightColor(), "Chronos Core")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText(getCosmosImage(ship), 64f)
        cosmosImage.addPara("Cosmos Core\n" +
                "Enables the \"Singularity\" Shipsystem, which for a short time increases the damage dealt by all weapons .", 0f,
            cosmosColor, Misc.getHighlightColor(), "Cosmos Core")
        tooltip.addImageWithText(0f)
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
                "Enables the \"Temporal Grid\" Shipsystem, which temporarily increases the timeflow of the ship.", 0f,
            chronosColor, Misc.getHighlightColor(), "Chronos Core")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText(getCosmosImage(ship), 64f)
        cosmosImage.addPara("Cosmos Core\n" +
                "Enables the \"Phase Dive\" Shipsystem, which temporarily dives the ship in to p-space. The ship is able to dissipate a small amounts of soft-flux while phased.", 0f,
            cosmosColor, Misc.getHighlightColor(), "Cosmos Core")
        tooltip.addImageWithText(0f)
    }

    fun morkothDescription(tooltip: TooltipMakerAPI, ship: ShipAPI) {
        var chronosSelected = isChronosSelected(ship)
        var cosmosSelected = isCosmosSelected(ship)

        var chronosColor = Misc.getTextColor()
        if (!chronosSelected) chronosColor = Misc.getGrayColor()

        var cosmosColor = Misc.getTextColor()
        if (!cosmosSelected) cosmosColor = Misc.getGrayColor()

        var chronosImage = tooltip.beginImageWithText(getChronosImage(ship), 64f)
        chronosImage.addPara("Chronos Core\n" +
                "Enables the \"Temporal Jets\" Shipsystem, which provides a brief but extreme boost to top speed and maneuverability.", 0f,
            chronosColor, Misc.getHighlightColor(), "Chronos Core")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText(getCosmosImage(ship), 64f)
        cosmosImage.addPara("Cosmos Core\n" +
                "Enables the \"Event Horizon\" Shipsystem, which converts any shield damage taken in to buffs for weapon, flux and shield stats.", 0f,
            cosmosColor, Misc.getHighlightColor(), "Cosmos Core")
        tooltip.addImageWithText(0f)
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
        if (AbyssalsCoreHullmod.isChronosCore(ship)) return true

        return false
    }

    fun isCosmosSelected(ship: ShipAPI) : Boolean
    {
        if (AbyssalsCoreHullmod.isCosmosCore(ship)) return true
        return false
    }
}