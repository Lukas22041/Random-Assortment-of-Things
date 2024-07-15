package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class SeaOfTranquillity : BaseAbyssBiome() {
    override fun getId(): String {
        return "sea_of_tranquillity"
    }

    override fun getName(): String {
        return "Sea of Tranquillity"
    }

    override fun getColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getLabelColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getLightColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getEnviromentColor(): Color {
        return Color(100, 0, 0)
    }

    override fun generate() {

    }


}