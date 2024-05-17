package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalSeraphsGrace
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import com.fs.starfarer.api.util.IntervalUtil
import org.magiclib.plugins.MagicTrailPlugin
import java.awt.Color

class SeraphimDriveShipsystem : BaseShipSystemScript() {

    var JITTER_COLOR = Color(255, 175, 255, 255)
    var JITTER_FADE_TIME = 0.5f

    var SHIP_ALPHA_MULT = 0.25f

    var VULNERABLE_FRACTION = 0f
    var INCOMING_DAMAGE_MULT = 0.25f

    var MAX_TIME_MULT = 3f

    var FLUX_LEVEL_AFFECTS_SPEED = true
    //var MIN_SPEED_MULT = 0.75f
    var MIN_SPEED_MULT = 0.45f
    var MIN_SPEED_MULT_BASE = 0.45f
    var BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f

    protected var STATUSKEY1 = Any()
    protected var STATUSKEY2 = Any()
    protected var STATUSKEY3 = Any()
    protected var STATUSKEY4 = Any()

    var trailID = MagicTrailPlugin.getUniqueID()
    var interval = IntervalUtil(0.05f, 0.05f)

    fun getMaxTimeMult(stats: MutableShipStatsAPI): Float {
        return 1f + (MAX_TIME_MULT - 1f) * stats.dynamic.getValue(Stats.PHASE_TIME_BONUS_MULT)
    }

    protected fun isDisruptable(cloak: ShipSystemAPI): Boolean {
        return cloak.specAPI.hasTag(Tags.DISRUPTABLE)
    }

