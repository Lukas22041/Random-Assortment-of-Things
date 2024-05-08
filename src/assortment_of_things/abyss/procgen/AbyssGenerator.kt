package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.intel.event.DiscoveredPhotosphere
import assortment_of_things.abyss.intel.map.AbyssMap
import assortment_of_things.abyss.procgen.types.*
import assortment_of_things.abyss.terrain.AbyssTerrainInHyperspacePlugin
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost
import com.fs.starfarer.api.impl.campaign.ghosts.GBDartAround
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.util.*

class  AbyssGenerator {

    companion object {
        var noBranchTag = "rat_no_branch"
        var branchTag = "rat_abyss_branch"
        var finalTag = "rat_abyss_final"

        var systemsOnMainBranch = 8
        var deepSystemsBeginAt = 4
        var branches = 2
    }


    var totalSystems = 0

    var positionsOnMap = ArrayList<Vector2f>()

    var usedNames = ArrayList<String>()

    var types = listOf<BaseAbyssType>(DefaultAbyssType(), IonicStormAbyssType(), DarkAbyssType(), ColossalPhotosphereType())

    fun beginGeneration() {

        //Setting up base data
        var abyssData = AbyssData()
        Global.getSector().memoryWithoutUpdate.set(AbyssUtils.ABYSS_DATA_KEY, abyssData)

        var hyperspaceLocation = Vector2f(-30000f, -25000f)
        var orion = Global.getSector().hyperspace.customEntities.find { it.fullName.contains("Orion-Perseus") }
        if (orion != null) {
            hyperspaceLocation = orion.location.plus(Vector2f(0f, -1000f))
        }

        abyssData.hyperspaceLocation = hyperspaceLocation

        //Setting up base system
        var twilightSystem = Global.getSector().createStarSystem("Sea of Twilight")
        twilightSystem.name = "Sea of Twilight"
        abyssData.rootSystem = twilightSystem
        AbyssProcgen.setupSystem(twilightSystem, DefaultAbyssType(), AbyssDepth.Shallow)
        var systemData = AbyssUtils.getSystemData(twilightSystem)
        AbyssProcgen.addAbyssParticles(twilightSystem)

        systemData.mapLocation = Vector2f(0f, 250f)
        twilightSystem.addTag(noBranchTag)

        //Hyperspace-Abyss Fracture
        var fractures = AbyssProcgen.createFractures(Global.getSector().hyperspace, twilightSystem)

        abyssData.hyperspaceFracture = fractures.fracture1
        fractures.fracture1.location.set(hyperspaceLocation)
        fractures.fracture2.location.set(Vector2f(0f, 0f))

        fractures.fracture1.name = "The Abyssal Depths"

        val hyper = Misc.getHyperspaceTerrain().plugin as HyperspaceTerrainPlugin
        val editor = NebulaEditor(hyper)
        editor.clearArc(hyperspaceLocation.x, hyperspaceLocation.y, 0f, 500f, 0f, 360f)
        editor.clearArc(hyperspaceLocation.x,hyperspaceLocation.y, 0f, 500f, 0f, 360f, 0.25f)

        //Generates terrain around hyperspace
        generateAbyssTerrainInHyperspace(hyperspaceLocation)

        AbyssProcgen.clearTerrainAround(fractures.fracture2, 500f)

        //Generate Slots for the twilight system.
        AbyssProcgen.generateCircularPoints(twilightSystem)
        AbyssProcgen.generateMinorPoints(twilightSystem)
        AbyssEntityGenerator.generateMajorLightsource(twilightSystem, 1, 1f)
        AbyssEntityGenerator.generateMinorEntity(twilightSystem, "rat_abyss_transmitter", 1, 1f)
        AbyssEntityGenerator.generateMinorEntityWithDefenses(twilightSystem, "rat_abyss_fabrication", 1, 0.9f, 0.7f)
        AbyssEntityGenerator.generateMinorEntity(twilightSystem, "rat_abyss_drone", 3, 0.8f)

        /*var gate = twilightSystem.addCustomEntity("rat_abyss_gate", "Abyssal Gate", "inactive_gate", Factions.NEUTRAL)
        var gateLoc = systemData.majorPoints.randomAndRemove()
        gate.location.set(gateLoc)
        gate.addTag("rat_abyss_gate")
        AbyssProcgen.clearTerrainAround(gate, 350f)*/


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
        var iconSystem = Global.getSector().createStarSystem("Abyssal Depths")
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
        entrancePoint.name = "The Abyssal Depths"
        entrancePoint.addTag("rat_abyss_entrance")
        //entrancePoint.clearDestinations()
        entrancePoint.memoryWithoutUpdate.set("\$rat_jumpoint_destination_override", fractures.fracture2)

        var beacon = Global.getSector().hyperspace.addCustomEntity("", "Warning Beacon", "rat_abyss_warning_beacon", Factions.NEUTRAL)
        beacon.setCircularOrbit(entrancePoint, MathUtils.getRandomNumberInRange(0f, 360f), 320f, 120f)

       //Gen
       generateMainBranch(twilightSystem)

   }

