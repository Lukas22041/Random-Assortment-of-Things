package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener

class SpatialInstabilityScript(var ship: ShipAPI) : AdvanceableListener {

    class InstabilityStack(var duration: Float)

    var stacks = ArrayList<InstabilityStack>()

    override fun advance(amount: Float) {
        for (stack in ArrayList(stacks)) {
            stack.duration = stack.duration - 1f * amount

            if (stack.duration < 0) {
                stacks.remove(stack)
            }
        }

        //var mult = 0.01f
        var mult = 0.005f
        mult *= stacks.size

        ship.mutableStats.hullDamageTakenMult.modifyMult("rat_spatial_instability", 1f + mult)
        ship.mutableStats.armorDamageTakenMult.modifyMult("rat_spatial_instability", 1f + mult)
    }
}