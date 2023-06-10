package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.AbyssalFracture
import assortment_of_things.abyss.procgen.templates.TestSystemTemplate
import assortment_of_things.misc.logger
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class AbyssChainGenerator {

    var maxSteps = 5
    var currentSteps = 0

    var reachedFinal = false
    var totalSystems = 0

    var latestSystems = ArrayList<StarSystemAPI>()
    private final var FractureBoundary = Vector2f(16000f, 16000f)

    var usedNames = ArrayList<String>()

    fun startChain(baseSystem: StarSystemAPI)
    {
        latestSystems.clear()

        var name = getName()
        var root = TestSystemTemplate(name).generate()
        AbyssUtils.setupTags(root)
        AbyssUtils.addAbyssSystemToMemory(root)

        var pos1 = Vector2f(750f, 750f)
        var pos2 = findUnusedFractureLocation(root, 20000f)

        var fractures = AbyssUtils.createFractures(baseSystem, root)

        fractures.fracture1.location.set(pos1)
        fractures.fracture2.location.set(pos2)

        AbyssUtils.clearTerrainAroundFractures(fractures)

        latestSystems.add(root)

        var time = measureTimeMillis {
            generateChain()
        }
        this.logger().debug("Generated Abyss Chain in ${time}ms and generated $totalSystems systems")

        latestSystems.clear()

        var test1 = time
        var test2 = time
    }


    fun generateChain()
    {
        if (currentSteps > maxSteps) return



        var newLatest = ArrayList<StarSystemAPI>()
        for (latest in latestSystems)
        {

            var count = getAmountOfSystems()

            for (i in 0 until count)
            {
                var name = getName()
                var system = TestSystemTemplate(name).generate()
                AbyssUtils.setupTags(system)
                AbyssUtils.addAbyssSystemToMemory(system)

                var pos1 = findUnusedFractureLocation(latest, 15000f)
                var pos2 = findUnusedFractureLocation(system, 15000f)

                var fractures = AbyssUtils.createFractures(latest, system)

                fractures.fracture1.location.set(pos1)
                fractures.fracture2.location.set(pos2)

                AbyssUtils.clearTerrainAroundFractures(fractures)

                totalSystems += 1
                newLatest.add(system)
            }

            if (currentSteps == maxSteps)
            {
                reachedFinal = true
            }

        }
        currentSteps += 1
        latestSystems = newLatest
        generateChain()
    }

    fun getName() : String
    {
        var names = AbyssUtils.SYSTEM_NAMES.filter { !usedNames.contains(it) }
        var name = names.randomOrNull()
        if (name == null)
        {
            name = "Abyss"
        }
        usedNames.add(name)
        return name
    }

    fun getAmountOfSystems() : Int {

        var rng = Random.nextFloat()

        return when (currentSteps) {
            0 -> 2
            1 -> 1
            2 -> 1
            3 -> 2
            4 -> 1
            5 -> 1
            else -> 1
        }

        var count = 1

    }

    fun findUnusedFractureLocation(system: StarSystemAPI, minDistance: Float) : Vector2f
    {
        var existingFractures = system.customEntities.filter { it.customPlugin is AbyssalFracture }
        var pos = Vector2f(MathUtils.getRandomNumberInRange(-FractureBoundary.x, FractureBoundary.x), MathUtils.getRandomNumberInRange(-FractureBoundary.y, FractureBoundary.y))
        for (existing in existingFractures)
        {
            var distance = MathUtils.getDistance(existing.location, pos)
            if (distance < minDistance)
            {
                if (minDistance < 250) return pos
                findUnusedFractureLocation(system, minDistance - 200f)
            }
        }

        return pos
    }

}