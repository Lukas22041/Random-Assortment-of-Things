package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color

//System with a large Photosphere illuminating it
class SeaOfHarmony() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "sea_of_harmony"
    }

    override fun getDisplayName(): String {
        return "Sea of Harmony"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 64, 50)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(102, 25, 20)
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    override fun getSaturation(): Float {
        return 1.1f
    }

    /** Called after all cells are generated */
    override fun init() {
        var system = AbyssUtils.getSystem()

        generateFogTerrain("rat_sea_of_harmony", "rat_terrain", "depths1", 0.6f)

        var sensor = AbyssProcgenUtils.createSensorArray(system!!, this)
        sensor.location.set(deepestCells.random().getWorldCenterWithCircleOffset(300f))
    }

}