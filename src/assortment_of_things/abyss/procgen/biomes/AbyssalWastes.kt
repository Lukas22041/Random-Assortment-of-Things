package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.procgen.BiomeParticleManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//System with no fog, threat is rampant and its generaly very dark
class AbyssalWastes() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "abyssal_wastes"
    }

    override fun getDisplayName(): String {
        return "Abyssal Wastes"
    }

    private var biomeColor = Color(30, 30, 30)
    private var darkBiomeColor = Color(10, 10, 10)
    private var tooltipColor = Color(150, 140, 140)
    private var systemLightColor = Color(25, 25, 25)
    private var particleColor = Color(168, 146, 145)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun getTooltipColor(): Color {
        return tooltipColor
    }

    override fun getSystemLightColor(): Color {
        return systemLightColor
    }

    override fun getParticleColor(): Color {
        return particleColor
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    override fun getGridAlphaMult(): Float {
        return 0.25f
    }

    override fun getSaturation(): Float {
        return 0.75f
    }

    override fun getMaxDarknessMult(): Float {
        return 0.5f
    }



    override fun getMusicKeyId(): String {
        return "music_campaign_abyssal"
    }

    /** Called after all cells are generated */
    override fun init() {

        var system = AbyssUtils.getSystem()

       //generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)


        var photosphereNum = MathUtils.getRandomNumberInRange(10, 13)

        for (i in 0 until photosphereNum) {

            //Unlike other biomes, try to force more spacing
            var cell: BiomeCellData? = pickAndClaimSurroundingOrSmaller() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_decyaing_photosphere_${Misc.genUID()}", "Decaying Photosphere", "rat_abyss_decaying_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

            majorLightsources.add(entity)

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }


        var sensor = AbyssProcgenUtils.createDecayingSensorArray(system!!, this)
        sensor.location.set(deepestCells.random().getWorldCenterWithCircleOffset(300f))

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