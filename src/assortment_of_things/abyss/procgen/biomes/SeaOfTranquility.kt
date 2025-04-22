package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeCellData
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

//Starting Biome
class SeaOfTranquility() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_tranquility"
    }

    override fun getDisplayName(): String {
        return "Sea of Tranquility"
    }

    override fun getBiomeColor(): Color {
        return Color(255, 0, 50)
    }

    override fun getDarkBiomeColor(): Color {
        return Color(77, 0, 15)
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_sea_of_tranquility", "rat_terrain", "depths1", 0.6f)

        var system = AbyssUtils.getSystem()

        while (true) {

            var cell: BiomeCellData? = pickAndClaimSurrounding() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }
    }

}