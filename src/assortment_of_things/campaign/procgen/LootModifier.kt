package assortment_of_things.campaign.procgen

import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec

object LootModifier
{
    //spec ID, Drop Group, Chance
    var originalSalvageChances: MutableMap<String, MutableMap<String, Int>> = HashMap()

    var hullmodGroups = listOf("any_hullmod_medium", "any_hullmod_high", "any_hullmod_low")
    var shipGroups = listOf("blueprints_low")

    fun saveOriginalData()
    {
        var specs = Global.getSettings().getAllSpecs(SalvageEntityGenDataSpec::class.java).toList() as List<SalvageEntityGenDataSpec>

        for (spec in specs)
        {
            var map: MutableMap<String, Int> = HashMap()
            for (drop in spec.dropRandom)
            {
                for (group in hullmodGroups)
                {
                    if (drop.group == group)
                    {
                        map.put(drop.group, drop.chances)
                    }
                }

                for (group in shipGroups)
                {
                    if (drop.group == group)
                    {
                        map.put(drop.group, drop.chances)
                    }
                }
            }
            originalSalvageChances.put(spec.id, map)
        }
    }

    fun modifySpawns()
    {
        var specs = Global.getSettings().getAllSpecs(SalvageEntityGenDataSpec::class.java).toList() as ArrayList<SalvageEntityGenDataSpec>
        var hullmodMod = RATSettings.hullmodLootFrequency
        var shipMod = RATSettings.shipLootFrequency

        for (spec in specs)
        {
            var ogData = originalSalvageChances.get(spec.id) ?: continue

            for (drop in spec.dropRandom)
            {
                for (group in hullmodGroups)
                {
                    if (drop.group == group)
                    {
                        var ogChances = ogData.get(group) ?: continue
                        drop.chances = (ogChances * hullmodMod!!).toInt()
                    }
                }
                for (group in shipGroups)
                {
                    if (drop.group == group)
                    {
                        var ogChances = ogData.get(group) ?: continue
                        drop.chances = (ogChances * shipMod!!).toInt()
                    }
                }
            }
        }
    }
}