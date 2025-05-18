package assortment_of_things.abyss.hullmods

import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.RiftLanceEffect
import com.fs.starfarer.api.impl.combat.threat.FragmentSwarmHullmod
import com.fs.starfarer.api.impl.combat.threat.FragmentWeapon
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.getSwarmFor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import org.magiclib.util.MagicIncompatibleHullmods
import java.awt.Color
import kotlin.math.max
import kotlin.random.Random

class SymbiosisHullmod : BaseHullMod() {


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip.addSpacer(10f)
        tooltip.addPara(
            "A swarm of Threat fragments roils around the ship, providing a regenerating supply of fragments for fragment-based weapons. \n\n" + "The base number of fragments is 50/100/200. Every 75 units of hull and armor damage dealt towards opponents or received by the ship itself generates 1 additional replacement fragment. \n\n" + "The ship is capable of using its subsystem to turn wrecks in a 1500 unit radius, friend or foe, in to 40/60/100/150 additional fragments, based on the hullsize of the targeted ship*. Fragments generated this way can temporarily go past the maximum capacity of the ship.",
            0f,
            Misc.getTextColor(),
            Misc.getHighlightColor(),
            "50",
            "100",
            "200",
            "75",
            "hull and armor",
            "1",
            "1500",
            "40",
            "60",
            "100",
            "150",
        )

        tooltip.addSpacer(10f)
        tooltip.addPara("The ships sensor profile is reduced by 50%% and damage towards its weapons, " +
                "engines and any kind of EMP damage is reduced by 25%%. Energy weapons have their range increased by 100 units.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "50%", "25%", "100")
        tooltip.addSpacer(10f)

        tooltip.addPara("*Wrecks that have split in to multiple pieces count as smaller hullsizes.", 0f, Misc.getGrayColor(), Misc.getGrayColor())



    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {


        //stats.dynamic.getMod(Stats.SWARM_LAUNCHER_WING_SIZE_MOD).modifyFlat(id, -1f)

        //Make Shrouded Hmods incomp
        for (hullmod in Global.getSettings().allHullModSpecs) {
            if (hullmod.hasTag(Tags.SHROUDED) && stats.variant.hasHullMod(hullmod.id)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(
                    stats.getVariant(),
                    hullmod.id,
                    "rat_abyssal_threat"
                );
            }
        }

        var blocked = ArrayList<String>()
        blocked.add(HullMods.FRAGMENT_SWARM)
        blocked.add(HullMods.SECONDARY_FABRICATOR)
        blocked.add(HullMods.FRAGMENT_COORDINATOR)

        for (block in blocked) {
            if(stats.getVariant().hasHullMod(block)){
                MagicIncompatibleHullmods.removeHullmodWithWarning(
                    stats.getVariant(),
                    block,
                    "rat_abyssal_threat"
                );
            }
        }



        stats.sensorProfile.modifyMult(id, 0.5f)

        stats.empDamageTakenMult.modifyMult(id, 0.75f)
        stats.engineDamageTakenMult.modifyMult(id, 0.75f)
        stats.weaponDamageTakenMult.modifyMult(id, 0.75f)

        stats.energyWeaponRangeBonus.modifyFlat(id, 100f)

    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {

        if (!ship.hasListenerOfClass(SymbiosisListener::class.java)) {
            var listener = SymbiosisListener(ship)
            ship.addListener(listener)
            MagicSubsystemsManager.addSubsystemToShip(ship, SymbiosisSubsystem(listener, ship))
        }

        if (!Global.getCombatEngine().listenerManager.hasListenerOfClass(SymbiosisDamageDealtListener::class.java)) {
            Global.getCombatEngine().listenerManager.addListener(SymbiosisDamageDealtListener())
        }
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }
}

class SymbiosisListener(var ship: ShipAPI) : AdvanceableListener {

    var despawnInterval = IntervalUtil(0.1f, 0.1f)

    //var damageDealtListener = SymbiosisDamageDealtListener(this)
    //var swarmCheckInterval = IntervalUtil(0.2f, 0.25f)

    var fragmentsPerSpawn = 1
    var damagePerSpawn = 75f
    var damageDealtOrTaken = 0f

    init {
        //ship.addListener(damageDealtListener)
    }


    override fun advance(amount: Float) {
        if (amount <= 0f) return


        //Add damage dealt listener to swarms launched by the ship
        /* swarmCheckInterval.advance(amount)
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
         }*/



        var swarm = RoilingSwarmEffect.getSwarmFor(ship)
        if (swarm == null) {
            swarm = FragmentSwarmHullmod.createSwarmFor(ship)
        }

        if (ship.isFighter) return

        val playerShip = Global.getCurrentState() == GameState.COMBAT && Global.getCombatEngine() != null && Global.getCombatEngine().playerShip === ship

        var sizeToMainain = when(ship.hullSize) {
            ShipAPI.HullSize.CRUISER -> 200
            ShipAPI.HullSize.DESTROYER -> 100
            ShipAPI.HullSize.FRIGATE -> 50
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
                var offset = point.plus(MathUtils.getRandomPointInCircle(Vector2f(), 10f))

                //VFX

                for (i in 0 until 3) {
                    Global.getCombatEngine().addNegativeNebulaParticle(offset, Vector2f(), MathUtils.getRandomNumberInRange(45f, 80f),
                        1f, 0.5f, 0f, MathUtils.getRandomNumberInRange(0.5f + delay, 1.5f + delay)
                        , RiftLanceEffect.getColorForDarkening(Color(130,155,145,150)))
                }

                p.loc.set(point)
                p.fader.durationIn = MathUtils.getRandomNumberInRange(0.30f, 0.40f) + delay
                delay += 0.05f //Dont make all of them fade in immediately
            }
        }

        damageDealtOrTaken = remainder
    }

}

//Added once to the engine
class SymbiosisDamageDealtListener() : DamageListener, DamageTakenModifier /*DamageDealtModifier*/ {

