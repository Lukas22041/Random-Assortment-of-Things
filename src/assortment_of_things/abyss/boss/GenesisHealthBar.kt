package assortment_of_things.abyss.boss

import assortment_of_things.misc.StateBasedTimer
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f

class GenesisHealthBar(var bossScript: GenesisBossScript, var ship: ShipAPI) : BaseEveryFrameCombatPlugin() {


    var mainHealthBarSprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_genesis_bar.png")
    var mainHealthBarFillSprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_genesis_bar_fill.png")
    var mainBarBlashSprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_genesis_bar_flashy.png")
    var mainBarLocked = Global.getSettings().getAndLoadSprite("graphics/ui/rat_genesis_bar_locked.png")



    var mainbarAlpha = 0f
    var mainBarMaximumProgress = 0f
    var lowestFlashbarPercent = 0f

    var lockAlpha = 0f

    var lastJitterlocations = ArrayList<Vector2f>()

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

        if (!Global.getCombatEngine().isPaused) {

            if (bossScript.transitionTimer.state == StateBasedTimer.TimerState.Out) {
                lockAlpha += 0.33f * (amount / Global.getCombatEngine().timeMult.modifiedValue)
            }
            if (bossScript.phase == GenesisBossScript.Phases.P3) {
                lockAlpha -= 3f * amount
            }
            lockAlpha = MathUtils.clamp(lockAlpha, 0f, 1f)

            if (!ship.isAlive) {
                mainbarAlpha -= 2f * amount
                mainbarAlpha = MathUtils.clamp(mainbarAlpha, 0f, 1f)
            }
            else if (bossScript.hasSeenBoss) {
                mainbarAlpha += 0.50f * amount
                mainBarMaximumProgress += 0.16f * amount

                mainbarAlpha = MathUtils.clamp(mainbarAlpha, 0f, 1f)
                mainBarMaximumProgress = MathUtils.clamp(mainBarMaximumProgress, 0f, 1f)
            }

            var percent = MathUtils.clamp(ship.hitpoints / ship.maxHitpoints, 0f, 1f)
            if (percent > lowestFlashbarPercent) {
                lowestFlashbarPercent = percent
            }
            else {
                //lowestFlashbarPercent -= 0.15f * amount
                lowestFlashbarPercent -= 0.125f * amount
            }
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        if (!Global.getCombatEngine().isUIShowingHUD) return

        var screenWidth = Global.getSettings().screenWidth
        var screenHeight = Global.getSettings().screenHeight

        var scale = Global.getSettings().screenScaleMult

        var width = 278 * scale
        var height = 34 * scale

        var posX = screenWidth / 2
        var posY = screenHeight - 40f

        mainHealthBarSprite.setSize(width, height)
        mainHealthBarSprite.alphaMult = mainbarAlpha
        mainHealthBarSprite.renderAtCenter(posX, posY)

        var mainBarPercent = ship.hitpoints / ship.maxHitpoints
        mainBarPercent = MathUtils.clamp(mainBarPercent, 0f, mainBarMaximumProgress)

        var flashBarPercent = lowestFlashbarPercent
        flashBarPercent = MathUtils.clamp(flashBarPercent, 0f, mainBarMaximumProgress)

        var stencilOffsetLeft = 43f * scale
        var stencilOffsetRight = 55f * scale

        startBarStencil(posX + stencilOffsetLeft - width / 2, posY - height, width - stencilOffsetRight, height * 2, flashBarPercent)


        mainBarBlashSprite.setSize(width, height)
        mainBarBlashSprite.alphaMult = mainbarAlpha
        mainBarBlashSprite.renderAtCenter(posX, posY)

        endStencil()

        startBarStencil(posX + stencilOffsetLeft - width / 2, posY - height, width - stencilOffsetRight, height * 2, mainBarPercent)

        mainHealthBarFillSprite.setNormalBlend()
        mainHealthBarFillSprite.setSize(width, height)
        mainHealthBarFillSprite.alphaMult = mainbarAlpha
        mainHealthBarFillSprite.renderAtCenter(posX, posY)

        doJitter(mainHealthBarFillSprite, mainbarAlpha * (1-lockAlpha), lastJitterlocations, 15, 10f, Vector2f(posX, posY))

        endStencil()

        mainBarLocked.setNormalBlend()
        mainBarLocked.setSize(width, height)
        mainBarLocked.alphaMult = lockAlpha
        mainBarLocked.renderAtCenter(posX, posY)


    }


    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
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

    fun doJitter(sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float, pos: Vector2f) {

        var paused = Global.getCombatEngine().isPaused
        var jitterAlpha = 0.1f

        if (!paused) {
            lastLocations.clear()
        }

        for (i in 0 until jitterCount) {

            var jitterLoc = Vector2f()

            if (!paused) {
                var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                jitterLoc = Vector2f(x, y)
                lastLocations.add(jitterLoc)
            }
            else {
                jitterLoc = lastLocations.getOrElse(i) {
                    Vector2f()
                }
            }

            sprite.setAdditiveBlend()
            sprite.alphaMult = level * jitterAlpha
            sprite.renderAtCenter(pos.x + jitterLoc.x, pos.y + jitterLoc.y)
        }
    }

}