    protected fun getDisruptionLevel(ship: ShipAPI?): Float {
        //return disruptionLevel;
        //if (true) return 0f;
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            val threshold = ship!!.mutableStats.dynamic.getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD)
                .computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED)
            if (threshold <= 0) return 1f
            var level = ship.hardFluxLevel / threshold
            if (level > 1f) level = 1f

            var grace = ship.getListeners(AbyssalSeraphsGrace.SeraphsGraceListener::class.java).firstOrNull()
            if (grace != null) {
                var stacks = grace.stacks
                var graceLevel = stacks.count() / 30f
                graceLevel *= 0.5f
                graceLevel = 1 - graceLevel
                graceLevel = graceLevel.coerceIn(0f, 1f)
                level *= graceLevel
            }

            return level
        }
        return 0f
    }

    protected fun maintainStatus(playerShip: ShipAPI?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        val f = VULNERABLE_FRACTION
        var cloak = playerShip!!.phaseCloak
        if (cloak == null) cloak = playerShip.system
        if (cloak == null) return
        if (effectLevel > f) {
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
//					cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "can not be hit", false);
            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                cloak.specAPI.iconSpriteName,
                cloak.displayName,
                "time flow altered",
                false)
        } else {
//			float INCOMING_DAMAGE_MULT = 0.25f;
//			float percent = (1f - INCOMING_DAMAGE_MULT) * getEffectLevel() * 100;
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
//					spec.getIconSpriteName(), cloak.getDisplayName(), "damage mitigated by " + (int) percent + "%", false);
        }
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (effectLevel > f) {
                if (getDisruptionLevel(playerShip) <= 0f) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                        cloak.specAPI.iconSpriteName,
                        "phase coils stable",
                        "top speed at 100%",
                        false)
                } else {
                    //String disruptPercent = "" + (int)Math.round((1f - disruptionLevel) * 100f) + "%";
                    //String speedMultStr = Strings.X + Misc.getRoundedValue(getSpeedMult());
                    val speedPercentStr: String =
                        "" + (Math.round(getSpeedMult(playerShip, effectLevel) * 100f) as Int) + "%"
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                        cloak.specAPI.iconSpriteName,  //"phase coils at " + disruptPercent,
                        "phase coil stress",
                        "top speed at $speedPercentStr",
                        true)
                }
            }
        }
    }


    fun getSpeedMult(ship: ShipAPI?, effectLevel: Float): Float {
        return if (getDisruptionLevel(ship) <= 0f) 1f else MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(
            ship) * effectLevel)
    }


    override fun apply(stats: MutableShipStatsAPI, id: String, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var id = id
        var ship: ShipAPI? = null
        var player = false
        if (stats.entity is ShipAPI) {
            ship = stats.entity as ShipAPI
            player = ship === Global.getCombatEngine().playerShip
            id = id + "_" + ship.id
        } else {
            return
        }

        if (player) {
            maintainStatus(ship, state, effectLevel)
        }
        if (Global.getCombatEngine().isPaused) {
            return
        }

        var cloak = ship!!.phaseCloak
        if (cloak == null) cloak = ship.system
        if (cloak == null) return

        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (state == ShipSystemStatsScript.State.ACTIVE || state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IN) {
                val mult = getSpeedMult(ship, effectLevel)
                if (mult < 1f) {
                    stats.maxSpeed.modifyMult(id + "_2", mult)
                } else {
                    stats.maxSpeed.unmodifyMult(id + "_2")
                }
                (cloak as PhaseCloakSystemAPI).minCoilJitterLevel = getDisruptionLevel(ship)
            }
        }
        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            unapply(stats, id)
            return
        }

        for (weapon in ship.allWeapons.filter { it.isDecorative }) {
            weapon.sprite.color = Color(255, 255, 255, (254 * (1 - ship.phaseCloak.effectLevel)).toInt())
        }

        val speedPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f)
        val accelPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f)
        stats.maxSpeed.modifyPercent(id, speedPercentMod * effectLevel)
        stats.acceleration.modifyPercent(id, accelPercentMod * effectLevel)
        stats.deceleration.modifyPercent(id, accelPercentMod * effectLevel)
        val speedMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult()
        val accelMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult()
        stats.maxSpeed.modifyMult(id, speedMultMod * effectLevel)
        stats.acceleration.modifyMult(id, accelMultMod * effectLevel)
        stats.deceleration.modifyMult(id, accelMultMod * effectLevel)

        var levelForAlpha = effectLevel


        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.isPhased = true
            levelForAlpha = effectLevel
        } else if (state == ShipSystemStatsScript.State.OUT) {
            ship.isPhased = effectLevel > 0.5f
            levelForAlpha = effectLevel
        }

        ship.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
        ship.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

        ship.engineController.fadeToOtherColor(this, Color(128, 41, 47, 200), Color(128, 41, 47, 200), 1f * effectLevel, 1f)
        ship.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)


        val extra = 0f
        val shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }


        //Fighters
        var fighters = ship.allWings.flatMap { it.wingMembers }.toMutableList()
        fighters.addAll(ship.allWings.flatMap { it.returning.map { it.fighter } })

        for (fighter in fighters) {

            if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
                fighter.isPhased = true
                fighter.isHoldFireOneFrame = true

                for (weapon in fighter.allWeapons) {
                    weapon.setRemainingCooldownTo(weapon.cooldown * 0.5f)
                    weapon.stopFiring()
                }

            } else if (state == ShipSystemStatsScript.State.OUT) {
                fighter.isPhased = effectLevel > 0.5f
            }

            fighter.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
            fighter.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

            fighter.engineController.fadeToOtherColor(this, Color(128, 41, 47, 200), Color(128, 41, 47, 20), 1f * effectLevel, 1f)
            fighter.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)

            val fighterTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra) * 0.5f
            fighter.mutableStats.timeMult.modifyMult(id, fighterTimeMult)


        }
    }


    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        var ship: ShipAPI? = null
        ship = if (stats.entity is ShipAPI) {
            stats.entity as ShipAPI
        } else {
            return
        }
        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)
        stats.maxSpeed.unmodify(id)
        stats.maxSpeed.unmodifyMult(id + "_2")
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)
        ship.isPhased = false
        ship!!.extraAlphaMult = 1f
        var cloak = ship.phaseCloak
        if (cloak == null) cloak = ship.system
        if (cloak != null) {
            (cloak as PhaseCloakSystemAPI).minCoilJitterLevel = 0f
        }
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {

        return null
    }


}