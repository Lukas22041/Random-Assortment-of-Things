package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

class OceanOfStorms : BaseAbyssBiome() {
    override fun getId(): String {
        return "ocean_of_storms"
    }

    override fun getName(): String {
        return "Ocean of Storms"
    }

    override fun getColor(): Color {
        return Color(235, 20, 120)
    }

    override fun getLabelColor(): Color {
        return Color(176, 0, 76)
    }

    override fun getLightColor(): Color {
        return Color(199, 2, 87)
    }

    override fun getEnviromentColor(): Color {
        return Color(155, 0, 67)
    }

    override fun generate() {

    }


}