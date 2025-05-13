package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class SeaOfSerenity() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_serenity"
    }

    override fun getDisplayName(): String {
        return "Sea of Serenity"
    }

    private var biomeColor = Color(255, 123, 0)
    private var darkBiomeColor = Color(77, 37, 0)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun getCombatNebulaTex() = "graphics/terrain/rat_combat/rat_combat_depths_serenity.png"
    override fun getCombatNebulaMapTex() = "graphics/terrain/rat_combat/rat_combat_depths_map_serenity.png"

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A dark, uninviting place. Analysis of distant sensor detections reveal more man-made structures than in other biomes of the abyss.")
    }

    override fun getMaxDarknessMult(): Float {
        return 0.6f
    }

    /** Called after all cells are generated */
    override fun init() {

        var system = AbyssUtils.getSystem()!!

        generateFogTerrain("rat_sea_of_serenity", "rat_terrain", "depths1", 0.6f)

        var photosphereNum = MathUtils.getRandomNumberInRange(12, 14)

        for (i in 0 until photosphereNum) {

            var cell: BiomeCellData? = pickAndClaimAdjacentOrSmaller() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_beacon${Misc.genUID()}", "Abyssal Beacon", "rat_abyss_beacon", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(10000f, 12500f)
            majorLightsources.add(entity)

            //Have some photospheres with cleared terrain, some not.
            if (Random().nextFloat() >= 0.5f) {
                AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(250f, 600f))
            }

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }

        var sensor = AbyssProcgenUtils.createSensorArray(system, this)
        var sphere = majorLightsources.randomOrNull()
        if (sphere != null) {
            sensor.setCircularOrbitWithSpin(sphere, MathUtils.getRandomNumberInRange(0f, 360f), sphere.radius + sensor.radius + MathUtils.getRandomNumberInRange(100f, 250f), 90f, -10f, 10f)
        }
    }


    override fun spawnDefenseFleet(source: SectorEntityToken, fpMult: Float): CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_abyssals", "",false)
        return fleet
    }

}