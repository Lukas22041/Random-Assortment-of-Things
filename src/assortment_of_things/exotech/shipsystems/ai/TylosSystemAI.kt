package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f

class TylosSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var interval = IntervalUtil(3f, 3f)
    var enoughTimePassedInterval = IntervalUtil(25f, 30f)
    var enoughTimePassed = false

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        if (ship!!.system.isActive) return

        var other = ship!!.customData.get("rat_tylos_parent") as ShipAPI?
        if (other == null) other = ship!!.customData.get("rat_tylos_child") as ShipAPI?
        if (other == null) return

        var wantsToSwitch = false

        var stats = ship!!.mutableStats
        var otherStats = other!!.mutableStats

        var hasSO = ship!!.variant.hasHullMod(HullMods.SAFETYOVERRIDES)
        var otherHasSO = other!!.variant.hasHullMod(HullMods.SAFETYOVERRIDES)

        if (target != null) {
            //Make it switch if other ship would be in range but itself isnt
            var isInRange = isInRange(ship!!, target)
            var isOtherInRange = isInRange(other, target)
            if (!isInRange && isOtherInRange) {
                wantsToSwitch = true
            }



            //If high on flux or low on health, switch if it helps escape or remove flux
            //Max-Speed is bugged if the ship isnt deployed var otherIsFaster = ship!!.maxSpeed + 10 < other.maxSpeed
            var otherIsBetterDiss = stats.fluxDissipation.modifiedValue + 20 < otherStats.fluxDissipation.modifiedValue
            if ((ship!!.fluxLevel >= 0.5f || ship!!.hitpoints <= ship!!.maxHitpoints * 0.3f) && (/*otherIsFaster || */otherIsBetterDiss)) {
                wantsToSwitch = true
            }



            if (!isInRange && !isOtherInRange) {
                if ((!hasSO && otherHasSO)) {
                    wantsToSwitch = true
                }
            }

            //Switch to Ship with more High-Explosive damage if there is atleast a 50% difference in HE damage between the two.
            if (isOtherInRange && (target.shield == null || target.shield.isOff || target.shield.arc <= 30f || target.fluxTracker.isOverloaded || target.fluxTracker.isVenting || target.fluxTracker.fluxLevel >= 0.7f)) {
                var HEonShip = ship!!.allWeapons.filter { it.damageType == DamageType.HIGH_EXPLOSIVE }.sumOf { it.damage.damage.toInt() }
                var HEonOther = other!!.allWeapons.filter { it.damageType == DamageType.HIGH_EXPLOSIVE }.sumOf { it.damage.damage.toInt() }
                if (HEonShip * 1.5f < HEonOther) {
                    wantsToSwitch = true
                }
            }
            //Switch to ship with more kinetic damage if there is atleast a 50% increase in kinetic damage.
            else if (target.fluxLevel <= 0.5f && target.shield?.isOn == true) {
                var KIonShip = ship!!.allWeapons.filter { it.damageType == DamageType.KINETIC }.sumOf { it.damage.damage.toInt() }
                var KIonOther = other!!.allWeapons.filter { it.damageType == DamageType.KINETIC }.sumOf { it.damage.damage.toInt() }
                if (KIonShip * 1.5f < KIonOther) {
                    wantsToSwitch = true
                }
            }

            //Dont even consider switching for the other conditions if the other ship wouldnt be in range and it doesnt wanna swap yet
            if (!wantsToSwitch && isInRange && !isOtherInRange) {
                resetInterval()
                return
            }

            //Switch on an interval randomly if both are in range
            enoughTimePassedInterval.advance(amount)
            if (enoughTimePassedInterval.intervalElapsed()) enoughTimePassed = true
            if (enoughTimePassed) wantsToSwitch = true

        } else {
            if (!hasSO && otherHasSO) {
                wantsToSwitch = true
            }
        }

        //Switch if running out of PPT
        if (ship!!.peakTimeRemaining <= 1) wantsToSwitch = true
        if (ship!!.currentCR <= 0.4f) wantsToSwitch = true

        //Dont ever switch if the other ship ran out of PPT
        if (other.peakTimeRemaining <= 1) wantsToSwitch = false
        if (other!!.currentCR <= 0.4f) wantsToSwitch = false

        //interval has to be advancing constantly, resets if none of the conditions makes the ship want to switch
        if (!wantsToSwitch) {
            resetInterval()
            return
        }

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            resetInterval()
            ship!!.useSystem()
            enoughTimePassedInterval = IntervalUtil(25f, 30f)
            enoughTimePassed = false
        }
    }

    fun resetInterval() {
        interval = IntervalUtil(3f, 3f)
    }

    fun isInRange(ship: ShipAPI, target: ShipAPI) : Boolean {
        var range = 0f
        for (weapon in ship.allWeapons) {
            if (weapon.range > range)
            {
                range = weapon.range
            }
        }
        var distance = MathUtils.getDistance(ship, target)
        var inRange = distance <= range + 100 //+100 to give it a bit of rest room
        return inRange
    }

}