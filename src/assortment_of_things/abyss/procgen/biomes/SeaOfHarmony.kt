package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

//System with a large Photosphere illuminating it
class SeaOfHarmony() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "sea_of_harmony"
    }

    override fun getDisplayName(): String {
        return "Sea of Harmony"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 64, 50)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(102, 25, 20)
    }

    /** Called after all cells are generated */
    override fun init() {
       generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)
    }

}