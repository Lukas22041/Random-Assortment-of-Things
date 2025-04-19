package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import java.awt.Color

//Sierra Biome
class EtherealShores() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "ethereal_shores"
    }

    override fun getDisplayName(): String {
        return "Ethereal Shores"
    }

    override fun getBiomeColor(): Color {
        return Color(205,155,255)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(123, 93, 153)
    }

    override fun getGridAlphaMult(): Float {
        return 0.75f
    }

    override fun getSaturation(): Float {
        return 1.25f
    }

    override fun getParticleColor(): Color {
        return Color(205,155,255)
    }



    //Do not let the normal generation handle this
    override fun shouldGenerateBiome(): Boolean {
        return false
    }

    override fun preGenerate() {

        var manager = AbyssUtils.getBiomeManager()
        var cell = manager.getCells().filter { it.getBiome() == null }.random()

        cell.setBiome(this)
        deepestCells.add(cell)

        for (surounding in cell.getSurrounding()) {
            if (surounding.getBiome() != null) continue
            if (surounding.isFake) continue

            surounding.setBiome(this)
        }

        for (i in 0 until 3) {
            var surrounding = cell.getSurrounding().random()

            var toConvert = surrounding.getSurrounding().filter { it.getBiome() != null && !it.isFake }.randomOrNull()
            toConvert?.setBiome(this)
        }

    }


    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_sea_of_ethereal_shores", "rat_terrain", "depths1", 0.75f)
        var fog = terrain as BaseFogTerrain
        var editor = OldNebulaEditor(fog)

        var center = deepestCells.first()

        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1.25f, AbyssBiomeManager.cellSize * 5f, 0f, 360f)
    }

}