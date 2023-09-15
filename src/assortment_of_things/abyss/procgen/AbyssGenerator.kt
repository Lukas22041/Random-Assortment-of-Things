package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.intel.AbyssMap
import assortment_of_things.abyss.procgen.types.BaseAbyssType
import assortment_of_things.abyss.procgen.types.DefaultAbyssType
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

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

    var types = listOf<BaseAbyssType>(DefaultAbyssType())

    fun beginGeneration() {

        //Setting up base data
        var abyssData = AbyssData()
        Global.getSector().memoryWithoutUpdate.set(AbyssUtils.ABYSS_DATA_KEY, abyssData)

        var hyperspaceLocation = Vector2f(-20000f, -15000f)
        var orion = Global.getSector().hyperspace.customEntities.find { it.fullName.contains("Orion-Perseus") }
        if (orion != null) {
            hyperspaceLocation = orion.location.plus(Vector2f(0f, -100f))
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
        AbyssProcgen.generateCircularSlots(twilightSystem)

        latestSystems.add(twilightSystem)



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
                AbyssProcgen.setupSystem(system, 0.35f, depth)
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
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, depth, 0.6f)
                }
                else if (latestDepth == AbyssDepth.Deep)
                {
                    AbyssProcgen.addDefenseFleetManager(fractures.fracture1, 1, depth, 0.75f)
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