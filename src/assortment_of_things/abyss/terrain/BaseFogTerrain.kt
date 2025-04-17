package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin

open class BaseFogTerrain : OldHyperspaceTerrainPlugin(), BiomeTerrain {

    var biomePlugin: BaseAbyssBiome? = null
    override fun getBiome(): BaseAbyssBiome? {
        return biomePlugin
    }

    fun save()
    {
        params.tiles = null
        savedTiles = encodeTiles(tiles)

        savedActiveCells.clear()

        for (i in activeCells.indices) {
            for (j in activeCells[0].indices) {
                val curr = activeCells[i][j]
                if (curr != null && isTileVisible(i, j)) {
                    savedActiveCells.add(curr)
                }
            }
        }
    }

}