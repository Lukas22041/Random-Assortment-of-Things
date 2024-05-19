package assortment_of_things.abyss.objectives

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*

class DeactivatedDronesObjective : BaseBattleObjectiveEffect() {

    var drones = ArrayList<ShipAPI>()
    var activated = false

    override fun init(engine: CombatEngineAPI?, objective: BattleObjectiveAPI?) {
        super.init(engine, objective)

        var system = Global.getSector().playerFleet.starSystem
        var data = AbyssUtils.getSystemData(system)


        var corePicker = WeightedRandomPicker<PersonAPI?>()
        corePicker.add(ChronosCore().createPerson(RATItems.CHRONOS_CORE, Factions.NEUTRAL, Random()), 1f)
        corePicker.add(CosmosCore().createPerson(RATItems.COSMOS_CORE, Factions.NEUTRAL, Random()), 1f)
        corePicker.add(null, 1.5f)

        if (data.depth == AbyssDepth.Deep) {
            corePicker.add(SeraphCore().createPerson(RATItems.SERAPH_CORE, Factions.NEUTRAL, Random()), 0.5f)
        }

        var count = 1
        var central = false
        var extraRange = 0f

        var variantPicker = WeightedRandomPicker<String>()

        if (objective!!.type == "rat_deactivated_drone") {
            var typePicker = WeightedRandomPicker<String>()
            typePicker.add("small", 1f)
            typePicker.add("medium", 0.75f)
            typePicker.add("large", 0.5f)
            var pick = typePicker.pick()

            if (pick == "small") {
                count = MathUtils.getRandomNumberInRange(3, 4)

                variantPicker.add("rat_merrow_Attack", 1f)
                variantPicker.add("rat_merrow_Support", 0.5f)
                variantPicker.add("rat_makara_Attack", 1f)
                variantPicker.add("rat_makara_Strike", 1f)
                variantPicker.add("rat_chuul_Attack", 0.15f)
                variantPicker.add("rat_chuul_Strike", 0.15f)

                if (data.depth == AbyssDepth.Deep) {
                    variantPicker.add("rat_raguel_Attack", 0.1f)
                    variantPicker.add("rat_raguel_Strike", 0.1f)
                }

            }
            else if (pick == "medium") {
                count = MathUtils.getRandomNumberInRange(2, 3)

                variantPicker.add("rat_merrow_Attack", 0.5f)
                variantPicker.add("rat_merrow_Support", 0.25f)
                variantPicker.add("rat_makara_Attack", 0.5f)
                variantPicker.add("rat_makara_Strike", 0.5f)
                variantPicker.add("rat_chuul_Attack", 1f)
                variantPicker.add("rat_chuul_Strike", 1f)

                if (data.depth == AbyssDepth.Deep) {
                    variantPicker.add("rat_raguel_Attack", 0.2f)
                    variantPicker.add("rat_raguel_Strike", 0.2f)

                    variantPicker.add("rat_sariel_Attack", 0.1f)
                    variantPicker.add("rat_sariel_Strike", 0.1f)
                }
            }
            else if (pick == "large") {
                count = 1

                variantPicker.add("rat_aboleth_Attack", 1f)
                variantPicker.add("rat_aboleth_Strike", 1f)

                variantPicker.add("rat_aboleth_m_Attack", 1f)
                variantPicker.add("rat_aboleth_m_Strike", 1f)
                variantPicker.add("rat_aboleth_m_Overdriven", 0.25f)

                if (data.depth == AbyssDepth.Deep) {
                    variantPicker.add("rat_sariel_Attack", 0.2f)
                    variantPicker.add("rat_sariel_Strike", 0.2f)
                }
            }
        }
        //Huge only
        else {
            var typePicker = WeightedRandomPicker<String>()
            typePicker.add("morkoth", 1f)
            typePicker.add("pack", 1f)

            if (data.depth == AbyssDepth.Deep) {
                typePicker.add("seraph", 1f)
                typePicker.add("seraph_large", 0.75f)
            }

            var pick = typePicker.pick()

            if (pick == "morkoth") {
                count = 1

                //central = true
                variantPicker.add("rat_morkoth_Attack", 1f)
                variantPicker.add("rat_morkoth_Anchor", 0.75f)
                variantPicker.add("rat_morkoth_Support", 0.5f)
            }
            else if (pick == "seraph_large") {
                count = 1

                //central = true
                variantPicker.add("rat_gabriel_Attack", 1f)
                variantPicker.add("rat_gabriel_Burst", 1f)
            }
            else if (pick == "pack") {
                count = MathUtils.getRandomNumberInRange(6, 7)

                extraRange += 300

                variantPicker.add("rat_merrow_Attack", 1f)
                variantPicker.add("rat_merrow_Support", 0.5f)
                variantPicker.add("rat_makara_Attack", 1f)
                variantPicker.add("rat_makara_Strike", 1f)
                variantPicker.add("rat_chuul_Attack", 0.33f)
                variantPicker.add("rat_chuul_Strike", 0.33f)

                if (data.depth == AbyssDepth.Deep) {
                    variantPicker.add("rat_raguel_Attack", 0.1f)
                    variantPicker.add("rat_raguel_Strike", 0.1f)
                }
            }
            else if (pick == "seraph") {
                count = MathUtils.getRandomNumberInRange(2, 3)

                variantPicker.add("rat_sariel_Attack", 0.5f)
                variantPicker.add("rat_sariel_Strike", 0.5f)

                variantPicker.add("rat_raguel_Attack", 1f)
                variantPicker.add("rat_raguel_Strike", 1f)
            }

        }





        for (i in 0 until count) {
            var pick = variantPicker.pick()
            var drone = createFromVariant(pick, count, extraRange, central)

            var corePick = corePicker.pick()
            if (corePick != null) {
                drone.captain = corePicker.pick()
            }

            if (drone.captain != null) {
                drone.captain.setPersonality(Personalities.RECKLESS)
            }
        }


    }

