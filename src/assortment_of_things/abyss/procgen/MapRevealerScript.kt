package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.SeaOfHarmony
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.util.IntervalUtil

class MapRevealerScript(var biomeManager: AbyssBiomeManager) : EveryFrameScript {

    init {
        Global.getSector().addScript(this)
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    companion object {
        var revealDelay = 0f
        var revealSpeedMult = 1f
    }

    var interval = IntervalUtil(0.25f, 0.4f)

    override fun advance(amount: Float) {

        /*var biome = biomeManager.getBiome("rat_test1")
        for (cell in biome.cells) {
            cell.isDiscovered = true
        }*/

        if (revealDelay > 0) {
            revealDelay -= 1 * amount
            return
        }

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            if (Global.getSector()?.playerFleet?.containingLocation == AbyssUtils.getData().system) {
                var playercell = biomeManager.getPlayerCell()
                playercell.isDiscovered = true

                var rad = 3
                //if (playercell.depth != BiomeDepth.BORDER && playercell.getBiome() is SeaOfHarmony) rad = 4 //Larger reveal radius in sea of harmony
                playercell.getAround(rad).forEach {
                    it.isDiscovered = true
                    it.getAdjacent().forEach { it.isPartialyDiscovered = true }
                }
            }
        }

        if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.MAP || RATSettings.enableMinimap!!) {
            for (cell in biomeManager.getCells()) {
                if (cell.isDiscovered) {
                    if (cell.discoveryFader >= 0) {
                        cell.discoveryFader -= 0.75f * amount * revealSpeedMult
                    }
                }

                if (cell.isPartialyDiscovered) {
                    if (cell.partialDiscoveryFader >= 0) {
                        cell.partialDiscoveryFader -= 0.75f * amount * revealSpeedMult
                    }
                }
            }
        }
    }



    /*//Test first, if theres performance issues, only place a stencil at the intersection of 4 cells, make only every 2nd cell responsible to stencil
    fun startCellStencil(factor: Float) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>


       *//* var loc = cell.getWorldCenter()
        val x = location
        val y = ship.location.y*//*

        var radius = AbyssBiomeManager.cellSize * factor
        var points = 50

        var devmode = Global.getSettings().isDevMode
        var cells = biomeManager.getCells()
        for (cell in cells) {
            if (cell.isDiscovered || devmode) {

                GL11.glBegin(GL11.GL_POLYGON) // Middle circle


                var loc = cell.getWorldCenter()

                for (i in 0..points) {

                    val angle: Double = (2 * Math.PI * i / points)
                    val vertX: Double = Math.cos(angle) * (radius)
                    val vertY: Double = Math.sin(angle) * (radius)
                    GL11.glVertex2d((loc.x + vertX) * factor, (loc.y + vertY) * factor)
                }

                GL11.glEnd()
1
            }
        }






        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endCellStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
*/



}