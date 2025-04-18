package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.procgen.BiomeParticleManager
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SeaOfSolitude() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_solitude"
    }

    override fun getDisplayName(): String {
        return "Sea of Solitude"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 0, 100)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(77, 0, 31)
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)
    }

    override fun spawnParticlesForCell(particleManager: BiomeParticleManager, cell: BiomeCellData) {
        //super.spawnParticlesForCell(particleManager, cell)

        var count = 7
        var fadeInOverwrite = false
        if (particleManager.particles.size <= 50) {
            count *= 4
            fadeInOverwrite = true
        }

        for (i in 0 until count) {
            var velocity = Vector2f(0f, 0f)
            velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

            var color = getParticleColor()

            //var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
            var spawnLocation = cell.getWorldCenter()

            var spread = AbyssBiomeManager.cellSize * 1f/** 0.75f*/
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 4f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.5f)

            var size = MathUtils.getRandomNumberInRange(25f, 50f)

            var alpha = MathUtils.getRandomNumberInRange(0.25f, 0.45f)

            particleManager.particles.add(BiomeParticleManager.AbyssalLightParticle(
                this,
                fadeIn,duration, fadeOut,
                color, alpha, size, spawnLocation, velocity,
                IntervalUtil(0.5f, 0.75f), MathUtils.getRandomNumberInRange(-1f, 1f), -1f, -1f))
        }
    }

}