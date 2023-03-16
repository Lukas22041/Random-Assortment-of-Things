package assortment_of_things.combat.hullmods.random

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.ui.TooltipMakerAPI

class RandomHullmodBuilder
{
    var effectId: String = ""
    var weight = 1f
    var possibleNames = ArrayList<String>()
    var description: (TooltipMakerAPI, HullSize, ShipAPI) -> Unit = { tooltipMakerAPI: TooltipMakerAPI, hullSize: HullSize, shipAPI: ShipAPI -> }
    var postDescription: (TooltipMakerAPI, HullSize, ShipAPI) -> Unit = { tooltipMakerAPI: TooltipMakerAPI, hullSize: HullSize, shipAPI: ShipAPI -> }

    var applyEffectsBeforeShipCreation: (ShipAPI, HullSize) -> Unit = { shipAPI: ShipAPI, hullSize: HullSize -> }
    var applyEffectsAfterShipCreation:(ShipAPI, String) -> Unit = { shipAPI: ShipAPI, string: String -> }

    var frigateCost = 4
    var destroyerCost = 6
    var cruiserCost = 8
    var capitalCost = 12

    var incompatibleHullmods = ArrayList<String>()
    var incompatibleEffects = ArrayList<String>()



    fun build()
    {

    }
}