package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import org.lwjgl.util.vector.Vector2f

class AbyssBiomeManager {


    var cellSize = 2800f

    var mapHorizontalSize = 56000 * 2
    var mapVerticalSize = 56000

    var cellCountHorizontal = (mapHorizontalSize / cellSize).toInt()
    var cellCountVertical = (mapVerticalSize / cellSize).toInt()


    var grid: Array<Array<BiomeCell>> = Array(cellCountHorizontal) { x ->
        Array(cellCountVertical) {y ->
            BiomeCell(x, y, cellSize)
        }
    }

    fun generate() {
        var data = AbyssUtils.getAbyssData()
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