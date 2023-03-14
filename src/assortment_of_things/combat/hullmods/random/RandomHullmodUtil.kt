package assortment_of_things.combat.hullmods.random

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils

object RandomHullmodUtil {

    data class HullmodEffect(var hullmodId: String, var effects: MutableList<String>, var name: String?)

    var hullmodSpecs = Global.getSettings().allHullModSpecs.filter { it.hasTag("derelictHullmod") }
    var effects: MutableList<BaseRandomHullmodEffect> = ArrayList()

    fun assignEffects()
    {
        var allEffects = ArrayList<HullmodEffect>()
        var assignedNames = ArrayList<String>()
        var limits: MutableMap<BaseRandomHullmodEffect, Int> = HashMap()
        for (mod in hullmodSpecs.filterNotNull())
        {
            var usedEffects = ArrayList<String>()
            var pickedEffects: MutableList<BaseRandomHullmodEffect> = ArrayList()
            for (i in 0..MathUtils.getRandomNumberInRange(1, 2))
            {
                var picker = WeightedRandomPicker<BaseRandomHullmodEffect>()
                for (effect in effects)
                {
                    if (usedEffects.contains(effect.getEffectId())) continue
                    var limit = limits.get(effect)
                    if (limit != null && effect.getLimitPerSector() <= limit) continue

                    var incompatibleEffect = false
                    for (eff in pickedEffects)
                    {
                        if (eff.getIncompatibleEffects().contains(effect.getEffectId())) {
                            incompatibleEffect = true
                        }
                    }
                    if (incompatibleEffect) continue

                    picker.add(effect, effect.getWeight())
                }

                var pick = picker.pick() ?: continue

                var limit = limits.get(pick)
                if (limit == null) limits.put(pick, 1)
                else limits.put(pick, limit + 1)

                usedEffects.add(pick.getEffectId())
                pickedEffects.add(pick)
            }

            var namePicker = WeightedRandomPicker<String>()
            for (name in pickedEffects.map { it.getPossibleNames() })
            {
                for ((key, value) in name)
                {
                    namePicker.add(key, value)
                }
            }
            var namePick = namePicker.pick()
            assignedNames.add(namePick)

            allEffects.add(HullmodEffect(mod.id, pickedEffects.map { it.getEffectId() }.toMutableList(), namePick))
        }

        setSavedEffects(allEffects)
    }

    fun setSavedEffects(savedEffects: List<HullmodEffect>)
    {
        Global.getSector().memoryWithoutUpdate.set("\$rat_random_hullmods", savedEffects)
    }

    fun getSavedEffects() : List<HullmodEffect>
    {
        return Global.getSector().memoryWithoutUpdate.get("\$rat_random_hullmods") as List<HullmodEffect>
    }

}