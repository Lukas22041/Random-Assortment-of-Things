package assortment_of_things.abyss.procgen.biomes

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

}