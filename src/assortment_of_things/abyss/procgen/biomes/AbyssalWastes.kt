package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

//System with no fog, threat is rampant and its generaly very dark
class AbyssalWastes() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "abyssal_wastes"
    }

    override fun getDisplayName(): String {
        return "Abyssal Wastes"
    }

    override fun getBiomeColor(): Color {
        return Color(30, 30, 30)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(10, 10, 10)
    }

    override fun getGridAlphaMult(): Float {
        return 0.25f
    }

    /** Called after all cells are generated */
    override fun init() {
       //generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)
    }

}