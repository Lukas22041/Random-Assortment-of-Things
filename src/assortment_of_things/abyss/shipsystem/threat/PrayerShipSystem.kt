package assortment_of_things.abyss.shipsystem.threat

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.PlasmaJetsStats
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import java.awt.Color
import java.util.*

class PrayerShipSystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.15f, 0.15f)

    var addedRenderer = false

    var fireInterval = IntervalUtil(0.3f, 0.3f)


    var activated = false

    override fun apply(stats: MutableShipStatsAPI?, id: String,  state: ShipSystemStatsScript.State?,  effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity
        if (ship !is ShipAPI) return
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        var system = ship.system

        if (!addedRenderer) {
            addedRenderer = true
            Global.getCombatEngine().addLayeredRenderingPlugin(PrayerGlowRenderer(ship))
        }

        if (!activated && system.isActive) {
            activated = true
            ship.mass *= 2
        }

        if (!system.isActive && activated) {
            activated = false
            ship.mass /= 2
        }

        //Extra AI, aside from just the base burn drive one.
        var flags = ship.aiFlags
        if (flags.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET)) {
            var distance = flags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET) as Float
            var swarm = RoilingSwarmEffect.getSwarmFor(ship)
            if (swarm.numActiveMembers >= 25 && distance >= 800f && !system.isCoolingDown && !system.isActive && !flags.hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF)) {
                ship.useSystem()
            }
        }

        var color = Color(196, 20, 35, 255)

        if (system.isActive) {


            if (system.state == ShipSystemAPI.SystemState.IN || system.effectLevel >= 0.5f) {
                ship.giveCommand(ShipCommand.ACCELERATE, null, 0)
                ship.blockCommandForOneFrame(ShipCommand.DECELERATE)
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)
            }

            //Slower firing rate during fade in
            fireInterval.advance(Global.getCombatEngine().elapsedInLastFrame*effectLevel)
            if (fireInterval.intervalElapsed()) {
                /*var weapon = Global.getCombatEngine().createFakeWeapon(ship, "rat_prayer_missile_wep")
                Global.getCombatEngine().spawnProjectile(ship, weapon, "rat_prayer_missile_wep", "rat_prayer_missile_proj", ship.location, ship.facing, ship.velocity)*/
            }

        }

        var speed = 125f

        stats.maxSpeed.modifyFlat(id, speed * effectLevel)
        stats.acceleration.modifyPercent(id, speed * 1f * effectLevel)
        stats.deceleration.modifyPercent(id, speed * 1f * effectLevel)

        stats.turnAcceleration.modifyMult(id, 1 - (0.6f * effectLevel))
        stats.maxTurnRate.modifyMult(id, 1 - (0.6f * effectLevel))


        ship.engineController.fadeToOtherColor(this, color, color, 1f * effectLevel, 0.7f)
        ship.engineController.extendFlame(this, 0.5f * effectLevel, 2.5f * effectLevel, 0f)
    }


    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        return super.isUsable(system, ship) && ship!!.fullTimeDeployed >= 0.33f
    }

    class PrayerGlowRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ships/rat_prayer_glow.png")

        override fun getRenderRadius(): Float {
            return 100000f
        }

        var layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return layers
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

            sprite.angle = ship.facing -90f
            sprite.alphaMult = ship.system.effectLevel
            sprite.setNormalBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)

            sprite.alphaMult = ship.system.effectLevel * 0.5f
            sprite.setAdditiveBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)
        }
    }
}