package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class SeaOfSerenity() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_serenity"
    }

    override fun getDisplayName(): String {
        return "Sea of Serenity"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 123, 0)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(77, 37, 0)
    }

    override fun getMaxDarknessMult(): Float {
        return 0.6f
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_sea_of_serenity", "rat_terrain", "depths1", 0.6f)
    }

}