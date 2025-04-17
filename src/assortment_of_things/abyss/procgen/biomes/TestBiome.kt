package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.terrain.TestAbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class TestBiome(var id: String, var color: Color, var darkColor: Color, var generateTerrain: Boolean) : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return id
    }

    override fun getBiomeColor(): Color {
        return color
    }

    override fun getDarkBiomeColor(): Color {
        return darkColor
    }

    /** Called after all cells are generated */
    override fun init() {

        if (generateTerrain) generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1")

    }


}