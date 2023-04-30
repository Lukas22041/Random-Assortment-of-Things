package assortment_of_things.modular_weapons.data

import assortment_of_things.modular_weapons.effects.ModularWeaponEffect
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import java.awt.Color
import kotlin.random.Random

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
    var description = "Custom Description"
    var op = MutableStat(14f)

    var finalized = false

    var effects: MutableList<ModularWeaponEffect> = ArrayList()

    var color = Color(0, 120, 200, 255)

    var weaponSize = WeaponSize.MEDIUM
    var mountType = WeaponType.HYBRID
    var damageType = DamageType.ENERGY

    var isPD = false

    var muzzleDuration = 0.5f
    var muzzleColor = Color(100,200,255,255)
    var muzzleParticles = 7

    var rngAttempts = ModularStatInt(0)
    fun rngCheck(chance: Float, attemptsDone: Int) : Boolean
    {
        if (rngAttempts.getValue() >= 0)
        {
            if (Random.nextFloat() < chance)
            {
                return true
            }
            else
            {
                if (attemptsDone >= rngAttempts.getValue())
                {
                    return false
                }
                else
                {
                    return rngCheck(chance, attemptsDone + 1)
                }
            }
        }
        else
        {
            if (Random.nextFloat() < chance)
            {
                if (attemptsDone >= -rngAttempts.getValue())
                {
                        return true
                }
                else
                {
                    return rngCheck(chance, attemptsDone + 1)
                }
            }
            else
            {
                return false

            }
        }
    }

    var range = MutableStat(700f)
    var turnrate = MutableStat(10f)

    var projectileLength = MutableStat(25f)
    var projectileWidth = MutableStat(20f)
    var projectileSpeed = MutableStat(1000f)

    var damagePerShot = MutableStat(100f)
    var energyPerShot = MutableStat(50f)
    var empDamage = MutableStat(0f)

    var maxAmmo = ModularStatInt(Int.MAX_VALUE)
    var ammoPerSecond = MutableStat(1f)
    var reloadSize = MutableStat(1f)

    var chargeUp = MutableStat(0f)
    var chargeDown = MutableStat(0.4f)
    var burstSize = ModularStatInt(1)
    var burstDelay = MutableStat(0.1f)

    var minSpread = MutableStat(1f)
    var maxSpread = MutableStat(4f)
    var spreadBuildup = MutableStat(1f)
    var spreadDecay = MutableStat(4f)

}