    /**
     * Apply/unapply effects here.
     * @param amount
     */
    override fun advance(amount: Float) {

        if (objective.owner == 100) {
            for (drone in drones) {

                if (drone.isHulk) continue

                for (weapon in drone.allWeapons) {
                    weapon.setRemainingCooldownTo(weapon.cooldown)
                }

                drone.mutableStats.armorDamageTakenMult.modifyMult("rat_deactivated_drone", 0.75f)
                drone.mutableStats.hullDamageTakenMult.modifyMult("rat_deactivated_drone", 0.75f)
                drone.mutableStats.empDamageTakenMult.modifyMult("rat_deactivated_drone", 0.75f)

                drone!!.velocity.set(Vector2f(drone!!.velocity.x * (1f - (0.25f * amount)), drone.velocity.y * (1f - (0.25f * amount))))
                drone!!.angularVelocity = drone.angularVelocity * (1f - (0.20f * amount))
                drone.isHoldFire = true
            }
        } else if (!activated) {
            activated = true

            for (drone in drones) {
                if (!drone.isHulk) {
                    //drone.isHulk = false

                    drone.isHoldFire = false

                    drone.owner = objective.owner
                    drone.originalOwner = objective.owner
                    if (objective.owner == 0) drone.isAlly = true

                    drone.mutableStats.armorDamageTakenMult.unmodify("rat_deactivated_drone")
                    drone.mutableStats.hullDamageTakenMult.unmodify("rat_deactivated_drone")
                    drone.mutableStats.empDamageTakenMult.unmodify("rat_deactivated_drone")

                    drone.shipAI = Global.getSettings().createDefaultShipAI(drone, ShipAIConfig())
                    drone.shipAI.forceCircumstanceEvaluation()

                    for (weapon in drone.allWeapons) {
                        weapon.repair()
                    }

                    for (engine in drone.engineController.shipEngines) {
                        engine.repair()
                    }
                }
            }

            Global.getCombatEngine().removeEntity(objective)
        }


        //revealArea(1000f)
    }

