package assortment_of_things.abyss.weapons.threat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.combat.threat.BaseFragmentMissileEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.*
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class PrayerWeaponEffect : BaseFragmentMissileEffect() {

    var FRINGE_COLOR: Color = Color(235, 255, 215, 235)
    var CORE_COLOR: Color = Color(225, 255, 205, 200)

    var SERAPH_COLOR = Color(196, 20, 35)
    var SERAPH_COLOR_CORE = Color(196, 20, 35).darker()

    override fun withEMPArc(): Boolean {
        return true
    }

    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {
        val ship = weapon!!.ship ?: return

        val swarm = getSwarmFor(ship)
        val active = swarm?.numActiveMembers ?: 0
        val required = numFragmentsToFire
        var disable = active < required
        if (ship.system.effectLevel <= 0.5f) disable = true //Only enabled with the shipsystem active

        //Disable if below 10 fragments so that other weapons can also fire a bit.
        var autopilot = Global.getCombatEngine()?.combatUI?.isAutopilotOn ?: false
        if (ship != Global.getCombatEngine().playerShip || autopilot) {
            if (swarm.numActiveMembers <= 10) {
                disable = true
            }
        }

        weapon!!.isForceDisabled = disable


        showNoFragmentSwarmWarning(weapon, ship)
    }

    override fun configureMissileSwarmParams(params: RoilingSwarmParams) {
//		params.flashFringeColor = new Color(235,255,215,235);
//		params.flashCoreColor = new Color(225,255,205,200);

//		params.baseSpringConstant *= 2f;
//		params.springConstantNegativeRange *= 2f;

        params.maxOffset = 40f

        params.maxSpeed = missile.maxSpeed + 10f
        params.outspeedAttachedEntityBy = 0f

        var core = Misc.interpolateColor(CORE_COLOR, SERAPH_COLOR_CORE, 0.80f)
        var fringe = Misc.interpolateColor(FRINGE_COLOR, SERAPH_COLOR, 0.80f)

        //params.flashFringeColor = Misc.setAlpha(FRINGE_COLOR, 50);
        params.flashFringeColor = core
        params.flashCoreColor = fringe

        params.flashCoreRadiusMult = 0f
        //params.renderFlashOnSameLayer = true;
        params.flashRadius = 45f
        params.autoscale = true

        params.flashFrequency = 40f
        params.flashProbability = 1f

        //params.preFlashDelay = 0.5f * (float) Math.random();

//		params.flashFrequency = 40f;
//		params.flashProbability = 1f;
    }

    override fun swarmCreated(missile: MissileAPI?,
                              missileSwarm: RoilingSwarmEffect,
                              sourceSwarm: RoilingSwarmEffect?) {
        var first = true
        for (p in missileSwarm.members) {
            p.scaler.brightness = p.scale
            if (first) {
                p.scaler.isBounceDown = false
                p.scaler.fadeIn()
            } else {
//				p.scaler.setBounceUp(false);
//				p.scaler.fadeOut();
                p.keepScale = true
            }
            p.flash = null
            p.flash()
            p.flash.isBounceDown = false
            first = false
        }
    }

    override fun swarmAdvance(amount: Float, missile: MissileAPI?, swarm: RoilingSwarmEffect?) {
        super.swarmAdvance(amount, missile, swarm)
    }

    override fun getNumOtherMembersToTransfer(): Int {
        return 2
    }

    override fun explodeOnFizzling(): Boolean {
        return false
    }

    override fun shouldMakeMissileFaceTargetOnSpawnIfAny(): Boolean {
        return true
    }

    override fun getOtherFragmentBehaviorOnImpact(): FragmentBehaviorOnImpact {
        return FragmentBehaviorOnImpact.STOP_AND_FADE
    }

    override fun reportFragmentHit(missile: MissileAPI,
                                   p: SwarmMember,
                                   swarm: RoilingSwarmEffect?,
                                   target: CombatEntityAPI?) {
        val engine = Global.getCombatEngine()
        val color = FRINGE_COLOR
        //color = Misc.setAlpha(color, 255);
        val size = 80f // not radius
        engine.addHitParticle(p.loc, Vector2f(), size, 0.5f, color)
        engine.addHitParticle(p.loc, Vector2f(), size * 0.25f, 1f, CORE_COLOR)

    }
}