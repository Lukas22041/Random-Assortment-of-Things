package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.hullmods.PhaseshiftShield
import assortment_of_things.misc.baseOrModSpec
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

class ExophaseShipsystem : BaseShipSystemScript() {

    companion object {
        var SHIP_ALPHA_MULT = 0.25f
        var MAX_TIME_MULT = 3f
        var ENGINE_COLOR = Color(255, 177, 127, 200)
    }


    var VULNERABLE_FRACTION = 0f
    var INCOMING_DAMAGE_MULT = 0.25f


    var MAX_TIME_MULT = 3f

    var FLUX_LEVEL_AFFECTS_SPEED = true
    //var MIN_SPEED_MULT = 0.75f
    var MIN_SPEED_MULT = 0.60f
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

        if (ship.travelDrive.isActive) return

        if (ship.variant.hasTag("Arkas-Phantom")) return

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
            weapon.sprite.color = Color(255, 255, 255, (254 * (1 - (ship.phaseCloak.effectLevel * 0.5f))).toInt())
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

        ship.engineController.fadeToOtherColor(this, ENGINE_COLOR, ENGINE_COLOR, 1f * effectLevel, 1f)
        ship.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)

        /*var thrusterID = 1000
        for (weapon in ship.allWeapons) {
            if (!ship.isAlive) continue
            if (ship.isHulk) continue
            if (weapon.spec.weaponId != "rat_exo_phase_trail_location") continue

            var color = Color(255, 177, 127, 50)


            thrusterID += 1000
            var facing = weapon.arcFacing + ship.facing
            var location = weapon.location

            MagicTrailPlugin.addTrailMemberSimple(ship, trailID + thrusterID, Global.getSettings().getSprite("fx", "base_trail_zapWithCore"),
                Vector2f(location.x, location.y) , 100f, facing, 10f, 5f, color, 1f * effectLevel, 0f, 0.15f, 0.1f, true )

        }*/

        val extra = 0f
        val shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
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

    override fun getInOverride(ship: ShipAPI?): Float {
        if (ship?.baseOrModSpec()?.baseHullId == "rat_gilgamesh") return 0.75f
        return super.getInOverride(ship)
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {

        return null
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {

        var disallowPhaseTimer = ship!!.customData.get("rat_dont_allow_phase") as Float?

        if (disallowPhaseTimer != null) {
            if (disallowPhaseTimer > 0f) {
                return false
            }
        }

        var phaseshiftShieldListener = ship.getListeners(PhaseshiftShield.PhaseshiftShieldListener::class.java).firstOrNull()
        if ((ship.shipAI != null || (Global.getCombatEngine().combatUI.isAutopilotOn && ship == Global.getCombatEngine().playerShip)) && phaseshiftShieldListener != null && ship.system?.isActive == true) {
            if (phaseshiftShieldListener.shieldHP >= 0.5f && ship.fluxLevel <= 0.5f) {
                return false
            }
        }

        /*if (ship!!.baseOrModSpec().hullId == "rat_gilgamesh") {
            if (ship.system.state == ShipSystemAPI.SystemState.IN || ship.system.state == ShipSystemAPI.SystemState.ACTIVE) {
                return false
            }
        }*/

        return super.isUsable(system, ship)
    }


}