package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.lwjgl.util.vector.Vector2f

open class BaseFogTerrain : OldHyperspaceTerrainPlugin(), BiomeTerrain {

    var biomePlugin: BaseAbyssBiome? = null
    override fun getBiome(): BaseAbyssBiome? {
        return biomePlugin
    }

    override fun hasTooltip(): Boolean {
        return false
    }

   /* override fun containsEntity(other: SectorEntityToken?): Boolean {
        return false
    }

    override fun containsPoint(test: Vector2f?, r: Float): Boolean {
        return false
    }*/

    //Name and Speed boost handled by central terrain
    override fun getTerrainName(): String? {
        /* val player = Global.getSector().playerFleet
         val inCloud = this.isInClouds(player)

         var name = biomePlugin?.getDisplayName() ?: ""
         if (isInStorm(player)) return "$name (Storm)"
         if (inCloud) return "$name (Fog)"
         return name*/

        return null
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