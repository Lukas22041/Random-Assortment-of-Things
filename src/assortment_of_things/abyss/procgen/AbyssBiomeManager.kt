package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssBiomeManager {


    var cellSize = 2800f

    var mapHorizontalSize = 56000 * 2
    var mapVerticalSize = 56000

    var cellCountHorizontal = (mapHorizontalSize / cellSize).toInt()
    var cellCountVertical = (mapVerticalSize / cellSize).toInt()

    var biomes = listOf(
        AbyssBiome("biome1", "", Color(200, 0, 0)),
        AbyssBiome("biome2", "", Color(155, 0, 0)),
        AbyssBiome("biome3", "Abyssal Wastes", Color(40, 0, 0)),
        AbyssBiome("biome4", "", Color(150, 0, 150)),
        AbyssBiome("biome5", "", Color(120, 56, 13))
    )

    /*var biomes = listOf(
        Color(200, 0, 0),
        Color(155, 0, 0),
        Color(40, 0, 0),
        Color(150, 0, 150),
        Color(120, 56, 13),
    )*/

    var grid: Array<Array<BiomeCell>> = Array(cellCountHorizontal) { x ->
        Array(cellCountVertical) {y ->
            BiomeCell(x, y, cellSize)
        }
    }

    var cells: List<BiomeCell> = grid.flatMap { it.asList() }

    fun generate() {
        var data = AbyssUtils.getAbyssData()

        for (biome in biomes) {

        }

    }


    fun getPlayerCell() : BiomeCell {
        var playerfleet = Global.getSector().playerFleet


        var horOffset = mapHorizontalSize / 2
        var verOffset = mapVerticalSize / 2

        var loc = playerfleet.location
        var x = ((loc.x+horOffset) / cellSize).toInt()
        var y = ((loc.y + verOffset) / cellSize).toInt()

        if (x in 0 until cellCountHorizontal && y in 0 until cellCountVertical) {
            return grid[x][y]
        }

        var fakeCell = BiomeCell(0, 0, 0f)
        fakeCell.isFake = true

        return fakeCell
    }


}