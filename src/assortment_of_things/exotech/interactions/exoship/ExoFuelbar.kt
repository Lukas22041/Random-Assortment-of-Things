package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11

class ExoFuelbar(var useage: Float, var current: Float, var max: Float, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var bar = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_fuelbar.png")
    var barFill = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_fuelbar_fill.png")
    var barFillFull = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_fuelbar_fill_dark.png")


    init {
        renderBorder = false
        renderBackground = false
        enableTransparency = true
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        bar.alphaMult = alphaMult
        bar.setSize(width, height)
        bar.render(x, y)

        current = MathUtils.clamp(current, 0f, 1f)

        startBarStencil(x, y, width, height, current)

        barFillFull.alphaMult = alphaMult
        barFillFull.setSize(width, height)
        barFillFull.render(x, y)

        endStencil()

        useage = MathUtils.clamp(useage, 0f, 1f)

        startBarStencil(x, y, width, height, useage)

        barFill.alphaMult = alphaMult
        barFill.setSize(width, height)
        barFill.render(x, y)

        endStencil()
    }

    fun startBarStencil(x: Float, y: Float, width: Float, height: Float, percent: Float) {
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

        GL11.glRectf(x, y, x + (width * percent), y + height)

        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>
    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}