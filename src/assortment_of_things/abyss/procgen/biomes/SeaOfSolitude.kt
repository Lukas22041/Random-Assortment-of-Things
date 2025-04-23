package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.misc.FlickerUtilV2Abyssal
import assortment_of_things.abyss.procgen.*
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

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

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Exotic matter moves violently and non-stop throughout the biome, leading to an enviroment that is constantly under the influence of charged particles. \n\n" +
                "" +
                "Extreme Storms, that span the entire biome, occur approximately every 3 days. Anything not hidden within the dense fog of the abyss will appear visible to the sensors of everything in its surroundings. ", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Extreme Storms", "3", "dense fog")
    }

    var photospheres = ArrayList<SectorEntityToken>()

    var stormHandler = SolitudeStormHandler(this)

    /** Called after all cells are generated */
    override fun init() {

        LunaCampaignRenderer.addRenderer(stormHandler)

        var system = AbyssUtils.getSystem()

        generateFogTerrain("rat_sea_of_solitude", "rat_terrain", "depths1", 0.5f)

        var photosphereNum = MathUtils.getRandomNumberInRange(12, 14)

        for (i in 0 until photosphereNum) {

            var cell: BiomeCellData? = pickAndClaimAdjacentOrSmaller() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

            photospheres.add(entity)

            //Have some photospheres with cleared terrain, some not.
            if (Random().nextFloat() >= 0.1f) {
                AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(550f, 1200f))
            }

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }
    }

    override fun advance(amount: Float) {

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
            if (cell.depth == BiomeDepth.BORDER) spread *= 0.9f
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 3.5f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.25f)

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

class SolitudeStormHandler(var solitude: SeaOfSolitude) : LunaCampaignRenderingPlugin {

    @Transient
    var thunder: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")

    override fun isExpired(): Boolean {
        return false
    }

    var stormSensorMultIncrease = 1.5f

    var stormInterval = IntervalUtil(27f, 33f)
    var stormDuration = 0f

    var flicker1 = FlickerUtilV2Abyssal(0.5f)
    var flicker2 = FlickerUtilV2Abyssal(0.7f)
    //var flicker2 = FlickerUtilV2(4f)

    init {
        flicker1.numBursts = 7
        flicker2.numBursts = 3
    }


    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()
        var cell = manager.getPlayerCell()
        var player = Global.getSector().playerFleet

        if (stormDuration <= 0 && dominant == solitude && cell.depth != BiomeDepth.BORDER) stormInterval.advance(amount) //Only advance if not storming
        if (stormInterval.intervalElapsed()) {
            stormDuration = MathUtils.getRandomNumberInRange(10f, 14f)
            stormInterval.advance(0f)
        }

        stormDuration -= 1 * amount

        //Only advance while in the biome, or if it has to finish flashing
        if (stormDuration <= 0f || dominant != solitude || cell.depth == BiomeDepth.BORDER) {
            flicker1.stopAll = true
            flicker2.stopAll = true
        } else {
            flicker1.stopAll = false
            flicker2.stopAll = false
        }

        flicker1.advance(amount * 0.15f)
        flicker2.advance(amount * 0.20f)

        var brightness = (flicker1.brightness * 0.7f) + (flicker2.brightness * 0.3f)
        if (brightness != 0f) {
            for (fleet in AbyssUtils.getSystem()!!.fleets) {
                var inClouds = (solitude.terrain as BaseFogTerrain).isInClouds(fleet)
                if (!inClouds) {
                    fleet.stats.addTemporaryModMult(0.1f, "rat_extreme_storm", "Extreme Storm", 1f + (stormSensorMultIncrease * brightness), fleet.stats.detectedRangeMod)
                }
            }
        }

        //flicker2.advance(amount)

        if (flicker1.isPeakFrame) {
            Global.getSoundPlayer().playSound("rat_abyss_solitude_storm_sounds", 0.8f, 1.5f, player.location, Vector2f())
        }
    }

    var layers = EnumSet.of(CampaignEngineLayers.TERRAIN_5)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layers
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        if (thunder == null) {
            thunder = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")
        }

        var llx = viewport.llx - 100
        var lly = viewport.lly - 100

        var width = viewport.visibleWidth + 200
        var height = viewport.visibleHeight + 200

        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.175f * flicker1.brightness /** flicker2.brightness*/
        thunder!!.color = solitude.getBiomeColor()
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)


        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.05f * flicker2.brightness /** flicker2.brightness*/
        thunder!!.color = Color(255, 220, 220)
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)
    }

}