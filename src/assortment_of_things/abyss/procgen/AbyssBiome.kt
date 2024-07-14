package assortment_of_things.abyss.procgen

import java.awt.Color

data class AbyssBiome(
    var id: String,
    var name: String,
    var color: Color) {

    var cells: List<BiomeCell> = ArrayList()

}