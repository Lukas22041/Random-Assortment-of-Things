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
class PrimordialWaters() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "primordial_waters"
    }

    override fun getDisplayName(): String {
        return "Primordial Waters"
    }

    private var biomeColor = Color(140, 0, 250)
    private var darkBiomeColor = Color(44, 0, 77)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    override fun getMusicKeyId(): String? {
        return null
    }

    override fun getMaxDarknessMult(): Float {
        return 0.6f
    }

    //Do not let the normal generation handle this
    override fun shouldGenerateBiome(): Boolean {
        return false
    }

    //Should not be overwriteable by another minibiome
    override fun canBeOverwritten(): Boolean {
        return false
    }

    //Place the biome between two other biomes
    override fun preDepthScan() {
        var manager = AbyssUtils.getBiomeManager()
        var picks = manager.getCells()/*.filter { it.getBiome() == null }*/
        //Do not let it spawn at the edges
        //var cell = picks.filter { it.gridX != 0 && it.gridX != AbyssBiomeManager.width && it.gridY != 0 && it.gridY != AbyssBiomeManager.height }.random()
        var cellsFull = picks.filter { it.gridX > 0 && it.gridX < AbyssBiomeManager.rows -1 && it.gridY > 0 && it.gridY < AbyssBiomeManager.columns -1 }


        //Only cells where any of their surrounding biome cells is not their own biome,
        // and only on the border of the sea of tranquility and not the wastes
        var cells = cellsFull.filter { center -> center.getSurrounding().any {
            surrounding -> center.getBiome() != surrounding.getBiome() && !surrounding.isFake && surrounding.getBiome() is SeaOfTranquility } }

        cells = cells.filter { it.getAdjacent().none { it.getBiome() is AbyssalWastes } }

        //More generous search, in case it cant find a connection to the sea of tranquility without also being in the wastes
        if (cells.isEmpty()) {
            cells = cellsFull.filter { center -> center.getSurrounding().any {
                    surrounding -> center.getBiome() != surrounding.getBiome() && !surrounding.isFake && surrounding.getBiome() is SeaOfTranquility } }
        }

        var radius = 3

        //Ensure it doesnt pick a biome border with any biome that can not be overwritten
        cells = cells.filter { center -> center.getAround(radius).none { it.getBiome()?.canBeOverwritten() == false} }

        var cell = cells.random()

        cell.setBiome(this)
        deepestCells.add(cell)

        for (surounding in cell.getAround(radius).shuffled()) {
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

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_primordial_waters", "rat_terrain", "depths1", 0.65f)
        var fog = terrain as BaseFogTerrain
        var editor = OldNebulaEditor(fog)

        var system = AbyssUtils.getSystem()

        var center = deepestCells.first()


        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, 0f, AbyssBiomeManager.cellSize * 0.6f, 0f, 360f)


        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 2.35f, AbyssBiomeManager.cellSize * 5f, 0f, 360f)

        //Make it look less circular

        var angle1 = MathUtils.getRandomNumberInRange(0f, 360f)
        var angle2 = MathUtils.getRandomNumberInRange(0f, 360f)

        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 2f, AbyssBiomeManager.cellSize * 5f, angle1, angle1+80f)
        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1.9f, AbyssBiomeManager.cellSize * 5f, angle2, angle2+50f)


        var pLoc = center!!.getWorldCenter()

        var photosphere = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
        photosphere.setLocation(pLoc.x, pLoc.y)
        photosphere.radius = 100f

        majorLightsources.add(photosphere)

        var plugin = photosphere.customPlugin as AbyssalLight
        plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)


    }
}