package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.combat.threat.FragmentSwarmHullmod
import com.fs.starfarer.api.impl.combat.threat.FragmentWeapon
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicIncompatibleHullmods
import kotlin.math.max

class SymbiosisHullmod : BaseHullMod() {


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip.addSpacer(10f)
        tooltip.addPara("A swarm of Threat fragments roils around the ship, providing a regenerating supply of fragments for fragment-based weapons. \n\n" +
                "The base number of fragments is 50/100/200. Every 100 units of hull and armor damage dealt towards opponents or received by the ship itself generates 1 additional replacement fragment. \n\n" +
                "The ship is capable of using its subsystem to turn nearby wrecks, friend or foe, in to 40/80/120/160 additional fragments, based on the hullsize of the targeted ship. Fragments generated this way can temporarily go past the maximum capacity of the ship.\n\n" +
                "The ships sensor profile is reduced by 50%% and damage towards its weapons, engines and any kind of EMP damage is reduced by 25%%. Energy weapons have their range increased by 100 units.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "50", "100", "200",      "100", "hull and armor", "1",      "40", "80", "120", "160",      "50%", "25%", "100")

    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {

        if(stats.getVariant().getHullMods().contains(HullMods.FRAGMENT_SWARM)){
            //if someone tries to install heavy armor, remove it
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                stats.getVariant(),
                HullMods.FRAGMENT_SWARM,
                "rat_abyssal_threat"
            );
        }

        stats.sensorProfile.modifyMult(id, 0.5f)

        stats.empDamageTakenMult.modifyMult(id, 0.75f)
        stats.engineDamageTakenMult.modifyMult(id, 0.75f)
        stats.weaponDamageTakenMult.modifyMult(id, 0.75f)

        stats.energyWeaponRangeBonus.modifyFlat(id, 100f)

    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        if (!ship.hasListenerOfClass(SymbiosisListener::class.java)) {
            ship.addListener(SymbiosisListener(ship))
        }
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }
}

class SymbiosisListener(var ship: ShipAPI) : AdvanceableListener {

    var despawnInterval = IntervalUtil(0.1f, 0.1f)

    var damageDealtListener = SymbiosisDamageDealtListener(this)
    var swarmCheckInterval = IntervalUtil(0.2f, 0.25f)

    var fragmentsPerSpawn = 1
    var damagePerSpawn = 100f
    var damageDealtOrTaken = 0f

    init {
        ship.addListener(damageDealtListener)
    }


    override fun advance(amount: Float) {
        if (amount <= 0f) return


        //Add damage dealt listener to swarms launched by the ship
        swarmCheckInterval.advance(amount)
        if (swarmCheckInterval.intervalElapsed()) {
            for (fighter in Global.getCombatEngine().ships) {
                if (!fighter.isFighter) continue
                if (RoilingSwarmEffect.getSwarmFor(fighter) == null) continue

                if (fighter.wing?.sourceShip == ship) {
                    if (!fighter.hasListenerOfClass(SymbiosisDamageDealtListener::class.java)) {
                        fighter.addListener(damageDealtListener)
                    }
                }
            }
        }



        var swarm = RoilingSwarmEffect.getSwarmFor(ship)
        if (swarm == null) {
            swarm = FragmentSwarmHullmod.createSwarmFor(ship)
        }

        if (ship.isFighter) return

        val playerShip = Global.getCurrentState() == GameState.COMBAT && Global.getCombatEngine() != null && Global.getCombatEngine().playerShip === ship

        var sizeToMainain = when(ship.hullSize) {
            ShipAPI.HullSize.CRUISER -> 200
            ShipAPI.HullSize.DESTROYER -> 100
            ShipAPI.HullSize.FRIGATE -> 500
            else -> 0
        }

        //swarm.addMember()

        val params = swarm!!.params
        params.baseMembersToMaintain = sizeToMainain
        params.maxNumMembersToAlwaysRemoveAbove = sizeToMainain
        params.removeMembersAboveMaintainLevel = false
        params.memberRespawnRate = 0f
        params.initialMembers = params.baseMembersToMaintain

        //swarm.addMembers(10)

        //Despawn members, 10 per second, if over the limit
        despawnInterval.advance(amount)
        if (despawnInterval.intervalElapsed()) {
            if (swarm.numActiveMembers > sizeToMainain) {
                swarm.despawnMembers(1)
            }
        }


        if (playerShip) {
            val active = swarm!!.numActiveMembers

            var maxRequired = 0
            for (w in ship.allWeapons) {
                if (w.effectPlugin is FragmentWeapon) {
                    val fw = w.effectPlugin as FragmentWeapon
                    maxRequired = max(maxRequired.toDouble(), fw.numFragmentsToFire.toDouble()).toInt()
                }
            }

            val debuff = active < maxRequired
            Global.getCombatEngine().maintainStatusForPlayerShip(FragmentSwarmHullmod.STATUS_KEY1,
                Global.getSettings().getSpriteName("ui", "icon_tactical_fragment_swarm"),
                "Symbiosis",
                "FRAGMENTS: $active",
                debuff)
        }
    }

    //Point is the location of where the damage occured
    fun turnDamageToFragments(point: Vector2f, onOpponent: Boolean) {
        if (damageDealtOrTaken < damagePerSpawn) return
        var swarm = RoilingSwarmEffect.getSwarmFor(ship) ?: return

        var divided = damageDealtOrTaken / damagePerSpawn
        var remainder = damageDealtOrTaken % damagePerSpawn

        var spawns = divided.toInt()

        var delay = 0f
        for (i in 0 until spawns) {
            for (j in 0 until fragmentsPerSpawn) {
                if (swarm.numActiveMembers >= swarm.params.baseMembersToMaintain) continue
                val p: SwarmMember = swarm.addMember()

                var offset = point.plus(MathUtils.getRandomPointInCircle(Vector2f(), 30f))

                p.loc.set(point)
                p.fader.durationIn = MathUtils.getRandomNumberInRange(0.5f, 0.6f) + delay
                delay += 0.05f //Dont make all of them fade in immediately
            }
        }

        damageDealtOrTaken = remainder
    }

}

class SymbiosisDamageDealtListener(var listener: SymbiosisListener) : DamageDealtModifier {

    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?, point: Vector2f?, shieldHit: Boolean): String? {

        if (target !is ShipAPI) return null
        if (!target.isAlive || target.owner == listener.ship.owner) return null
        if (shieldHit) return null

        var currentDamage = 0f

       /* if (param is BeamAPI) {
            currentDamage += damage!!.damage * damage.dpsDuration
        }
        else {
            currentDamage += damage!!.damage
        }*/

        currentDamage += damage!!.computeDamageDealt(damage.dpsDuration)

        listener.damageDealtOrTaken += currentDamage

        listener.turnDamageToFragments(point!!, true)

        return null
    }

}