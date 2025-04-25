package assortment_of_things.abyss.procgen.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

class MiscAbyssScript : EveryFrameScript {

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }


    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        var manager = AbyssUtils.getBiomeManager()
        var levels = manager.getBiomeLevels()

        var playerFleet = Global.getSector().playerFleet

        var sensorStrength = 1f
        for ((biome, level) in levels) {
            if (biome.isSensorRevealed) {
                sensorStrength += 1f * level
            }
        }

        if (sensorStrength > 1f) {
            playerFleet.stats.addTemporaryModMult(0.1f, "rat_sensor_array_str", "Sensor Array Data", sensorStrength, playerFleet.stats.sensorRangeMod)
        }

    }
}