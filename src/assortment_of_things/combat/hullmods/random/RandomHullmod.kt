package assortment_of_things.combat.hullmods.random

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.scripts.util.MagicIncompatibleHullmods


class RandomHullmod : BaseHullMod() {


    override fun init(spec: HullModSpecAPI?) {
        super.init(spec)

        var effects = getEffectScripts() ?: return

        if (spec == null) return

        var frigateCost = 0
        var destroyerCost = 0
        var cruiserCost = 0
        var capitalCost = 0
        for (effect in effects)
        {
            frigateCost += effect.getFrigateCost()
            destroyerCost += effect.getDestroyerCost()
            cruiserCost += effect.getCruiserCost()
            capitalCost += effect.getCapitalCost()
        }

        var name = getHullmodEffect().name
        if (name != null)
        {
            spec.displayName = "Derelict Hullmod: " + getHullmodEffect().name
        }
        else
        {
            spec.displayName = "Derelict Hullmod: Damaged Beyond Use"
        }

        spec.setFrigateCost(frigateCost)
        spec.setDestroyerCost(destroyerCost)
        spec.setCruiserCost(cruiserCost)
        spec.setCapitalCost(capitalCost)
    }

    fun getHullmodEffect() : RandomHullmodUtil.HullmodEffect
    {
        var unfilteredEffects = RandomHullmodUtil.getSavedEffects()
        var effect = unfilteredEffects.find { it.hullmodId == spec.id }
        return effect!!
    }

    fun getEffectScripts() : List<BaseRandomHullmodEffect>?
    {
        var unfilteredEffects = RandomHullmodUtil.getSavedEffects()
        var effects: MutableList<String> = unfilteredEffects.find { it.hullmodId == spec.id }?.effects ?: return null

        var effectScripts = RandomHullmodUtil.effects.filter { effects.contains(it.getEffectId()) }
        return effectScripts
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean)
    {
        var effects = getEffectScripts() ?: return

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Effects", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        if (effects.isEmpty())
        {
            tooltip.addPara("Due to damage in its blueprints, this hullmod seems to have no useable effects", 0f)
        }

        for (effect in effects)
        {
            if (effect.isNegative()) continue
            effect.getDescription(tooltip, hullSize, ship, width, isForModSpec)
        }
        for (effect in effects)
        {
            if (!effect.isNegative()) continue
            effect.getDescription(tooltip, hullSize, ship, width, isForModSpec)
        }
        tooltip.addSpacer(10f)

        for (effect in effects)
        {
            effect.getPostDescription(tooltip, hullSize, ship, width, isForModSpec)
        }

        var addedHeader = false
        tooltip.addSectionHeading("Incompatibilities", Alignment.MID, 0f)
        tooltip.addSpacer(10f)
        tooltip.addPara("Due to uncertainties with compatibility, only one Derelict Hullmod may be installed per hull.", 0f)
        tooltip.addSpacer(5f)

        for (effect in effects)
        {
            for (incompatible in effect.getIncompatible())
            {
                if (!addedHeader)
                {
                    addedHeader = true
                }
                var name = Global.getSettings().getHullModSpec(incompatible).displayName
                tooltip.addPara("Incompatible with $name.", 0f, Misc.getNegativeHighlightColor(), name)
            }
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?)
    {
        var effects = getEffectScripts() ?: return

        for (effect in effects)
        {
            effect.applyEffectPreCreation(hullSize, stats, id)

            for (incompatibleMod in effect.getIncompatible()) {
                if (stats != null) {
                    if (stats.variant.hasHullMod(incompatibleMod))
                    {
                        MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), incompatibleMod, spec.id)
                    }
                }
            }
        }

        if (stats != null)
        {
            for (mod in stats.variant.hullMods)
            {
                if (mod == spec.id) continue
                if (Global.getSettings().getHullModSpec(mod).hasTag("derelictHullmod"))
                {
                    MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), mod, spec.id)
                    break;
                }
            }
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?)
    {
        var effects = getEffectScripts() ?: return

        for (effect in effects)
        {
            effect.applyEffectAfterCreation(ship,id)
        }
    }
}