package assortment_of_things.combat.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.world.industry.bonus.AICoreHeavyIndustry
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.text.DecimalFormat

class StatComparer : BaseHullMod() {

    companion object
    {
        var selectedHullsize = "Match Ship Size"
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI, width: Float, isForModSpec: Boolean) {

        if (Keyboard.isKeyDown(Keyboard.KEY_H))
        {
            if (selectedHullsize == "Match Ship Size") selectedHullsize = "All"
            else if (selectedHullsize == "All") selectedHullsize = HullSize.FRIGATE.toString().lowercase().capitalize()
            else if (selectedHullsize == HullSize.FRIGATE.toString().lowercase().capitalize()) selectedHullsize = HullSize.DESTROYER.toString().lowercase().capitalize()
            else if (selectedHullsize == HullSize.DESTROYER.toString().lowercase().capitalize()) selectedHullsize = HullSize.CRUISER.toString().lowercase().capitalize()
            else if (selectedHullsize == HullSize.CRUISER.toString().lowercase().capitalize()) selectedHullsize = HullSize.CAPITAL_SHIP.toString().lowercase().capitalize()
            else if (selectedHullsize == HullSize.CAPITAL_SHIP.toString().lowercase().capitalize()) selectedHullsize = "Match Ship Size"
        }

        tooltip.addSectionHeading("Config", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("Hullsize: ${selectedHullsize.replace("_", " ")} (Hold H to Toggle)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Hullsize", "")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("Averages", Alignment.MID, 0f)
        var spec = ship.hullSpec

        var specs: List<ShipHullSpecAPI> = ArrayList()
        if (selectedHullsize == "Match Ship Size") specs = Global.getSettings().allShipHullSpecs.filter { it.hullSize == hullSize }
        if (selectedHullsize == "All") specs = Global.getSettings().allShipHullSpecs
        if (selectedHullsize == HullSize.FRIGATE.toString().lowercase().capitalize()) specs = Global.getSettings().allShipHullSpecs.filter { it.hullSize == HullSize.FRIGATE }
        if (selectedHullsize == HullSize.DESTROYER.toString().lowercase().capitalize()) specs = Global.getSettings().allShipHullSpecs.filter { it.hullSize == HullSize.DESTROYER }
        if (selectedHullsize == HullSize.CRUISER.toString().lowercase().capitalize()) specs = Global.getSettings().allShipHullSpecs.filter { it.hullSize == HullSize.CRUISER }
        if (selectedHullsize == HullSize.CAPITAL_SHIP.toString().lowercase().capitalize()) specs = Global.getSettings().allShipHullSpecs.filter { it.hullSize == HullSize.CAPITAL_SHIP }


        var hullIntegrity = specs.map { it.hitpoints }.average()
        var armorRating = specs.map { it.armorRating }.average()
        val shieldArc = specs.filter { it.shieldSpec.arc > 1 }.map { it.shieldSpec.arc }.average()
        val shieldUpkeep = specs.filter { it.shieldSpec.upkeepCost > 1 }.map { it.shieldSpec.arc }.average()
        val shieldFluxPerDamage = specs.map { it.shieldSpec.fluxPerDamageAbsorbed }.average()
        val fluxCapacity = specs.map { it.fluxCapacity }.average().toInt()
        val fluxDissipation = specs.map { it.fluxDissipation }.average()
        val maxSpeed = specs.map { it.engineSpec.maxSpeed }.average()

        var hullDiff = ((spec.hitpoints / hullIntegrity) * 100).toInt()
        var armorDiff = ((spec.armorRating / armorRating) * 100).toInt()
        var shieldArcDiff = ((spec.shieldSpec.arc / shieldArc) * 100).toInt()
        var shieldUpDiff = ((spec.shieldSpec.upkeepCost / shieldUpkeep) * 100).toInt()
        var shieldAbsorbDiff = ((spec.shieldSpec.fluxPerDamageAbsorbed / shieldFluxPerDamage) * 100).toInt()
        var fluxCapDiff = ((spec.fluxCapacity / fluxCapacity) * 100).toInt()
        var fluxDissDiff = ((spec.fluxDissipation / fluxDissipation) * 100).toInt()
        var maxSpeedDiff = ((spec.engineSpec.maxSpeed / maxSpeed) * 100).toInt()

        var format = DecimalFormat("#.##")

        var tc = Misc.getTextColor()
        var hc = Misc.getHighlightColor()

        tooltip.addSpacer(5f)
        tooltip.addPara("Hull: ${format.format(hullIntegrity)} ($hullDiff%%)", 0f, tc, "Hull:", "$hullDiff%").setHighlightColors(hc, percentColor(hullDiff))
        tooltip.addPara("Armor: ${format.format(armorRating)} ($armorDiff%%)", 0f, tc, "Armor:", "$armorDiff%").setHighlightColors(hc, percentColor(armorDiff))

        if (ship.shield != null)
        {
            tooltip.addPara("Shield Arc: ${format.format(shieldArc)} ($shieldArcDiff%%)", 0f, tc, "Shield Arc:", "$shieldArcDiff%").setHighlightColors(hc, percentColor(shieldArcDiff))
            tooltip.addPara("Shield Upkeep: ${format.format(shieldUpkeep)} ($shieldUpDiff%%)", 0f, tc, "Shield Upkeep:", "$shieldUpDiff%").setHighlightColors(hc, percentColorReverse(shieldUpDiff))
            tooltip.addPara("Flux per Shield Damage: ${format.format(shieldFluxPerDamage)} ($shieldAbsorbDiff%%)", 0f, tc, "Flux per Shield Damage:", "$shieldAbsorbDiff%").setHighlightColors(hc, percentColorReverse(shieldAbsorbDiff))

        }
        tooltip.addPara("Flux Capacity: ${format.format(fluxCapacity)} ($fluxCapDiff%%)", 0f, tc, "Flux Capacity:", "$fluxCapDiff%").setHighlightColors(hc, percentColor(fluxCapDiff))
        tooltip.addPara("Flux Dissipation: ${format.format(fluxDissipation)} ($fluxDissDiff%%)", 0f, tc, "Flux Dissipation:", "$fluxDissDiff%").setHighlightColors(hc, percentColor(fluxDissDiff))
        tooltip.addPara("Max Speed: ${format.format(maxSpeed)} ($maxSpeedDiff%%)", 0f, tc, "Max Speed:", "$maxSpeedDiff%").setHighlightColors(hc, percentColor(maxSpeedDiff))
    }

    fun percentColor(percentage: Number) : Color
    {
        if (percentage in 0..99) return Misc.getNegativeHighlightColor()
        if (percentage == 100) return Misc.getTextColor()
        if (percentage in 101..1000) return Misc.getPositiveHighlightColor()
        else return Misc.getTextColor()
    }

    fun percentColorReverse(percentage: Number) : Color
    {
        if (percentage in 0..99) return Misc.getPositiveHighlightColor()
        if (percentage == 100) return Misc.getTextColor()
        if (percentage in 101..1000) return Misc.getNegativeHighlightColor()
        else return Misc.getTextColor()
    }

    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {
        super.advanceInCampaign(member, amount)


    }

    fun calculateAverageShipdata(hullsize: HullSize)
    {


    }
}