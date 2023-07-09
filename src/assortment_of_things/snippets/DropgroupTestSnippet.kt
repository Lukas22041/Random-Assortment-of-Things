package assortment_of_things.snippets

import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDebug.LunaSnippet
import lunalib.lunaDebug.SnippetBuilder
import java.util.*

class DropgroupTestSnippet : LunaSnippet()
{
    override fun getName(): String {
        return "Dropgroup Debug"
    }

    override fun getDescription(): String {
        return "Does 10.000 Attempts to spawn something from a dropgroup and prints the gathered data afterwards."
    }

    override fun getModId(): String {
        return RATSettings.modID
    }

    override fun getTags(): MutableList<String> {
        return mutableListOf(LunaSnippet.SnippetTags.Debug.toString())
    }

    override fun addParameters(parameter: SnippetBuilder?) {
        parameter!!.addStringParameter("Drop Group", "dropgroup")
    }

    override fun execute(parameter: MutableMap<String, Any>?, output: TooltipMakerAPI) {
        var dropgroup = parameter!!.get("dropgroup") as String


        var cargo = Global.getFactory().createCargo(true)

        try {
            var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
            var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()

            var drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 10000
            drop.group = dropgroup
            dropRandom.add(drop)

            cargo = SalvageEntity.generateSalvage(Random(), 1f, 1f, 1f, 1f, dropValue, dropRandom)
        } catch (e: Throwable)
        {
            output.addPara("Some error has occured:", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
            output.addPara(e.message, 0f)
            return
        }

        var total = cargo.stacksCopy.map { it.size }.sum()

        var totalPercent = total / 10000 * 100
        val totalPercentString = String.format("%.2f", totalPercent)

        output.addPara("Printing Results for \"$dropgroup\":", 0f, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())
        output.addPara("${total.toInt()} out of 10.000 Attempts generated an item ($totalPercentString%%):", 0f, Misc.getTextColor(), Misc.getHighlightColor())
        output.addSpacer(10f)


        var specs = HashMap<CargoStackAPI, Any>()

        for (item in cargo.stacksCopy)
        {
            if (item.isFighterWingStack)
            {
                var spec = Global.getSettings().getFighterWingSpec(item.fighterWingSpecIfWing.id)
                specs.put(item, spec)
            }
            else if (item.isWeaponStack)
            {
                var spec = Global.getSettings().getWeaponSpec(item.weaponSpecIfWeapon.weaponId)
                specs.put(item, spec)
            }
            else if (!item.isSpecialStack)
            {
                var spec = Global.getSettings().getCommoditySpec(item.commodityId)
                specs.put(item, spec)
            }
            else if (item.isSpecialStack)
            {
                var spec = Global.getSettings().getSpecialItemSpec(item.specialItemSpecIfSpecial.id)
                specs.put(item, spec)
            }
            Global.getSector().eventManager
        }

        var sortedSpecs = specs.toSortedMap(compareByDescending() { it.size })

        for ((item, spec) in sortedSpecs)
        {
            var percent = item.size / 10000 * 100
            val percentString = String.format("%.2f", percent)

            //if (spec is FighterWingSpecAPI) continue
           // if (spec is WeaponSpecAPI) continue

            var iconname = "graphics/fx/rat_darkener.png"
            Global.getSettings().loadTexture(iconname)
            if (spec is SpecialItemSpecAPI) iconname = spec.iconName
            if (spec is CommoditySpecAPI) iconname = spec.iconName
            if (spec is WeaponSpecAPI) iconname = spec.turretSpriteName
            if (spec is FighterWingSpecAPI) iconname = spec.variant.hullSpec.spriteName

            var stackName = ""
            if (spec is SpecialItemSpecAPI) stackName = spec.name
            if (spec is CommoditySpecAPI) stackName = spec.name
            if (spec is WeaponSpecAPI) stackName = spec.weaponName
            if (spec is FighterWingSpecAPI) stackName = spec.wingName

            var img = output.beginImageWithText(iconname, 16f)
            img.addPara("$stackName: $percentString%%", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$stackName")
            if (spec is SpecialItemSpecAPI)
            {
                img.addPara("Special Data: ${item.specialDataIfSpecial.data}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "$name")
            }
            output.addImageWithText(0f)
          //  output.addPara("Test: ${item.displayName}", 0f)
            output.addSpacer(5f)
        }

    }
}