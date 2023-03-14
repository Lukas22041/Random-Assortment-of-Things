package assortment_of_things.combat.hullmods.random

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class BaseRandomHullmodEffect
{

    abstract fun getEffectId() : String

    abstract fun getPossibleNames(): Map<String, Float>

    abstract fun getDescription(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean)

    abstract fun getWeight() : Float

    open fun getPostDescription(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {}

    abstract fun applyEffectPreCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?)

    abstract fun applyEffectAfterCreation(ship: ShipAPI?, id: String?)

    open fun getLimitPerSector() : Int = 100

    open fun getIncompatible() = listOf<String>()
    open fun getIncompatibleEffects() = listOf<String>()

    open fun getFrigateCost() = 4
    open fun getDestroyerCost() = 6
    open fun getCruiserCost() = 8
    open fun getCapitalCost() = 12

    open fun isNegative() = false

}