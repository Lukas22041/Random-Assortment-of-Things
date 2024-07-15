package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class SeaOfSerenity : BaseAbyssBiome() {
    override fun getId(): String {
        return "sea_of_serenity"
    }

    override fun getName(): String {
        return "Sea of Serenity"
    }

    override fun getColor(): Color {
        return Color(252, 157, 3)
    }

    override fun getLabelColor(): Color {
        return Color(200, 90, 3)
    }

    override fun getLightColor(): Color {
        return Color(252, 137, 3)
    }

    override fun getEnviromentColor(): Color {
        return Color(200, 90, 3)
    }

    override fun generate() {

    }


}