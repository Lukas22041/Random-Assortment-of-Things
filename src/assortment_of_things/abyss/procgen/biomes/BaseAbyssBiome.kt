package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.procgen.BiomeCell
import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.Random

abstract class BaseAbyssBiome() {

    abstract fun getId() : String
    abstract fun getName() : String
    abstract fun getColor() : Color
    abstract fun getLabelColor() : Color
    abstract fun getLightColor() : Color
    abstract fun getEnviromentColor() : Color

    abstract fun generate()

    var labelAngle = MathUtils.getRandomNumberInRange(-12f, 12f)

    var cells: MutableList<BiomeCell> = ArrayList()
    lateinit var centralCell: BiomeCell

    init {
        labelAngle = MathUtils.getRandomNumberInRange(-4f, -13f)
        if (Random().nextFloat() >= 0.5f) {
            labelAngle = MathUtils.getRandomNumberInRange(4f, 13f)
        }
    }

}