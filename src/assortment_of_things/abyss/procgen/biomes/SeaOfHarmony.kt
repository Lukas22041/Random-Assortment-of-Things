package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//System with a large Photosphere illuminating it
class SeaOfHarmony() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "sea_of_harmony"
    }

    override fun getDisplayName(): String {
        return "Sea of Harmony"
    }

    private var biomeColor = Color(255, 64, 50)
    private var darkBiomeColor = Color(102, 25, 20)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun getCombatNebulaTex() = "graphics/terrain/rat_combat/rat_combat_depths_harmony.png"
    override fun getCombatNebulaMapTex() = "graphics/terrain/rat_combat/rat_combat_depths_map_harmony.png"

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A biome indiscrible with any other word than \"warm\". Colossal photospheres are spread throughout, illuminating almost every location within.", 0f)
    }

    override fun getVignetteLevel(): Float {
        return 0.5f
    }

    override fun getSaturation(): Float {
        return 1.1f
    }

    /** Called after all cells are generated */
    override fun init() {
        var system = AbyssUtils.getSystem()

        generateFogTerrain("rat_sea_of_harmony", "rat_terrain", "depths1", 0.6f)

        var photosphereNum = MathUtils.getRandomNumberInRange(8, 8)

        //Spawn an even larger colossal first.
        var first = true
        for (i in 0 until photosphereNum) {

            //Claim all cells around a larger pattern
            var cell: BiomeCellData? = null

            if (first) {
                cell = pickAndClaimDeep() ?: break
                cell!!.getAround(3).forEach {
                    it.claimed = true
                }
            } else {
                cell = pickAndClaimAroundNoOtherBiome(3) ?: break
            }

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_colossal_photosphere_${Misc.genUID()}", "Colossal Photosphere", "rat_abyss_colossal_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 600f
            if (first) entity.radius += 400f

            var plugin = entity.customPlugin as AbyssalLight
            var lightRadius = MathUtils.getRandomNumberInRange(entity.radius + 42500f, entity.radius + 45000f)
            if (first) lightRadius += 4000f
            plugin.radius = lightRadius

            majorLightsources.add(entity)

            //Have some photospheres with cleared terrain, some not.
            AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(entity.radius + 200f, entity.radius + 1200f))

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/

            if (first) {
                entity.addTag("rat_supersized_colossal")
            }

            first = false
        }

        var sensor = AbyssProcgenUtils.createSensorArray(system!!, this)
        var sphere = majorLightsources.randomOrNull()
        if (sphere != null) {
            sensor.setCircularOrbitWithSpin(sphere, MathUtils.getRandomNumberInRange(0f, 360f), sphere.radius + sensor.radius + MathUtils.getRandomNumberInRange(100f, 250f), 90f, -10f, 10f)
        }
    }

}