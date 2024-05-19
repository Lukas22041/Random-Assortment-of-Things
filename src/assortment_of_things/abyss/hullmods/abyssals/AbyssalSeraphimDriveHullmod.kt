package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class AbyssalSeraphimDriveHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.sensorProfile.modifyMult(id, 0.5f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {

        Global.getCombatEngine().addLayeredRenderingPlugin(SeraphimDriveRenderer(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The ships drivesystem is connected to a unique type of phase-coil. It enables the ship to enter phase-space while being affected by abyssal phenonema. \n\n" +
                "Stacks of \"Saving Grace\" from the \"Seraphs Grace\" hullmod affect the amount of stress the ships coils can take. The speed reduction from phase coil stress is reduced by up to 50%% at 30 stacks.\n\n" +
                "All deployed fighters are also interconnected to this shipsystem and are forced in to phase-space whenever the ship is aswell, but only receive a fraction of the increase in timeflow. \n\n" +
                "The ships sensor profile is decreased by 50%%. ",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "Saving Grace", "Seraphs Grace", "speed reduction", "50%", "30", "deployed fighters", "50%")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }
    }

    override fun getDisplaySortOrder(): Int {
        return 0
    }


    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}

class SeraphimDriveRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

    var glow: SpriteAPI
    var additiveGlow: SpriteAPI
    var lastJitterLocations = ArrayList<Vector2f>()

    init {
        glow = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow1.png")
        additiveGlow = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow2.png")
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }
    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        var level = ship.phaseCloak.effectLevel
        var extraRange = 0f

        extraRange += 4 * ship.fluxLevel

        var outPercent = ship.phaseCloak.chargeDownDur / ship.phaseCloak.cooldown
        if (ship.phaseCloak.state == ShipSystemAPI.SystemState.OUT) {
            level = (1 - outPercent) + ship.phaseCloak.effectLevel * outPercent
            extraRange += 20 * (1-level)
        }
        if (ship.phaseCloak.state == ShipSystemAPI.SystemState.COOLDOWN) {
            var cooldownLevel = (ship.phaseCloak.cooldownRemaining - 0f) / (ship.phaseCloak.cooldown - 0f)
            level = cooldownLevel * (1 - outPercent)
            extraRange += 20 * (1-level)
        }

        level = easeOutSine(level)

        ship.setCustomData("rat_phase_level", level)

        glow.alphaMult = level
        glow.setNormalBlend()
        glow.angle = ship.facing - 90f
        glow.color = Color(255, 255, 255)
        glow.renderAtCenter(ship.location.x, ship.location.y)

        additiveGlow.alphaMult = level
        additiveGlow.setAdditiveBlend()
        additiveGlow.angle = ship.facing - 90f
        additiveGlow.color = Color(255, 255, 255)

        doJitter(ship, additiveGlow, 0.25f * level, lastJitterLocations, 5, 6f + extraRange)

    }


    fun easeOutSine(x: Float): Float {
        return (Math.sin((x * Math.PI) / 2)).toFloat();

    }

    fun doJitter(ship: ShipAPI, sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getCombatEngine().isPaused
        var jitterAlpha = 0.2f


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
            sprite.renderAtCenter(ship.location.x + jitterLoc.x, ship.location.y + jitterLoc.y)
        }
    }

}