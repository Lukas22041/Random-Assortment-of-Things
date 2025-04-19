package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.procgen.BiomeParticleManager
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

//System with no fog, threat is rampant and its generaly very dark
class AbyssalWastes() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "abyssal_wastes"
    }

    override fun getDisplayName(): String {
        return "Abyssal Wastes"
    }

    override fun getBiomeColor(): Color {
        return Color(30, 30, 30)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(10, 10, 10)
    }

    override fun getGridAlphaMult(): Float {
        return 0.25f
    }

    override fun getSaturation(): Float {
        return 0.75f
    }

    override fun getParticleColor(): Color {
        return Color(168, 146, 145)
    }

    /** Called after all cells are generated */
    override fun init() {
       //generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)
    }

    override fun spawnParticlesForCell(particleManager: BiomeParticleManager, cell: BiomeCellData) {
        //super.spawnParticlesForCell(particleManager, cell) //Replace the particle spawner

        var count = 3
        var fadeInOverwrite = false
        if (particleManager.particles.size <= 50) {
            count *= 4
            fadeInOverwrite = true
        }

        for (i in 0 until count) {
            var velocity = Vector2f(0f, 0f)
            velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(100f, 150f), MathUtils.getRandomNumberInRange(0f, 360f)))

            var color = getParticleColor()

            //var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
            var spawnLocation = cell.getWorldCenter()

            var spread = AbyssBiomeManager.cellSize * 0.75f
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 3f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 5f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 3f)

            var size = MathUtils.getRandomNumberInRange(25f, 45f)

            var alpha = MathUtils.getRandomNumberInRange(0.15f, 0.25f)

            particleManager.particles.add(BiomeParticleManager.AbyssalLightParticle(
                this,
                fadeIn,duration, fadeOut,
                color, alpha, size, spawnLocation, velocity,
                IntervalUtil(0.5f, 0.75f), MathUtils.getRandomNumberInRange(-5f, 5f), -20f, 20f))
        }
    }

}