    /**
     * Must return null if this objective has no effect on the passed in ship.
     * @param ship
     * @return
     */
    override fun getStatusItemsFor(ship: ShipAPI?): MutableList<BattleObjectiveEffect.ShipStatusItem>? {
        return null
    }

    override fun getLongDescription(): String {

        var extra = ""
        if (objective.type == "rat_deactivated_drone_large") extra = "This field appears to hold stronger ships."

        return "Deactivated drones that could be brought back online through capture by either side. $extra\n\n" +
                "Once captured, it can no longer be re-captured and the objective dissapears. The drones do not count towards your Deployment Points.\n\n" +
                "Due to strong internal damage, the drones can only support its side for this encounter. "
    }





    fun createFromVariant(variant: String, count: Int, extraRange: Float, centralSpawn: Boolean) : ShipAPI {
        var variant = Global.getSettings().getVariant(variant)

        variant = variant.clone();
        variant.originalVariant = null;
        variant.hullVariantId = Misc.genUID()
        variant.source = VariantSource.REFIT

        DModManager.addDMods(variant, false, MathUtils.getRandomNumberInRange(1, 3), Random())
        if (DModManager.getNumDMods(variant) > 0) {
            DModManager.setDHull(variant)
        }

        var drone = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, 100, 0.35f,
            objective!!.location, MathUtils.getRandomNumberInRange(0f, 360f))

        drone!!.shipAI = null
        //drone.isHulk = true

        if (!centralSpawn) {
            var extraRadius = 0f
            if (count >= 3) extraRadius + 500
            extraRadius += extraRange
            /*var start = MathUtils.getRandomPointOnCircumference(objective.location, 250f + drone.collisionRadius)
            var destination = MathUtils.getRandomPointInCircle(start, 1000f + extraRadius)*/

            var destination = MathUtils.getRandomPointOnCircumference(objective.location, MathUtils.getRandomNumberInRange(drone.collisionRadius + 250f, 1000f + extraRadius))

            var location = findClearLocation(destination)
            drone.location.set(location)
        }
        else {
            drone.location.set(objective.location)
        }

        drones.add(drone)

        for (weapon in drone.allWeapons) {
            weapon.disable(true)
        }

        for (engine in drone.engineController.shipEngines) {
            engine.disable(true)
        }

        var healthLevel = MathUtils.getRandomNumberInRange(0.75f, 0.90f)
        drone.hitpoints *= healthLevel

        if (drone.baseOrModSpec().hasTag("rat_seraph")) {
            var core = SeraphCore().createPerson(RATItems.SERAPH_CORE, Factions.NEUTRAL, Random())
            drone.captain = core
        }

        return drone
    }

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
        Global.getCombatEngine().getFleetManager(owner).isSuppressDeploymentMessages = true

        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)

        val ship = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        ship.crAtDeployment = combatReadiness
        ship.currentCR = combatReadiness
        ship.owner = owner
        ship.shipAI.forceCircumstanceEvaluation()

        Global.getCombatEngine().getFleetManager(owner).isSuppressDeploymentMessages = false

        return ship
    }



    private fun findClearLocation(dest: Vector2f): Vector2f {
        if (isLocationClear(dest)) return dest

        val incr = 50f

        val tested = WeightedRandomPicker<Vector2f>()
        var distIndex = 1f
        while (distIndex <= 32f) {
            val start = Math.random().toFloat() * 360f
            var angle = start
            while (angle < start + 360) {
                val loc = Misc.getUnitVectorAtDegreeAngle(angle)
                loc.scale(incr * distIndex)
                Vector2f.add(dest, loc, loc)
                tested.add(loc)
                if (isLocationClear(loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }

        if (tested.isEmpty) return dest // shouldn't happen

        return tested.pick()
    }

    private fun isLocationClear(loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            if (other.isFighter) continue

            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield * 1.2f
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }

            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            var checkDist = MineStrikeStats.MIN_SPAWN_DIST
            if (other.isFrigate) checkDist = MineStrikeStats.MIN_SPAWN_DIST_FRIGATE
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + MineStrikeStats.MIN_SPAWN_DIST) {
                return false
            }
        }

        return true
    }

}