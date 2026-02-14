package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.items.AbyssalSurveyData
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ShipAPI
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
import org.lwjgl.opengl.GL11
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
        tooltip.addPara("A small, exotic bubble within the otherwise ruthless depths of the abyss, bordering the hostile Abyssal Wastes. The matter colliding of your ship plays a calming harmony in its interiors.", 0f, Misc.getTextColor(), Color(150, 140, 140), "Abyssal Wastes")
    }

    override fun getGridAlphaMult(): Float {
        return 0.75f
    }

    override fun getSaturation(): Float {
        return 1.25f
    }

    override fun hasCombatNebula(): Boolean {
        return false
    }

    override fun getMusicKeyId(): String? {
        return null
    }

    //Do not let the normal generation handle this
    override fun shouldGenerateBiome(): Boolean {
        return false
    }

    //Should not be overwriteable by another minibiome
    override fun canBeOverwritten(): Boolean {
        return false
    }

    override fun isMainBiome(): Boolean {
        return false
    }

    //Place the biome between two other biomes
    override fun preDepthScan() {
        var manager = AbyssUtils.getBiomeManager()
        var picks = manager.getCells()/*.filter { it.getBiome() == null }*/
        //Do not let it spawn at the edges
        //var cell = picks.filter { it.gridX != 0 && it.gridX != AbyssBiomeManager.width && it.gridY != 0 && it.gridY != AbyssBiomeManager.height }.random()
        var cellsFull = picks.filter { it.gridX > 0 && it.gridX < manager.rows -1 && it.gridY > 0 && it.gridY < manager.columns -1 }

        //Only cells where any of their surrounding biome cells is not their own biome, and only on the border of the abyssal wastes
        var cells = cellsFull.filter { center -> center.getSurrounding().any { surrounding -> center.getBiome() != surrounding.getBiome() && !surrounding.isFake && surrounding.getBiome() is AbyssalWastes } }

        //Ensure it doesnt pick a biome border with any biome that can not be overwritten
        cells = cells.filter { center -> center.getSurrounding().none { it.getBiome()?.canBeOverwritten() == false} }

        var cell = cells.random()

        cell.setBiome(this)
        deepestCells.add(cell)

        for (surounding in cell.getSurrounding().shuffled()) {
            //if (surounding.getBiome() != null) continue
            if (surounding.isFake) continue
            surounding.setBiome(this)
        }
    }

    override fun preGenerate() {

    }

    override fun getBackgroundColor(): Color {
        return super.getBackgroundColor().darker()
    }

    override fun spawnDefenseFleet(source: SectorEntityToken, fpMult: Float): CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_abyssals", "",false)
        return fleet
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_ethereal_shores", "rat_terrain", "depths1", 0.65f)
        var fog = terrain as BaseFogTerrain
        var editor = OldNebulaEditor(fog)

        var system = AbyssUtils.getSystem()

        var center = deepestCells.first()




        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1.35f, AbyssBiomeManager.cellSize * 5f, 0f, 360f)

        //Make it look less circular

        var angle1 = MathUtils.getRandomNumberInRange(0f, 360f)
        var angle2 = MathUtils.getRandomNumberInRange(0f, 360f)

        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1f, AbyssBiomeManager.cellSize * 5f, angle1, angle1+80f)
        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 0.9f, AbyssBiomeManager.cellSize * 5f, angle2, angle2+50f)


        var pLoc = center!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.25f))

        var photosphere = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere_sierra", Factions.NEUTRAL)
        photosphere.setLocation(pLoc.x, pLoc.y)
        photosphere.radius = 100f

        majorLightsources.add(photosphere)

        var plugin = photosphere.customPlugin as AbyssalLight
        plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

        photosphere.sensorProfile = 1f

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
        raphael.addTag(AbyssalSurveyData.TAG_MAJOR_ENTITY)

        raphael.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), 280f, 90f)

        raphael.addTag("rat_abyss_sierra_raphael")

    }
}