    fun generateAbyssTerrainInHyperspace(location: Vector2f) {
        var hyper = Global.getSector().hyperspace

        val w = 200
        val h = 200

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }


        val nebula = hyper.addTerrain("rat_depths_in_hyper",
            OldBaseTiledTerrain.TileParams(string.toString(),
                w,
                h,
                "rat_terrain",
                "depths1",
                4,
                4,
                null))
        nebula.id = "rat_depths_in_hyper_${Misc.genUID()}"
        nebula.location.set(location)

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as AbyssTerrainInHyperspacePlugin
        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.75f)
        editor.regenNoise()

        //Clear all but a part on the right to make it less even
        editor.clearArc(location.x, location.y, nebulaPlugin.range * 0.70f, 100000f, 165f, 225f)
        editor.clearArc(location.x, location.y, nebulaPlugin.range * 0.85f, 100000f, 50f, 350f)
        editor.clearArc(location.x, location.y, nebulaPlugin.range, 100000f, 0f, 360f)
        editor.clearArc(location.x, location.y, 0f, nebulaPlugin.centerClearRadius, 0f, 360f)

        var gate = hyper.addCustomEntity("rat_abyss_gate", "Abyssal Gate", "rat_abyss_gate", Factions.NEUTRAL)
        var gateLoc = location.plus(Vector2f(700f, -300f))
        gate.location.set(gateLoc)
        gate.addTag("rat_abyss_gate")

        editor.clearArc(gateLoc.x, gateLoc.y, 0f, 300f, 0f, 360f)

        var clearLeft = Vector2f(-700f, 250f)
        editor.clearArc(clearLeft.x, clearLeft.y, 0f, 200f, 0f, 360f)

        var particleManager = hyper.addCustomEntity("rat_abyss_particle_manager_hyper_${Misc.genUID()}", "", "rat_abyss_in_hyper_particle_spawner", Factions.NEUTRAL)
        particleManager.location.set(location)
    }



   fun generateMainBranch(twilight: StarSystemAPI)
   {
       /*if (currentSteps > maxSteps) {
           generateAfterChain()
           return
       }*/

       var previous: StarSystemAPI = twilight

       for (step in 0 until systemsOnMainBranch) {
           var previousData = AbyssUtils.getSystemData(previous)

           var name = "Sea of " + getName()

           var depth = AbyssDepth.Shallow
           if (step >= deepSystemsBeginAt) depth = AbyssDepth.Deep

           var picker = WeightedRandomPicker<BaseAbyssType>()
           for (type in types) {
               picker.add(type, type.getWeight())
           }
           var type = picker.pick()

           var system = Global.getSector().createStarSystem(name)
           system.name = name

           var isFinal = false
           if (step == systemsOnMainBranch - 1) {
               AbyssUtils.getAbyssData().finalSystem = system
               system.addTag(finalTag)
               type = FinalAbyssType()
               isFinal = true
           }

           AbyssProcgen.setupSystem(system, type, depth, isFinal)
           var systemData = AbyssUtils.getSystemData(system)
           systemData.step = step

           if (step == systemsOnMainBranch || step == systemsOnMainBranch - 1 || step == systemsOnMainBranch - 2) {
               system.addTag(noBranchTag)
           }

           type.pregenerate(systemData)

           var pos1 = previousData.fracturePoints.randomAndRemove()
           var pos2 = systemData.fracturePoints.randomAndRemove()

           var fractures = AbyssProcgen.createFractures(previous, system)

           previousData.fractures.add(fractures.fracture1.customPlugin as AbyssalFracture)
           systemData.fractures.add(fractures.fracture2.customPlugin as AbyssalFracture)

           fractures.fracture1.location.set(pos1)
           fractures.fracture2.location.set(pos2)
           if (isFinal) {
               fractures.fracture1.addTag("rat_final_fracture")
           }

           AbyssProcgen.clearTerrainAroundFractures(fractures)

           var latestDepth = previousData.depth

           if (latestDepth == AbyssDepth.Shallow)
           {
               AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.6f)
           }
           else if (latestDepth == AbyssDepth.Deep)
           {
               AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.75f)
           }

           type.generate(systemData)


           systemData.previous = previous
           setMainBranchMapLocation(system)

           totalSystems += 1
           previous = system
       }

       generateBranches()
   }

    fun generateBranches() {
        var systems = ArrayList(AbyssUtils.getAbyssData().systemsData).filter { !it.system.hasTag(noBranchTag) }.toMutableList()

        var hasDeepBranch = false
        var hasShallowBranch = false

        for (i in 0 until branches) {
            var current: AbyssSystemData? = null
            if (current == null) {

                if (!hasDeepBranch) {
                    current = systems.filter { it.depth == AbyssDepth.Deep }.toMutableList().random()
                    systems.remove(current)
                    hasDeepBranch = true
                }
                else if (!hasShallowBranch) {
                    current = systems.filter { it.depth == AbyssDepth.Shallow }.toMutableList().random()
                    systems.remove(current)
                    hasShallowBranch = true
                }
                else {
                    current = systems.randomAndRemove()
                }

            }

            var length = MathUtils.getRandomNumberInRange(2, 3)

            for (e in 0 until length) {
                var previousData = AbyssUtils.getSystemData(current!!.system)

                var name = "Sea of " + getName()

                var depth = current.depth

                var picker = WeightedRandomPicker<BaseAbyssType>()
                for (type in types) {
                    picker.add(type, type.getWeight())
                }
                var type = picker.pick()

                var system = Global.getSector().createStarSystem(name)
                system.name = name
                AbyssProcgen.setupSystem(system, type, depth)
                var systemData = AbyssUtils.getSystemData(system)
                systemData.step = current.step

                system.addTag(branchTag)

                type.pregenerate(systemData)

                var pos1 = previousData.fracturePoints.randomAndRemove()
                var pos2 = systemData.fracturePoints.randomAndRemove()

                var fractures = AbyssProcgen.createFractures(current.system, system)

                previousData.fractures.add(fractures.fracture1.customPlugin as AbyssalFracture)
                systemData.fractures.add(fractures.fracture2.customPlugin as AbyssalFracture)

                fractures.fracture1.location.set(pos1)
                fractures.fracture2.location.set(pos2)

                AbyssProcgen.clearTerrainAroundFractures(fractures)

                var latestDepth = previousData.depth

                if (latestDepth == AbyssDepth.Shallow)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.6f)
                }
                else if (latestDepth == AbyssDepth.Deep)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, latestDepth, 0.75f)
                }

                type.generate(systemData)


                systemData.previous = current.system
                setSubBranchMapLocation(system)

                totalSystems += 1
                current = systemData
            }
        }

        generateAfterChain()
    }

    fun generateAfterChain() {

        addLogsToTransmitters()
       // generateRift()

        var systems = ArrayList(AbyssUtils.getAbyssData().systemsData.filter { it.minorPoints.isNotEmpty() } )

        /*var labSystems = systems.filter { it.depth == AbyssDepth.Deep }
        if (labSystems.isNotEmpty()) {
            var system = labSystems.random()
            systems.remove(system)
            AbyssEntityGenerator.generateMinorEntityWithDefenses(system.system, "rat_abyss_unknown_lab", 1, 1f, 1f)
        }*/

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

        var majorLightSourceInDeep = systems.filter { it.depth == AbyssDepth.Deep }.flatMap { it.system.customEntities.filter { it.hasTag("rat_abyss_major_lightsource") } }.toMutableList()
        if (majorLightSourceInDeep.isNotEmpty()) {
            var majorLightsource = majorLightSourceInDeep.random()
            majorLightSourceInDeep.remove(majorLightsource)

            var entity = AbyssEntityGenerator.spawnMinorEntity(majorLightsource.starSystem, "rat_sariel_outpost")
            entity.setCircularOrbit(majorLightsource, MathUtils.getRandomNumberInRange(0f, 360f), MathUtils.getRandomNumberInRange(600f, 700f), 120f)
        }

        if (Global.getSettings().modManager.isModEnabled("secretsofthefrontier")) {
            var systemsWithUniquePoints = systems.filter { it.system != AbyssUtils.getAbyssData().rootSystem && it.uniquePoints.isNotEmpty() && it.system.customEntities.any { it.customPlugin is AbyssalPhotosphere }}

            if (systemsWithUniquePoints.isNotEmpty()) {

                var system = systemsWithUniquePoints.random()
                var pos = system.uniquePoints.randomAndRemove()

                var photosphere = system.system.addCustomEntity("rat_abyss_photosphere_sierra_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere_sierra", Factions.NEUTRAL)
                photosphere.setLocation(pos.x, pos.y)
                photosphere.radius = 100f
                photosphere.addScript(DiscoveredPhotosphere(10, photosphere))

                var plugin = photosphere.customPlugin as AbyssalPhotosphere
                // plugin.radius = 15000f
                plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)
                plugin.color = AbyssUtils.SIERRA_COLOR


                photosphere.memoryWithoutUpdate.set("\$rat_photosphere_color_overwrite", AbyssUtils.SIERRA_COLOR)
                photosphere.addTag("rat_abyss_sierra")

                // sensor ghosts
                for (i in 0 until 3) {
                    val g = BaseSensorGhost(null, 0)
                    g.initEntity(g.genMediumSensorProfile(), g.genSmallRadius(), 0, system.system)
                    g.addBehavior(GBDartAround(photosphere,
                        9999f,
                        8 + Misc.random.nextInt(4),
                        photosphere.radius + 200f,
                        2500f))
                    g.despawnRange = -1f
                    g.entity.addTag("sotf_AMDancingGhost")
                    g.setLoc(Misc.getPointAtRadius(photosphere.location, 1200f))
                    //g.placeNearEntity(tia.getHyperspaceAnchor(), 800, 3200);
                    system.system.addScript(g)
                }

                val params = DerelictShipEntityPlugin.createVariant("rat_raphael_Hull", Random(), DerelictShipEntityPlugin.getDefaultSModProb())
                val raphael = BaseThemeGenerator.addSalvageEntity(Random(), system.system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
                raphael.setDiscoverable(true)

                raphael.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), 280f, 90f)

                raphael.addTag("rat_abyss_sierra_raphael")

                var drone = AbyssEntityGenerator.spawnMinorEntity(system.system, "rat_abyss_drone")
                drone.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), 550f, 120f)
            }

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



    fun setMainBranchMapLocation(system: StarSystemAPI)
    {
        var data = AbyssUtils.getSystemData(system)
        var previous = data.previous

        var root = AbyssUtils.getSystemData(AbyssUtils.getAbyssData().rootSystem!!)
        var prevLoc = AbyssUtils.getSystemData(data.previous!!).mapLocation


        var angle = MathUtils.getRandomNumberInRange(-65f, 65f)
        var randomDistance = MathUtils.getRandomNumberInRange(80f, 90f)

      /*  var testPoint = MathUtils.getPointOnCircumference(prevLoc, randomDistance, angle)
        var angleTowardsStart = Misc.getAngleInDegrees(Vector2f(0f, 0f), testPoint)

        while (angle + 90 in (angleTowardsStart - 40)..(angleTowardsStart + 40)) {
            if (angle > 0) {
                angle++
            }
            if (angle < 0) {
                angle--
            }
        }*/

        if (previous == root.system) {
            angle = MathUtils.getRandomNumberInRange(-30f, 30f)
        }

        angle -= 90


        var lastDistance = 50f
        var point = MathUtils.getPointOnCircumference(prevLoc, randomDistance, angle)




        positionsOnMap.add(point)
        AbyssUtils.getSystemData(system).mapLocation = point
    }

    //Ugly ass Code but hopefully works
    fun setSubBranchMapLocation(system: StarSystemAPI, circleRadius: Float = 110f) : ArrayList<Vector2f>
    {
        var data = AbyssUtils.getSystemData(system)
        var previous = data.previous
        var prevLoc = AbyssUtils.getSystemData(data.previous!!).mapLocation

        var allOtherLocations = AbyssUtils.getAbyssData().systemsData.map { it.mapLocation }

        var points = ArrayList<Vector2f>()
        var pointCount = 100
        for (point in 0 until pointCount) {
            var angle = (360f / pointCount) * point
            var pos = MathUtils.getPoint(prevLoc, MathUtils.getRandomNumberInRange(60f, 90f), angle)

            var inRadius = false
            for (other in allOtherLocations) {
                if (other == prevLoc) continue
                if (prevLoc!!.x == other!!.x && prevLoc.y == other.y) continue
                if (MathUtils.isPointWithinCircle(pos, other, circleRadius)) {
                    inRadius = true
                    break
                }
            }
            if (!inRadius)  {
                points.add(pos)
            }
        }

        if (points.isEmpty()) {
            if (circleRadius <= 20) {
                points.add(MathUtils.getRandomPointOnCircumference(prevLoc, 400f))
            }
            else {
                points = setSubBranchMapLocation(system, circleRadius - 10f)
            }
        }

        var point = points.random()



        positionsOnMap.add(point)
        AbyssUtils.getSystemData(system).mapLocation = point
        return points
    }

}