    var lastTarget: ShipAPI? = null
    var lastPoint = Vector2f()

    //TODO check if the fighter is part of a wing of a ship with symbiosis
    override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI?) {

        if (target !is ShipAPI) return
        if (!target.isAlive) return
        if (target.isFighter) return

        var hull = result!!.damageToHull
        var armor = result!!.totalDamageToArmor

        if (hull <= 0f && armor <= 0f) return

        //Find Point from other listener
        var point = target.location
        if (target == lastTarget) {
            point = lastPoint
        }

        //Check for damage applied to the host ship
        if (target.hasListenerOfClass(SymbiosisListener::class.java)) {
            var listener = target.getListeners(SymbiosisListener::class.java).first()

            listener.damageDealtOrTaken += hull + armor
            listener.turnDamageToFragments(point, true)

            return
        }

        var ship: ShipAPI? = null
        if (source is WeaponAPI) ship = source.ship
        if (source is ShipAPI) ship = source
        if (ship == null) return

        if (target.owner == ship.owner) return

        var listener: SymbiosisListener? = null
        //Check if the fighter has a host ship with the listener
        if (ship.isFighter) {
            var host = ship.wing?.sourceShip
            listener = host?.getListeners(SymbiosisListener::class.java)?.firstOrNull()
        } else {
            listener = ship.getListeners(SymbiosisListener::class.java)?.firstOrNull()
        }

        if (listener == null) return

        listener.damageDealtOrTaken += hull + armor
        listener.turnDamageToFragments(point, true)
    }

    //Required to get the points of damage because ApplyDamageResult does not give it
    override fun modifyDamageTaken(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?,  point: Vector2f, shieldHit: Boolean): String? {
        if (target !is ShipAPI) return null

        lastTarget = target
        lastPoint = point

        return null
    }

}

class SymbiosisSubsystem(var listener: SymbiosisListener, ship: ShipAPI) : MagicSubsystem(ship) {

    var MAX_RANGE = 1500f
    fun getSystemRange() = ship.mutableStats.systemRangeBonus.computeEffective(MAX_RANGE)

    override fun getBaseInDuration(): Float {
        return 0.33f
    }

    override fun getHUDColor(): Color {
        return Color(130,155,145,150)
    }

    override fun getBaseActiveDuration(): Float {
        return 0.5f
    }

    override fun getBaseCooldownDuration(): Float {
        return 3f
    }

    override fun getBaseOutDuration(): Float {
        return 0.5f
    }

    //Check less often on the smaller ship to make the larger ones get preference for most wrecks.
    var aiUpdateInterval = when(ship!!.hullSize) {
        ShipAPI.HullSize.CRUISER -> IntervalUtil(1f, 2f)
        ShipAPI.HullSize.DESTROYER -> IntervalUtil(2f, 4f)
        ShipAPI.HullSize.FRIGATE -> IntervalUtil(3f, 5f)
        else -> IntervalUtil(1f, 2f)
    }



    override fun shouldActivateAI(amount: Float): Boolean {

        if (ship.hasTag("rat_do_not_use_symbiosis")) return false

        aiUpdateInterval.advance(amount)
        if (aiUpdateInterval.intervalElapsed()) {
            var swarm = RoilingSwarmEffect.getSwarmFor(ship)
            if (swarm.numActiveMembers <= swarm.params.baseMembersToMaintain * 0.9f) {
                if (findValidWrecks().isNotEmpty()) {
                    return true
                }
            }
        }

        return false
    }

    var claimed = ArrayList<ShipAPI>()

    fun findValidWrecks() : List<ShipAPI> {
        var list = ArrayList<ShipAPI>()

        var grid = Global.getCombatEngine().shipGrid.getCheckIterator(ship.location, 2000f, 2000f)

        var max = getSystemRange()
        for (wreck in grid.iterator()) {
            if (wreck !is ShipAPI) continue
            if (!wreck.isHulk) continue
            if (wreck.isFighter) continue
            if (MathUtils.getDistance(ship, wreck) > max) continue
            if (wreck.variant.hasHullMod("shard_spawner")) continue //Prevent omegas from being turned to scrap
            //if (wreck.hasTag("symbiosis_claimed") && !claimed.contains(wreck)) continue
            list.add(wreck)
        }

        return list
    }


