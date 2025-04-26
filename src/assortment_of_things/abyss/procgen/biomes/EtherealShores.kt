package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost
import com.fs.starfarer.api.impl.campaign.ghosts.GBDartAround
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//Sierra Biome
class EtherealShores() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "ethereal_shores"
    }

    override fun getDisplayName(): String {
        return "Ethereal Shores"
    }

    private var biomeColor = Color(205,155,255)
    private var darkBiomeColor = Color(82, 62, 102)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    override fun getGridAlphaMult(): Float {
        return 0.75f
    }

    override fun getSaturation(): Float {
        return 1.25f
    }



    override fun getMusicKeyId(): String? {
        return null
    }

    //Do not let the normal generation handle this
    override fun shouldGenerateBiome(): Boolean {
        return false
    }

    override fun preGenerate() {

        var manager = AbyssUtils.getBiomeManager()
        var picks = manager.getCells().filter { it.getBiome() == null }
        //Do not let it spawn at the edges
        var cell = picks.filter { it.gridX != 0 && it.gridX != AbyssBiomeManager.width && it.gridY != 0 && it.gridY != AbyssBiomeManager.height }.random()

        cell.setBiome(this)
        deepestCells.add(cell)

        //var count = 0
        for (surounding in cell.getSurrounding().shuffled()) {
            //count++
            //if (count >= 6) break //Kind of just breaks without the 8 surrounding cells
            if (surounding.getBiome() != null) continue
            if (surounding.isFake) continue
            surounding.setBiome(this)
        }

        /*for (i in 0 until 2) {
            var surrounding = cell.getSurrounding().random()

            var toConvert = surrounding.getAdjacent().filter { it != cell && it.getBiome() == null && !it.isFake }.randomOrNull()
            toConvert?.setBiome(this)
        }*/

    }

    override fun getBackgroundColor(): Color {
        return super.getBackgroundColor().darker()
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_sea_of_ethereal_shores", "rat_terrain", "depths1", 0.75f)
        var fog = terrain as BaseFogTerrain
        var editor = OldNebulaEditor(fog)

        var system = AbyssUtils.getSystem()

        var center = deepestCells.first()

        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1.25f, AbyssBiomeManager.cellSize * 5f, 0f, 360f)


        var pLoc = center!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.25f))

        var photosphere = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere_sierra", Factions.NEUTRAL)
        photosphere.setLocation(pLoc.x, pLoc.y)
        photosphere.radius = 100f

        majorLightsources.add(photosphere)

        var plugin = photosphere.customPlugin as AbyssalLight
        plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)


        // sensor ghosts
        for (i in 0 until 3) {
            val g = BaseSensorGhost(null, 0)
            g.initEntity(g.genMediumSensorProfile(), g.genSmallRadius(), 0, system)
            g.addBehavior(GBDartAround(photosphere,
                9999f,
                8 + Misc.random.nextInt(4),
                photosphere.radius + 200f,
                2500f))
            g.despawnRange = -1f
            g.entity.addTag("sotf_AMDancingGhost")
            g.setLoc(Misc.getPointAtRadius(photosphere.location, 1200f))
            //g.placeNearEntity(tia.getHyperspaceAnchor(), 800, 3200);
            system.addScript(g)
        }

        val params = DerelictShipEntityPlugin.createVariant("rat_raphael_Hull", Random(), DerelictShipEntityPlugin.getDefaultSModProb())
        val raphael = BaseThemeGenerator.addSalvageEntity(Random(), system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
        raphael.setDiscoverable(true)

        raphael.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), 280f, 90f)

        raphael.addTag("rat_abyss_sierra_raphael")

    }
}