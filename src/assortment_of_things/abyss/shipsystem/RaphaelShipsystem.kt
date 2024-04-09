package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import com.fs.starfarer.api.util.IntervalUtil
import data.hullmods.SotfSierrasConcord
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class RaphaelShipsystem : BaseShipSystemScript() {

    val SHIP_ALPHA_MULT: Float = 0.25f
    val VULNERABLE_FRACTION: Float = 0f

    val MAX_TIME_MULT: Float = 3f
    var FLUX_LEVEL_AFFECTS_SPEED: Boolean = true
    var MIN_SPEED_MULT: Float = 0.65f
    var BASE_FLUX_LEVEL_FOR_MIN_SPEED: Float = 0.75f

    // no, ship-system scripts are not one-per-spec like hullmods are
    //private List<ShipAPI> have_phased = new ArrayList<>();
    private var isPhased = false


    protected var STATUSKEY1: Any = Any()
    protected var STATUSKEY2: Any = Any()
    protected var STATUSKEY3: Any = Any()
    protected var STATUSKEY4: Any = Any()

    var activated = false
    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    var buffTimer = 0f
    var maxBufftimer = 3f

    fun getMaxTimeMult(stats: MutableShipStatsAPI): Float {
        return 1f + (MAX_TIME_MULT - 1f) * stats.dynamic.getValue(Stats.PHASE_TIME_BONUS_MULT)
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


        var sotfEnabled = false

        if (Global.getSettings().modManager.isModEnabled("secretsofthefrontier")) {
            sotfEnabled = true
            ship.removeListenerOfClass(SotfSierrasConcord.SotfSierraAfterImageScript::class.java)
        }

        if (player) {
            maintainStatus(ship, state, effectLevel)
        }

        if (Global.getCombatEngine().isPaused) {
            return
        }

        if (buffTimer > 0) {
            buffTimer -= 1f * Global.getCombatEngine().elapsedInLastFrame
            var level = buffTimer / maxBufftimer
            level = level.coerceIn(0f, 1f)

            ship.setCustomData("rat_lensflare_level_overwrite", level)

            stats.energyWeaponDamageMult.modifyMult("rat_raphael_bonus", 1.2f)
            stats.missileWeaponDamageMult.modifyMult("rat_raphael_bonus", 1.2f)
            stats.ballisticWeaponDamageMult.modifyMult("rat_raphael_bonus", 1.2f)

            stats.energyRoFMult.modifyMult("rat_raphael_bonus", 1.2f)
            stats.missileRoFMult.modifyMult("rat_raphael_bonus", 1.2f)
            stats.ballisticRoFMult.modifyMult("rat_raphael_bonus", 1.2f)

            stats.energyWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 0.8f)
            stats.missileWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 0.8f)
            stats.ballisticWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 0.8f)
        }
        else {
            ship.setCustomData("rat_lensflare_level_overwrite", 0f)

            stats.energyWeaponDamageMult.modifyMult("rat_raphael_bonus", 1f)
            stats.missileWeaponDamageMult.modifyMult("rat_raphael_bonus", 1f)
            stats.ballisticWeaponDamageMult.modifyMult("rat_raphael_bonus", 1f)

            stats.energyRoFMult.modifyMult("rat_raphael_bonus", 1f)
            stats.missileRoFMult.modifyMult("rat_raphael_bonus", 1f)
            stats.ballisticRoFMult.modifyMult("rat_raphael_bonus", 1f)

            stats.energyWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 1f)
            stats.missileWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 1f)
            stats.ballisticWeaponFluxCostMod.modifyMult("rat_raphael_bonus", 1f)
        }

        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            isPhased = false
            activated = false
            unapply(stats, id)
            return
        }



        if (!activated && ship.system.isActive) {
            activated = true

            for (weapon in ship.allWeapons) {
                if (weapon.id.contains("raphael_launcher")) {
                    var extra = weapon.ammo + 4
                    extra = MathUtils.clamp(extra, 0, weapon.maxAmmo)
                    weapon.ammo = extra
                }
            }
        }

        val baseSpeedBonus = 75f

        //if (ship.getVariant().hasHullMod(SotfIDs.EIDOLONS_CONCORD)) {
        //	baseSpeedBonus = 25f;
        //}
        val level = effectLevel

        //float f = VULNERABLE_FRACTION;
        val jitterLevel = 0f
        val jitterRangeBonus = 0f
        var levelForAlpha = level

        var cloak = ship!!.phaseCloak
        if (cloak == null) cloak = ship.system

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

        val speedPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(baseSpeedBonus)
        val accelPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(baseSpeedBonus)
        stats.maxSpeed.modifyPercent(id, speedPercentMod * effectLevel)
        stats.maxTurnRate.modifyPercent(id, accelPercentMod * effectLevel)
        stats.turnAcceleration.modifyPercent(id, accelPercentMod * effectLevel)
        stats.acceleration.modifyPercent(id, accelPercentMod * effectLevel)
        stats.deceleration.modifyPercent(id, accelPercentMod * effectLevel)

        val speedMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult()
        val accelMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult()
        stats.maxSpeed.modifyMult(id, speedMultMod * effectLevel)
        stats.maxTurnRate.modifyPercent(id, accelMultMod * effectLevel)
        stats.turnAcceleration.modifyPercent(id, accelMultMod * effectLevel)
        stats.acceleration.modifyMult(id, accelMultMod * effectLevel)
        stats.deceleration.modifyMult(id, accelMultMod * effectLevel)

        stats.shieldUpkeepMult.modifyMult(id, 0.5f)


        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!,  Color(196, 20, 35, 25), AbyssUtils.SIERRA_COLOR.setAlpha(0),  1f, 0f, Vector2f().plus(ship!!.location))
            }
        }

        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.isPhased = true
            levelForAlpha = level



            if (!isPhased) {
                val p = RiftCascadeMineExplosion.createStandardRiftParams(Color(200, 125, 255, 155),
                    ship.shieldRadiusEvenIfNoShield + 20f)
                p.fadeOut = 0.15f
                p.hitGlowSizeMult = 0.25f
                p.underglow = Color(255, 175, 255, 50)
                p.withHitGlow = false
                p.noiseMag = 1.25f

                if (ship.variant.hasHullMod("sotf_serenity")) {
                    p.noiseMag *= 0.25f
                }

                if (ship.variant.hasHullMod("sotf_fervor")) {
                    p.thickness += 10f
                    p.noiseMag *= 2f
                }

                val e = Global.getCombatEngine().addLayeredRenderingPlugin(NegativeExplosionVisual(p))
                e.location.set(ship.location)

                //if (SotfModPlugin.GLIB) {
                if (!ship.variant.hasHullMod("sotf_serenity")) {
                    val ripple = RippleDistortion(ship.location, ship.velocity)
                    ripple.intensity = ship.collisionRadius * 0.75f
                    ripple.size = ship.shieldRadiusEvenIfNoShield
                    ripple.fadeInSize(0.15f)
                    ripple.fadeOutIntensity(0.5f)
                    DistortionShader.addDistortion(ripple)
                }

                isPhased = true

                var whisperType = "playful"
                if (Global.getSector() != null) {
                    var guilt = Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().getFloat("\$sotf_guilt") ?: 0f
                    if (guilt >= 4f) {
                        whisperType = "angry"
                    }
                }

                // phase ghost whispers
                if (player && Math.random() < 0.1f && sotfEnabled) {
                    Global.getSoundPlayer().playUISound("sotf_ghost_$whisperType", 1f, 1f)
                }


            }
        } else if (state == ShipSystemStatsScript.State.OUT) {
            if (level > 0.5f) {
                ship.isPhased = true
            } else {
                ship.isPhased = false
            }
            levelForAlpha = level

            buffTimer = maxBufftimer
        }

      /*  ship.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
        ship.setApplyExtraAlphaToEngines(true)*/

        ship.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
        ship.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

        ship.engineController.fadeToOtherColor(this, Color(196, 20, 35, 255), Color(196, 20, 35, 255), 1f * effectLevel, 1f)
        ship.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)

        val shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha
        var perceptionMult = shipTimeMult
        if (player) {
            perceptionMult = 1f + ((getMaxTimeMult(stats) - 1f) * 0.65f) * levelForAlpha
        }
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / perceptionMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }
    }


    override fun unapply(stats: MutableShipStatsAPI, id: String?) {
        var ship: ShipAPI? = null
        val player = false
        if (stats.entity is ShipAPI) {
            ship = stats.entity as ShipAPI
            //player = ship == Global.getCombatEngine().getPlayerShip();
            //if (player) {
            //	ship.getSystem().setCooldownRemaining(2f);
            //}
            //id = id + "_" + ship.getId();
        } else {
            return
        }

        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)

        ship.isPhased = false
        ship!!.extraAlphaMult = 1f

        stats.maxSpeed.unmodify(id)
        stats.maxTurnRate.unmodify(id)
        stats.turnAcceleration.unmodify(id)
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)
        stats.shieldUpkeepMult.unmodify(id)

        var cloak = ship.phaseCloak
        if (cloak == null) cloak = ship.system
        if (cloak != null) {
            (cloak as PhaseCloakSystemAPI).minCoilJitterLevel = 0f
        }
    }

    protected fun isDisruptable(cloak: ShipSystemAPI): Boolean {
        return cloak.specAPI.hasTag(Tags.DISRUPTABLE)
    }

    protected fun getDisruptionLevel(ship: ShipAPI?): Float {
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
        val level = effectLevel
        val f = VULNERABLE_FRACTION

        var cloak = playerShip!!.phaseCloak
        if (cloak == null) cloak = playerShip.system
        if (cloak == null) return

        if (level > f) {
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
            if (level > f) {
                if (getDisruptionLevel(playerShip) <= 0f) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                        cloak.specAPI.iconSpriteName,
                        "concord stable",
                        "top speed at 100%",
                        false)
                } else {
                    //String disruptPercent = "" + (int)Math.round((1f - disruptionLevel) * 100f) + "%";
                    //String speedMultStr = Strings.X + Misc.getRoundedValue(getSpeedMult());
                    val speedPercentStr = Math.round(getSpeedMult(playerShip, effectLevel) * 100f).toString() + "%"
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                        cloak.specAPI.iconSpriteName,  //"phase coils at " + disruptPercent,
                        "concord stress",
                        "top speed at $speedPercentStr",
                        true)
                }
            }
        }
    }

    fun getSpeedMult(ship: ShipAPI?, effectLevel: Float): Float {
        if (getDisruptionLevel(ship) <= 0f) return 1f
        return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel)
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {
//		if (index == 0) {
//			return new StatusData("time flow altered", false);
//		}
//		float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
//		if (index == 1) {
//			return new StatusData("damage mitigated by " + (int) percent + "%", false);
//		}
        return null
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI): Boolean {
        return !ship.variant.hasTag("sotf_inert")
    }


}