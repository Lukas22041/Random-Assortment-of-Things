package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.event.NewDepthReachedFactor
import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.abyss.procgen.templates.AbyssSystemHigh
import assortment_of_things.abyss.procgen.templates.AbyssSystemLow
import assortment_of_things.abyss.procgen.templates.AbyssSystemMid
import assortment_of_things.misc.logger
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

class AbyssChainGenerator {

    var maxSteps = 4
    var currentSteps = 0

    var reachedFinal = false
    var totalSystems = 0

    var latestSystems = ArrayList<StarSystemAPI>()
    var systemsInSameStep: HashMap<Int, MutableList<StarSystemAPI>> = HashMap()

    var positionsOnMap = ArrayList<Vector2f>()

    var usedNames = ArrayList<String>()

    var lastSystemWasPositiveOnMap = false
    var generatedExtra = false

    fun startChain(baseSystem: StarSystemAPI)
    {
        latestSystems.clear()

        var name = "Sea of " + getName()
        var root = AbyssSystemLow(name, AbyssProcgen.Tier.Low).generate()
        AbyssUtils.setupTags(root)
        AbyssUtils.addAbyssSystemToMemory(root)

        var pos1 = Vector2f(750f, 750f)
        var pos2 = AbyssProcgen.takeEmptySlot(root)

        var fractures = AbyssUtils.createFractures(baseSystem, root)

        fractures.fracture1.location.set(pos1)
        fractures.fracture2.location.set(pos2)

      // baseSystem.addScript(AbyssalDefendingFleetManager(fractures.fracture1, 60f, 120f))
        //AbyssUtils.generateBaseDetails(root, AbyssProcgen.Tier.Low)
        AbyssUtils.clearTerrainAroundFractures(fractures)

        AbyssUtils.setNeighbours(baseSystem, root)
        AbyssUtils.setPreviousSystem(root, baseSystem)
        setMapLocation(baseSystem, 0)
        setMapLocation(root, 0)

        AbyssUtils.setTier(baseSystem, AbyssProcgen.Tier.Low)
        AbyssUtils.setTier(root, AbyssProcgen.Tier.Low)

        baseSystem.addScript(NewDepthReachedFactor(baseSystem))
        root.addScript(NewDepthReachedFactor(root))


        latestSystems.add(root)

        var time = measureTimeMillis {
            generateChain()
        }
        this.logger().debug("Generated Abyss Chain in ${time}ms and generated $totalSystems systems")

        var amount = totalSystems

        latestSystems.clear()
    }


