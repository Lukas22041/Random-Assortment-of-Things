package assortment_of_things.modular_weapons.data

import assortment_of_things.modular_weapons.effects.ModularWeaponEffect
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import java.awt.Color

data class SectorWeaponData(var id: String) : Cloneable {

    fun getBudget(): Float
    {
        var budget = baseBudget
        for ((key, value) in budgetAdditions)
        {
            budget += value
        }
        return budget
    }

    var maxBudget = 150f
    var baseBudget = 0f
    var budgetAdditions: MutableMap<String, Float> = HashMap()

    var name = "Modular Weapon"
    var op = ModularStatInt(15)

    var finalized = false

    var effects: MutableList<ModularWeaponEffect> = ArrayList()

    var color = Color(0, 120, 200, 255)

    var weaponSize = WeaponSize.MEDIUM
    var mountType = WeaponType.HYBRID
    var damageType = DamageType.ENERGY


    var range = ModularStatFloat(800f)
    var projectileLength = ModularStatFloat(25f)
    var projectileWidth = ModularStatFloat(20f)
    var projectileSpeed = ModularStatFloat(1000f)

    var damagePerShot = ModularStatFloat(100f)
    var energyPerShot = ModularStatFloat(75f)
    var empDamage = ModularStatFloat(0f)

    var maxAmmo = ModularStatInt(Int.MAX_VALUE)
    var ammoPerSecond = ModularStatFloat(1f)

    var chargeUp = ModularStatFloat(0f)
    var chargeDown = ModularStatFloat(0.4f)
    var burstSize = ModularStatInt(1)
    var burstDelay = ModularStatFloat(0.1f)

    var minSpread = ModularStatFloat(1f)
    var maxSpread = ModularStatFloat(4f)
    var spreadBuildup = ModularStatFloat(1f)
    var spreadDecay = ModularStatFloat(4f)

}