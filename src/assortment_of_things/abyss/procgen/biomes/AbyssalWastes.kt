package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class AbyssalWastes : BaseAbyssBiome() {
    override fun getId(): String {
        return "abyssal_wastes"
    }

    override fun getName(): String {
        return "Abyssal Wastes"
    }

    override fun getColor(): Color {
        return Color(40, 0, 0)
    }

    override fun getLabelColor(): Color {
        return Color(100, 0, 0)
    }

    override fun getLightColor(): Color {
        return Color(140, 0, 0)
    }

    override fun getEnviromentColor(): Color {
        return Color(40, 0, 0)
    }

    override fun generate() {

        //Clear Biome of any Nebula here


    }


}