    fun generateChain()
    {
        if (currentSteps > maxSteps) {
            generateAfterChain()
            return
        }

        var newLatest = ArrayList<StarSystemAPI>()
        for (latest in latestSystems)
        {

            var count = getAmountOfSystems()

            var step = currentSteps

            for (i in 0 until count)
            {

                var name = "Sea of " + getName()

                var tier = AbyssProcgen.Tier.Low
                if (currentSteps in 1..2) tier = AbyssProcgen.Tier.Mid
                if (currentSteps in 3..10) tier = AbyssProcgen.Tier.High

                var system = when(tier) {
                    AbyssProcgen.Tier.Low -> AbyssSystemLow(name, tier).generate()
                    AbyssProcgen.Tier.Mid -> AbyssSystemMid(name, tier).generate()
                    AbyssProcgen.Tier.High -> AbyssSystemHigh(name, tier).generate()
                }
                AbyssUtils.setupTags(system)
                //AbyssUtils.generateBaseDetails(system, tier)
                AbyssUtils.addAbyssSystemToMemory(system)
                system.addScript(NewDepthReachedFactor(system))


                var pos1 = AbyssProcgen.takeEmptySlot(latest)
                var pos2 = AbyssProcgen.takeEmptySlot(system)

                var fractures = AbyssUtils.createFractures(latest, system)

                fractures.fracture1.location.set(pos1)
                fractures.fracture2.location.set(pos2)

                AbyssUtils.clearTerrainAroundFractures(fractures)

                if (tier == AbyssProcgen.Tier.Low)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, tier, FleetTypes.PATROL_SMALL, 0.75f)
                }
                if (tier == AbyssProcgen.Tier.Mid)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, tier, FleetTypes.PATROL_MEDIUM, 0.6f)
                }
                else if (tier == AbyssProcgen.Tier.High)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, tier, FleetTypes.PATROL_MEDIUM, 0.75f)
                }


                AbyssUtils.setNeighbours(latest, system)
                AbyssUtils.setPreviousSystem(system, latest)
                setMapLocation(system, i)

                totalSystems += 1
                newLatest.add(system)
            }

            if (currentSteps == maxSteps)
            {
                reachedFinal = true
            }
        }
        systemsInSameStep.put(currentSteps, newLatest)
        currentSteps += 1
        latestSystems = newLatest
        generateChain()
    }

    fun generateAfterChain()
    {

        var maxTier2Outposts = 1
        var maxTier3Outposts = 2

        var tier2Systems = AbyssUtils.getAllAbyssSystems().filter { AbyssUtils.getTier(it) == AbyssProcgen.Tier.Mid }.toMutableList()
        for (i in 0 until maxTier2Outposts)
        {
            var pick = tier2Systems.random()
            AbyssProcgen.generateOutposts(pick)
            tier2Systems.remove(pick)
        }

        var tier3Systems = AbyssUtils.getAllAbyssSystems().filter { AbyssUtils.getTier(it) == AbyssProcgen.Tier.High }.toMutableList()
        for (i in 0 until maxTier3Outposts)
        {
            var pick = tier3Systems.random()
            AbyssProcgen.generateOutposts(pick)
            tier3Systems.remove(pick)
        }


        generateRift()

        generateFarFracture(3, true)
       // generateFarFracture(4, false)

    }

    fun generateRift() {
        var systems = AbyssUtils.getAllAbyssSystems().filter { !it.hasTag(AbyssUtils.RIFT_TAG) }
        systems = systems.filter { AbyssUtils.getTier(it) == AbyssProcgen.Tier.High }
        systems = systems.filter { AbyssProcgen.hasEmptySlots(it) }

        if (systems.isEmpty()) return

        var pick = systems.random()
        var location = AbyssProcgen.takeEmptySlot(pick)

        var riftSystem = AbyssProcgen.createRift(pick, location)
        var station = riftSystem.addCustomEntity("rift_station${Misc.genUID()}", "Rift Station", "rat_abyss_rift_station", "rat_abyssals")
        station.setLocation(0f, 0f)

        addWormBoss(station)

       /* var playerFleet = Global.getSector().playerFleet
        var currentLocation = playerFleet.containingLocation

        currentLocation.removeEntity(playerFleet)
        riftSystem.addEntity(playerFleet)
        Global.getSector().setCurrentLocation(riftSystem)
        playerFleet.location.set(Vector2f(0f, 0f))*/
    }

    fun addWormBoss(station: SectorEntityToken) {

        val params = FleetParamsV3(null,
            station.containingLocation.location,
            AbyssUtils.FACTION_ID,
            5f,
            FleetTypes.PATROL_MEDIUM,
            200f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            5f // qualityMod
        )
        params.withOfficers = false

        val fleet = FleetFactoryV3.createFleet(params)

        for (i in 0 until 3) {
            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_charybdis_head_standard")

            var core = PrimordialCore().createPerson("rat_primordial_core", AbyssUtils.FACTION_ID, Random())
            member.captain = core

            member.repairTracker.cr = member.repairTracker.maxCR
            fleet.fleetData.addFleetMember(member)
        }

        fleet.fleetData.sort()

        var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_charybdis_head_standard")

        var core = PrimordialCore().createPerson("rat_primordial_core", AbyssUtils.FACTION_ID, Random())
        member.captain = core

        member.repairTracker.cr = member.repairTracker.maxCR
        station.memoryWithoutUpdate.set("\$rewardShip", member)

       

        station.memoryWithoutUpdate.set("\$defenderFleet", fleet)
    }


    fun generateFarFracture(step: Int, useDistance: Boolean)
    {
        var tier = systemsInSameStep.get(step)!!.filter { AbyssProcgen.hasEmptySlots(it) }.toMutableList()
        var pick = tier.random()
        tier.remove(pick)

        var target: StarSystemAPI? = null
        var currentDistance = 0f
        var pickDis = AbyssUtils.getSystemLocation(pick)

        var pickNeighbours = AbyssUtils.getNeighbouringSystems(pick)

        if (!useDistance)
        {
            target = tier.filter { !pickNeighbours.contains(it) }.random()
        }

        if (useDistance)
        {
            for (system in tier.filter { !pickNeighbours.contains(it) })
            {
                var systemDis = AbyssUtils.getSystemLocation(system)
                var distance = MathUtils.getDistance(pickDis, systemDis)
                if (distance > currentDistance)
                {
                    currentDistance = distance
                    target = system
                }
            }
        }

        if (target == null) return

        var pos1 = AbyssProcgen.takeEmptySlot(pick)
        var pos2 = AbyssProcgen.takeEmptySlot(target)

        var fractures = AbyssUtils.createFractures(pick, target)

        fractures.fracture1.location.set(pos1)
        fractures.fracture2.location.set(pos2)

        AbyssUtils.clearTerrainAroundFractures(fractures)
        AbyssUtils.setFarNeighbours(pick, target)
    }

    fun getName() : String
    {
        var names = AbyssUtils.SYSTEM_NAMES.filter { !usedNames.contains(it) }
        var name = names.randomOrNull()
        if (name == null)
        {
            name = "the Abyss"
        }
        usedNames.add(name)
        return name
    }

    fun getAmountOfSystems() : Int {

        var systems = 1

         when (currentSteps) {
            0 -> systems = 2
            1 -> systems = 1
            2 -> systems = 1
            3 -> systems = 2
           /* 4 -> {
                 if (Random().nextFloat() > 0.5f && !generatedExtra)
                 {
                     systems = 2
                     generatedExtra = true
                 }
                 else
                 {
                     systems = 1
                 }
             }*/
             4 -> systems = 1

            else -> 1
        }

        return systems
    }

    /*fun getLocationForFractures(system: StarSystemAPI) : Vector2f
    {
        var existingEntities = system.customEntities
      //  var pos = Vector2f(MathUtils.getRandomNumberInRange(-FractureBoundary.x, FractureBoundary.x), MathUtils.getRandomNumberInRange(-FractureBoundary.y, FractureBoundary.y))

        var min = 4000f;
        var max = 15000f;

        var range = MathUtils.getRandomNumberInRange(min, max)
        var pos = Misc.getPointAtRadius(Vector2f(0f, 0f), range)

        for (existing in existingEntities)
        {
            var distance = MathUtils.getDistance(existing.location, pos)
            if (distance < 3000)
            {
                getLocationForFractures(system)
            }
        }

        return pos
    }*/


    fun setMapLocation(system: StarSystemAPI, currentBranch: Int, lastDistance: Float = 50f)
    {
        var previous = AbyssUtils.getPreviousSystem(system)
        if (previous == null)
        {
            AbyssUtils.setSystemLocation(system, Vector2f(0f, 0f))
            positionsOnMap.add(Vector2f(0f, 0f))
            return
        }

        var prevLoc = AbyssUtils.getSystemLocation(previous)
        if (previous.baseName == "Midnight")
        {
            var point = Vector2f(0f, 40f)
            positionsOnMap.add(point)
            AbyssUtils.setSystemLocation(system, point)
            return
        }

        var angle = Misc.getAngleInDegrees(Vector2f(0f, 0f), prevLoc)


        var randomDistance = 50f

        var angleAddition = 0f

        var wasPositive = false


        if (currentBranch == 0)
        {
            angleAddition = 60f
        }
        if (currentBranch == 1)
        {
            angleAddition = -60f
        }


        var newAngle = angle + angleAddition


        var point = MathUtils.getPointOnCircumference(prevLoc, randomDistance, newAngle)
        var failed = false
        for (existingPoint in positionsOnMap)
        {
            if (prevLoc == existingPoint) continue
            var distance = MathUtils.getDistance(point, existingPoint)
            if (distance < lastDistance)
            {
                if (lastDistance > 2)
                {
                    failed = true
                    break
                }
                else
                {
                   var test = ""
                }
            }
        }

        if (failed) setMapLocation(system, currentBranch, lastDistance - 0.5f)

        lastSystemWasPositiveOnMap = wasPositive
        positionsOnMap.add(point)
        AbyssUtils.setSystemLocation(system, point)
    }
}