    var particleInterval = IntervalUtil(0.1f, 0.1f)

    fun getAllClaimedWrecks() : MutableList<ShipAPI> {
        var list = Global.getCombatEngine().customData.get("rat_symbiosis_claimed") as MutableList<ShipAPI>?
        if (list == null) {
            list = ArrayList<ShipAPI>()
            Global.getCombatEngine().customData.set("rat_symbiosis_claimed", list)
        }
        return list
    }


    fun isClaimedByThisShip(wreck: ShipAPI) = claimed.contains(wreck)
    fun isClaimed(wreck: ShipAPI) = getAllClaimedWrecks().contains(wreck)

    fun claimWreck(wreck: ShipAPI) {
        getAllClaimedWrecks().add(wreck)
        claimed.add(wreck)
    }

    override fun advance(amount: Float, isPaused: Boolean) {
        ship.setJitter(this, Color(130,155,145, 55), effectLevel, 3, 0f, 0 + 2f * effectLevel)
        ship.setJitterUnder(this,  Color(130,155,145, 155), effectLevel, 25, 0f, 7f + 4f * effectLevel)

        var color = Color(130,155,145, 155 + (100 * effectLevel).toInt())

        particleInterval.advance(amount)
        if (particleInterval.intervalElapsed()) {
            var wrecks = findValidWrecks()
            for (wreck in wrecks) {

                if (isClaimed(wreck) && !isClaimedByThisShip(wreck)) continue //Dont let multiple ships claim this.

                if (isActive && !isClaimed(wreck)) {
                    claimWreck(wreck)
                }

                var count = 1 + (5 * effectLevel).toInt()

                for (i in 0 until count) {

                    var offset = wreck.location

                    wreck.exactBounds.update(wreck.location, wreck.facing)
                    var bound = wreck.exactBounds.segments.random().p1

                    offset = bound
                    if (Random.nextFloat() >= 0.8f) offset = wreck.location

                    var vel = MathUtils.getRandomPointInCircle(Vector2f(), 10f + (20*effectLevel))

                    if (isActive) {
                        vel = vel.plus(wreck.velocity)
                        offset = offset.plus(MathUtils.getRandomPointInCircle(Vector2f(), wreck.collisionRadius * 0.4f))
                    } else {
                        vel = vel.plus((Vector2f(wreck.velocity.x * 0.2f, wreck.velocity.y * 0.2f)))
                    }

                    Global.getCombatEngine().addNebulaParticle(offset, vel, MathUtils.getRandomNumberInRange(45f + (80 * effectLevel), 80f + (120 * effectLevel)),
                        1f, 0.5f, 0f, MathUtils.getRandomNumberInRange(0.5f + (0.75f * effectLevel), 1.5f + (1.5f * effectLevel))
                        , color)
                }


                if (state == State.OUT) {
                    wreck.alphaMult = effectLevel
                    wreck.extraAlphaMult = effectLevel
                }
            }
        }


    }


    //Removed, Pieces already count as smaller hullsizes
    //Maps ShipID to Pieces. Whenever retrieved, checks to see if the pieces have grown larger and increases by that if so
    /*fun getMaxPiecesRecorded(shipId: String) : Int {
        var map = Global.getCombatEngine().customData.get("rat_symbiosis_pieces") as HashMap<String, Int>?
        if (map == null) {
            map = HashMap<String, Int>()
            Global.getCombatEngine().customData.set("rat_symbiosis_pieces", map)
        }


        var recorded: Int? = map.get(shipId)
        var onField = Global.getCombatEngine().ships.count { it.id == shipId }
        if (recorded == null || onField > recorded) {
            recorded = onField
        }
        map.set(shipId, recorded)

        return recorded
    }*/


    override fun onFinished() {
        super.onFinished()

        for (claimed in claimed) {
            //var pieces = getMaxPiecesRecorded(claimed.id)

            var fragments = when(claimed.hullSize) {
                ShipAPI.HullSize.CAPITAL_SHIP -> 150
                ShipAPI.HullSize.CRUISER -> 100
                ShipAPI.HullSize.DESTROYER -> 60
                ShipAPI.HullSize.FRIGATE -> 40
                else -> 0
            }

            //var test = ""

            //fragments /= pieces

            claimed.explosionScale = 0f

            var swarm = getSwarmFor(ship)
            for (i in 0 until fragments) {
                var m = swarm.addMember()
                m.fader.durationIn = MathUtils.getRandomNumberInRange(0.25f, 0.3f)
                m.loc = claimed.location.plus(MathUtils.getRandomPointInCircle(Vector2f(), claimed.collisionRadius * 0.3f))
            }

            Global.getCombatEngine().removeEntity(claimed)
        }

        claimed.clear()
    }

    override fun onActivate() {
        Global.getSoundPlayer().playSound("system_construction_swarm", 0.7f, 0.8f, ship.location, Vector2f())
    }

    override fun getDisplayText(): String {
       return "Fragmentation"
    }

}