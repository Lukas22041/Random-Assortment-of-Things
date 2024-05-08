package assortment_of_things.abyss.scripts.fleet_generation

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.procgen.AbyssGenerator
import assortment_of_things.abyss.procgen.AbyssSystemData
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils

abstract class BaseAbyssFleetGen(var data: AbyssSystemData) {

    abstract fun spawnFleet()

    abstract fun getAvailableVariants()

    abstract fun applyAICores()

    fun sortForSeraphs() {

    }

    fun applyAlterations() {

    }

    fun getStrengthLevelForStep() : Float {
        var level = data.step.toFloat() / (AbyssGenerator.systemsOnMainBranch - 1f)
        return level
    }

    fun getMinPointsForDepth() : Float {
        var minPoints = 0f
        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep()

        minPoints = 84f
        if (difficulty == AbyssDifficulty.Hard) minPoints += 50f
        minPoints += 64 * level

        return minPoints
    }

    fun getMMaxPointsForDepth() : Float {
        var maxPoints = 0f
        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep()

        maxPoints = 138f
        if (difficulty == AbyssDifficulty.Hard) maxPoints += 70f
        maxPoints += 64 * level

        return maxPoints
    }

    fun getFleetScalingPoints(points: Float) : Float {

        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep()
        var depth = data.depth

        if (difficulty == AbyssDifficulty.Hard)
        {
            var playerFleet = Global.getSector().playerFleet
            var playerFP = playerFleet.fleetPoints.toFloat()
            var scalingPointsCap = 250f

            if (points < playerFP) {

                var difference = playerFP - points

                var scalingMult = when(depth) {
                    AbyssDepth.Shallow -> 0.45f
                    AbyssDepth.Deep -> 0.5f
                }

                var pointsForScaling = difference * scalingMult
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, playerFP)
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, scalingPointsCap)

                var adjusted = (pointsForScaling * 0.5f) + (pointsForScaling * 0.5f * level)

                return pointsForScaling
            }
        }

        return 0f
    }

    fun generateFleetPoints() : Float {
        var min = getMinPointsForDepth()
        var max = getMMaxPointsForDepth()
        var result = MathUtils.getRandomNumberInRange(min, max)

        result += getFleetScalingPoints(result)
        return result
    }
}