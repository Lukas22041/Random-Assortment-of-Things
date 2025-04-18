package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

//Starting Biome
class SeaOfTranquility() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_tranquility"
    }

    override fun getDisplayName(): String {
        return "Sea of Tranquility"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 0, 50)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(77, 0, 15)
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_sea_of_tranquility", "rat_terrain", "depths1", 0.6f)
    }

}