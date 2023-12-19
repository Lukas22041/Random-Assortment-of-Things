package assortment_of_things.frontiers.plugins.modifiers

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem
import com.fs.starfarer.api.campaign.impl.items.ModSpecItemPlugin
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*
import kotlin.collections.ArrayList

open class RareTechModifier() : BaseSettlementModifier() {


    var amount = hashMapOf(
        1 to "sparse",
        2 to "moderate",
        3 to "abundant",
        4 to "rich")

    override fun getDescription(tooltip: TooltipMakerAPI) {
        var actualAmount = amount.get(getTier())
        var a = Misc.getAOrAnFor(actualAmount)
        tooltip.addPara("${getName()}:\n" +
                "This site is filled with $a $actualAmount amount of salvageable ruins, providing the settlement with the ability to perform tech-mining. Part of this output can be acquired by the owner.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "${getName()}", "$actualAmount")
    }

    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {

        var mult = 0.5f

        for (condition in settlement.primaryPlanet.market.conditions) {
            mult = when(condition.id) {
                Conditions.RUINS_SCATTERED -> 0.3f
                Conditions.RUINS_EXTENSIVE -> 0.5f
                Conditions.RUINS_WIDESPREAD -> 0.6f
                Conditions.RUINS_VAST -> 0.8f
                else -> 0.5f
            }
        }


        val dropRandom: MutableList<DropData> = ArrayList()
        val dropValue: MutableList<DropData> = ArrayList()

        var d = DropData()
        d.chances = 1
        d.group = "blueprints_low"
        //d.addCustom("item_:{tags:[single_bp], p:{tags:[rare_bp]}}", 1f);
        //d.addCustom("item_:{tags:[single_bp], p:{tags:[rare_bp]}}", 1f);
        dropRandom.add(d)

        d = DropData()
        d.chances = 1
        d.group = "rare_tech_low"
        d.valueMult = 0.1f
        dropRandom.add(d)

        d = DropData()
        d.chances = 1
        d.group = "ai_cores3"
        //d.valueMult = 0.1f; // already a high chance to get nothing due to group setup, so don't reduce further
        //d.valueMult = 0.1f; // already a high chance to get nothing due to group setup, so don't reduce further
        dropRandom.add(d)

        d = DropData()
        d.chances = 1
        d.group = "any_hullmod_low"
        dropRandom.add(d)

        d = DropData()
        d.chances = 5
        d.group = "weapons2"
        dropRandom.add(d)

        d = DropData()
        //d.chances = 100;
        //d.chances = 100;
        d.group = "basic"
        d.value = 10000
        dropValue.add(d)

        val result = SalvageEntity.generateSalvage(Random(), 1f, 1f, mult, 1f, dropValue, dropRandom)

        val pf = Global.getSector().playerFaction
        OUTER@ for (stack in result.stacksCopy) {
            if (stack.plugin is BlueprintProviderItem) {
                val bp = stack.plugin as BlueprintProviderItem
                var list = bp.providedShips
                if (list != null) {
                    for (id in list) {
                        if (!pf.knowsShip(id)) continue@OUTER
                    }
                }
                list = bp.providedWeapons
                if (list != null) {
                    for (id in list) {
                        if (!pf.knowsWeapon(id)) continue@OUTER
                    }
                }
                list = bp.providedFighters
                if (list != null) {
                    for (id in list) {
                        if (!pf.knowsFighter(id)) continue@OUTER
                    }
                }
                list = bp.providedIndustries
                if (list != null) {
                    for (id in list) {
                        if (!pf.knowsIndustry(id)) continue@OUTER
                    }
                }
                result.removeStack(stack)
            } else if (stack.plugin is ModSpecItemPlugin) {
                val mod = stack.plugin as ModSpecItemPlugin
                if (!pf.knowsHullMod(mod.modId)) continue@OUTER
                result.removeStack(stack)
            }
        }

        //result.addMothballedShip(FleetMemberType.SHIP, "hermes_d_Hull", null);


        //result.addMothballedShip(FleetMemberType.SHIP, "hermes_d_Hull", null);
        return result
    }
}