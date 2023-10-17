package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.intel.AbyssMap
import assortment_of_things.abyss.procgen.types.DefaultAbyssType
import assortment_of_things.abyss.procgen.types.IonicStormAbyssType
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class  AbyssGenerator {

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

    var types = listOf<BaseAbyssType>(DefaultAbyssType(), IonicStormAbyssType())

    fun beginGeneration() {

        //Setting up base data
        var abyssData = AbyssData()
        Global.getSector().memoryWithoutUpdate.set(AbyssUtils.ABYSS_DATA_KEY, abyssData)

        var hyperspaceLocation = Vector2f(-20000f, -15000f)
        var orion = Global.getSector().hyperspace.customEntities.find { it.fullName.contains("Orion-Perseus") }
        if (orion != null) {
            hyperspaceLocation = orion.location.plus(Vector2f(0f, -1000f))
        }

        abyssData.hyperspaceLocation = hyperspaceLocation

        //Setting up base system
        var twilightSystem = Global.getSector().createStarSystem("Sea of Twilight")
        twilightSystem.name = "Sea of Twilight"
        abyssData.rootSystem = twilightSystem
        AbyssProcgen.setupSystem(twilightSystem, 0.35f, AbyssDepth.Shallow)
        var systemData = AbyssUtils.getSystemData(twilightSystem)
        AbyssProcgen.addAbyssParticles(twilightSystem)
        systemData.mapLocation = Vector2f(0f, 0f)

        //Hyperspace-Abyss Fracture
        var fractures = AbyssProcgen.createFractures(Global.getSector().hyperspace, twilightSystem)

        fractures.fracture1.location.set(hyperspaceLocation)
        fractures.fracture2.location.set(Vector2f(0f, 0f))


        val hyper = Misc.getHyperspaceTerrain().plugin as HyperspaceTerrainPlugin
        val editor = NebulaEditor(hyper)
        editor.clearArc(hyperspaceLocation.x, hyperspaceLocation.y, 0f, 500f, 0f, 360f)
        editor.clearArc(hyperspaceLocation.x,hyperspaceLocation.y, 0f, 500f, 0f, 360f, 0.25f)

        AbyssProcgen.clearTerrainAround(fractures.fracture2, 500f)

        //Generate Slots for the twilight system.
        AbyssProcgen.generateCircularPoints(twilightSystem)
        AbyssProcgen.generateMinorPoints(twilightSystem)
        AbyssEntityGenerator.generatePhotospheres(twilightSystem, 1, 1f)
        AbyssEntityGenerator.generateMinorEntity(twilightSystem, "rat_abyss_transmitter", 1, 1f)
        AbyssEntityGenerator.generateMinorEntityWithDefenses(twilightSystem, "rat_abyss_fabrication", 1, 0.9f, 0.7f)
        AbyssEntityGenerator.generateMinorEntity(twilightSystem, "rat_abyss_drone", 3, 0.8f)

        latestSystems.add(twilightSystem)

        var gate = twilightSystem.addCustomEntity("testgate", "Abyssal Gate", "inactive_gate", Factions.NEUTRAL)
        var gateLoc = systemData.majorPoints.randomAndRemove()
        gate.location.set(gateLoc)
        gate.addTag("rat_abyss_gate")

        //Map Script
        twilightSystem.addScript( object : EveryFrameScript {

            var done = false

            override fun isDone(): Boolean {
                return done
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {

                if (Global.getSector().playerFleet.containingLocation == twilightSystem)
                {
                    Global.getSector().intelManager.addIntel(AbyssMap())
                    done = true
                }
            }
        })


        //Fake icon system
        var iconSystem = Global.getSector().createStarSystem("Abyss")
        iconSystem.addTag(Tags.THEME_HIDDEN)
        iconSystem.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        //iconSystem.initNonStarCenter()
        //var nebula = iconSystem.addPlanet("abyss_icon_star", iconSystem.center, "Abyss", "rat_abyss_icon_star_2", 0f, 0f, 0f, 1f)
        var center = iconSystem.initStar("abyss_icon_star", "rat_abyss_hyperspace_icon", 0f, 0f)

        // nebula.isSkipForJumpPointAutoGen = true

        iconSystem.location.set(hyperspaceLocation)
        iconSystem.generateAnchorIfNeeded()

        iconSystem.autogenerateHyperspaceJumpPoints(false, false)

        var points = ArrayList<JumpPointAPI>()
        var pointEntities = Global.getSector().hyperspace.jumpPoints.filter { it is JumpPointAPI }
        if (pointEntities.isNotEmpty()) {
            points.addAll(pointEntities as MutableList<JumpPointAPI>)
        }

        var entrancePoint = points.find { it.destinations.any { it.destination.containingLocation == iconSystem } }
        entrancePoint!!.radius = 0f
        entrancePoint.name = "Abyssal Fracture"
        entrancePoint.addTag("rat_abyss_entrance")
        entrancePoint.clearDestinations()
        entrancePoint.memoryWithoutUpdate.set("\$rat_jumpoint_destination_override", fractures.fracture2)

        var beacon = Global.getSector().hyperspace.addCustomEntity("", "Warning Beacon", "rat_abyss_warning_beacon", Factions.NEUTRAL)
        beacon.setCircularOrbit(entrancePoint, MathUtils.getRandomNumberInRange(0f, 360f), 320f, 120f)

       //Gen
       generateSystems()

       latestSystems.clear()
   }

   fun generateSystems()
   {
       if (currentSteps > maxSteps) {
           generateAfterChain()
           return
       }

       var newLatest = ArrayList<StarSystemAPI>()
       for (latest in latestSystems)
       {

           var latestData = AbyssUtils.getSystemData(latest)
           var count = getAmountOfSystems()

           var step = currentSteps

           for (i in 0 until count)
           {

               var name = "Sea of " + getName()

               var depth = AbyssDepth.Shallow
               if (currentSteps in 3..10) depth = AbyssDepth.Deep

               var picker = WeightedRandomPicker<BaseAbyssType>()
               for (type in types) {
                   picker.add(type, type.getWeight())
               }
               var type = picker.pick()

               var system = Global.getSector().createStarSystem(name)
               system.name = name
               AbyssProcgen.setupSystem(system, type.getTerrainFraction(), depth)
               var systemData = AbyssUtils.getSystemData(system)

               type.pregenerate(systemData)

               var pos1 = latestData.fracturePoints.randomAndRemove()
               var pos2 = systemData.fracturePoints.randomAndRemove()

               var fractures = AbyssProcgen.createFractures(latest, system)

               latestData.fractures.add(fractures.fracture1.customPlugin as AbyssalFracture)
               systemData.fractures.add(fractures.fracture2.customPlugin as AbyssalFracture)

               fractures.fracture1.location.set(pos1)
               fractures.fracture2.location.set(pos2)

               AbyssProcgen.clearTerrainAroundFractures(fractures)

               var latestDepth = latestData.depth

               if (latestDepth == AbyssDepth.Shallow)
               {
                   AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.6f)
               }
               else if (latestDepth == AbyssDepth.Deep)
               {
                   AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.75f)
               }

               type.generate(systemData)


               systemData.previous = latest
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
       generateSystems()
   }

    fun generateAfterChain() {

        addLogsToTransmitters()
        generateRift()

        var systems = ArrayList(AbyssUtils.getAbyssData().systemsData.filter { it.minorPoints.isNotEmpty() } )

        var labSystems = systems.filter { it.depth == AbyssDepth.Deep }
        if (labSystems.isNotEmpty()) {
            var system = labSystems.random()
            systems.remove(system)
            AbyssEntityGenerator.generateMinorEntityWithDefenses(system.system, "rat_abyss_unknown_lab", 1, 1f, 1f)
        }

        var milOutpostSystems = systems.filter { it.depth == AbyssDepth.Deep }
        if (milOutpostSystems.isNotEmpty()) {
            var system = milOutpostSystems.random()
            systems.remove(system)
            AbyssEntityGenerator.generateMinorEntityWithDefenses(system.system, "rat_military_outpost", 1, 1f, 1f)
        }

        for (i in 0 until 2) {
            var system = systems.randomAndRemove()
            AbyssEntityGenerator.generateMinorEntityWithDefenses(system.system, "rat_abyss_research", 1, 1f, 1f)
        }


    }

    fun addLogsToTransmitters() {

        var data = AbyssUtils.getAbyssData()
        var transmitters = data.systemsData.map { it.system }.flatMap { system -> system.customEntities.filter { entity -> entity.customEntityType == "rat_abyss_transmitter" } }.toMutableList()

        var descriptions: MutableList<String> = ArrayList<String>()
        var index = 0
        var descriptionId = "rat_abyss_log"
        while (true) {
            var currentId = descriptionId + index
            var description = Global.getSettings().getDescription(currentId, Description.Type.CUSTOM)

            if (description.text1 == "No description... yet") {
                break
            }

            descriptions.add(currentId)
            index++
        }

        for (description in descriptions) {
            if (transmitters.isEmpty()) break;
            var pick = transmitters.randomAndRemove()
            var descriptionSpec = Global.getSettings().getDescription(description, Description.Type.CUSTOM)
            pick.memoryWithoutUpdate.set("\$rat_log_id", description)
            pick.addTag("rat_transmitter_has_log_${descriptionSpec.text1}")
        }
    }


    fun generateRift() {
        var systems = AbyssUtils.getAbyssData().systemsData
        var filtered = systems.filter { it.depth == AbyssDepth.Deep && it.uniquePoints.isNotEmpty() }

        if (filtered.isEmpty()) return

        var pick = filtered.random()
        var location = pick.uniquePoints.randomAndRemove()

        var riftSystem = RiftCreator.createRift(pick.system, location)
        var station = riftSystem.addCustomEntity("rift_station${Misc.genUID()}", "Rift Station", "rat_abyss_rift_station", "rat_abyssals")
        station.setLocation(0f, 0f)
        station.getSalvageSeed()

        addWormBoss(station)

        /* var playerFleet = Global.getSector().playerFleet
         var currentLocation = playerFleet.containingLocation

         currentLocation.removeEntity(playerFleet)
         riftSystem.addEntity(playerFleet)
         Global.getSector().setCurrentLocation(riftSystem)
         playerFleet.location.set(Vector2f(0f, 0f))*/
    }

    fun addWormBoss(station: SectorEntityToken) {


        var points = 300f
        if (AbyssUtils.getDifficulty() == AbyssDifficulty.Hard) {
            points += 50f
        }

        val params = FleetParamsV3(null,
            station.containingLocation.location,
            AbyssUtils.FACTION_ID,
            5f,
            FleetTypes.PATROL_MEDIUM,
            points,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            5f // qualityMod
        )
        params.withOfficers = false

        val fleet = FleetFactoryV3.createFleet(params)
        fleet.addTag("rat_boss_fleet")

        var seraphs = 5
        if (AbyssUtils.getDifficulty() == AbyssDifficulty.Hard) seraphs += 2

        AbyssalSeraphSpawner.addSeraphsToFleet(fleet, Random(), seraphs ,seraphs)


        for (member in fleet.fleetData.membersListCopy) {
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }
        AbyssUtils.addAlterationsToFleet(fleet, 0.8f, Random())
        AbyssalSeraphSpawner.sortWithSeraphs(fleet)

        /*  for (i in 0 until 3) {
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
          station.memoryWithoutUpdate.set("\$rewardShip", member)*/



        station.memoryWithoutUpdate.set("\$defenderFleet", fleet)
        fleet.inflateIfNeeded()
    }


   fun getName() : String
   {
       var names = AbyssProcgen.SYSTEM_NAMES.filter { !usedNames.contains(it) }
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

   fun setMapLocation(system: StarSystemAPI, currentBranch: Int, lastDistance: Float = 50f)
   {
       var data = AbyssUtils.getSystemData(system)
       var previous = data.previous
       if (previous == null)
       {
           data.mapLocation = Vector2f(0f, 0f)

           positionsOnMap.add(Vector2f(0f, 0f))
           return
       }

       var prevLoc = AbyssUtils.getSystemData(data.previous!!).mapLocation
       /*if (previous.name.contains("Twilight"))
       {
           var point = Vector2f(0f, 40f)
           positionsOnMap.add(point)

           data.mapLocation = point

           return
       }*/

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
       AbyssUtils.getSystemData(system).mapLocation = point
   }

}