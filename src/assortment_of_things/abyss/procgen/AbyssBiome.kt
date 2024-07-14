package assortment_of_things.abyss.procgen

import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.Random

data class AbyssBiome(
    var id: String,
    var name: String,
    var color: Color, var labelColor: Color) {

    var labelAngle = MathUtils.getRandomNumberInRange(-12f, 12f)

    var cells: MutableList<BiomeCell> = ArrayList()
    lateinit var centralCell: BiomeCell

    init {
        labelAngle = MathUtils.getRandomNumberInRange(-6f, -12f)
        if (Random().nextFloat() >= 0.5f) {
            labelAngle = MathUtils.getRandomNumberInRange(6f, 12f)
        }
    }

}