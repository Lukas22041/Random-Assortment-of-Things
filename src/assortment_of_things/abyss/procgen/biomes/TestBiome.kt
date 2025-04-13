package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class TestBiome(var id: String, var color: Color) : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return id
    }

    override fun getBiomeColor(): Color {
        return color
    }


}