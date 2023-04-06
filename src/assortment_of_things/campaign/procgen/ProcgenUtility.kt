package assortment_of_things.campaign.procgen

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.StarSystemData
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

object ProcgenUtility
{
    @JvmStatic
    fun getSortedAvailableConstellations(context: ThemeGenContext, emptyOk: Boolean, sortFrom: Vector2f?, exclude: List<Constellation>?): List<Constellation>? {
        val constellations: MutableList<Constellation> = ArrayList()
        for (c in context.constellations) {
            if (context.majorThemes.containsKey(c)) continue
            if (!emptyOk && constellationIsEmpty(c)) continue
            constellations.add(c)
        }
        if (exclude != null) {
            constellations.removeAll(exclude)
        }
        constellations.sortWith(Comparator { o1, o2 ->
            val d1 = Misc.getDistance(o1.location, sortFrom)
            val d2 = Misc.getDistance(o2.location, sortFrom)
            Math.signum(d2 - d1).toInt()
        })
        return constellations
    }

    fun getAvailableConstellationsWithFilter(context: ThemeGenContext, filter: (Constellation) -> Boolean) : List<Constellation>
    {
        val constellations: MutableList<Constellation> = ArrayList()
        for (constellation in context.constellations)
        {
            if (context.majorThemes.containsKey(constellation)) continue
            if (constellationIsEmpty(constellation)) continue
            if (!filter(constellation)) continue
            constellations.add(constellation)
        }
        return constellations
    }

    fun constellationIsEmpty(c: Constellation): Boolean {
        for (s in c.systems) {
            if (!systemIsEmpty(s)) return false
        }
        return true
    }

    fun systemIsEmpty(system: StarSystemAPI): Boolean {
        for (p in system.planets) {
            if (!p.isStar) return false
        }
        //system.getTerrainCopy().isEmpty()
        return true
    }

    fun getScoredSystemsByFilter(data: List<StarSystemData>, filter: (StarSystemData) -> Boolean) : List<StarSystemData>
    {
        val result: MutableList<BaseThemeGenerator.StarSystemData> = ArrayList()

        for (dat in data)
        {
            var hasNonGasGiant = false
            dat.system.planets.forEach { if (!it.hasTag(Tags.GAS_GIANT)) hasNonGasGiant = true }
            if (!hasNonGasGiant) continue
            if (filter(dat))
            {
                result.add(dat)
            }
        }

        return result.sortedByDescending { getMainCenterScore(it) }
    }

    @JvmStatic
    fun getSortedSystemsSuitedToBePopulated(systems: List<BaseThemeGenerator.StarSystemData>, minPlanets: Int = 4): List<BaseThemeGenerator.StarSystemData>? {
        val result: MutableList<BaseThemeGenerator.StarSystemData> = ArrayList()
        for (data in systems) {
            var hasNonGasGiant = false
            if (data.isBlackHole || data.isNebula || data.isPulsar) continue
            data.system.planets.forEach { if (!it.hasTag(Tags.GAS_GIANT)) hasNonGasGiant = true }
            if (!hasNonGasGiant) continue
            if (data.planets.size >= minPlanets || data.habitable.size >= 1) {
                result.add(data)

//				Collections.sort(data.habitable, new Comparator<PlanetAPI>() {
//					public int compare(PlanetAPI o1, PlanetAPI o2) {
//						return (int) Math.signum(o1.getMarket().getHazardValue() - o2.getMarket().getHazardValue());
//					}
//				});
            }
        }
        systems.toMutableList().sortWith(Comparator { o1, o2 ->
            val s1: Float = getMainCenterScore(o1)
            val s2: Float = getMainCenterScore(o2)
            Math.signum(s2 - s1).toInt()
        })
        return result
    }

    @JvmStatic
    fun getMainCenterScore(data: BaseThemeGenerator.StarSystemData): Float {
        var total = 0f
        total += data.planets.size * 1f
        total += data.habitable.size * 2f
        total += data.resourceRich.size * 0.25f
        return total
    }

    @JvmStatic
    fun addBeacon(beaconEntity: String, system: StarSystemAPI, glowColor: Color, pingColor: Color): CustomCampaignEntityAPI? {
        val anchor = system.hyperspaceAnchor
        val points: List<SectorEntityToken?> = Global.getSector().hyperspace.getEntities(JumpPointAPI::class.java) as List<SectorEntityToken?>
        val minRange = 600f
        var closestRange = Float.MAX_VALUE
        var closestPoint: JumpPointAPI? = null
        for (entity in points) {
            val point = entity as JumpPointAPI?
            if (point!!.destinations.isEmpty()) continue
            val dest = point.destinations[0]
            if (dest.destination.containingLocation !== system) continue
            val dist = Misc.getDistance(anchor.location, point.location)
            if (dist < minRange + point.radius) continue
            if (dist < closestRange) {
                closestPoint = point
                closestRange = dist
            }
        }
        val beacon = Global.getSector().hyperspace.addCustomEntity(null, null, beaconEntity, Factions.NEUTRAL)

        //beacon.getMemoryWithoutUpdate().set("$remnant", true);
        /*beacon.memoryWithoutUpdate[type.beaconFlag] = true
        when (type) {
            RATRemnantThemeGenerator.RemnantSystemType.DESTROYED -> beacon.addTag(Tags.BEACON_LOW)
            RATRemnantThemeGenerator.RemnantSystemType.SUPPRESSED -> beacon.addTag(Tags.BEACON_MEDIUM)
            RATRemnantThemeGenerator.RemnantSystemType.RESURGENT -> beacon.addTag(Tags.BEACON_HIGH)
        }*/
        if (closestPoint == null) {
            val orbitDays = minRange / (10f + StarSystemGenerator.random.nextFloat() * 5f)
            //beacon.setCircularOrbit(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
            beacon.setCircularOrbitPointingDown(anchor,
                StarSystemGenerator.random.nextFloat() * 360f,
                minRange,
                orbitDays)
        } else {
            val angleOffset = 20f + StarSystemGenerator.random.nextFloat() * 20f
            val angle = Misc.getAngleInDegrees(anchor.location, closestPoint.location) + angleOffset
            val radius = closestRange
            if (closestPoint.orbit != null) {
//				OrbitAPI orbit = Global.getFactory().createCircularOrbit(anchor, angle, radius,
//																closestPoint.getOrbit().getOrbitalPeriod());
                val orbit = Global.getFactory()
                    .createCircularOrbitPointingDown(anchor, angle, radius, closestPoint.orbit.orbitalPeriod)
                beacon.orbit = orbit
            } else {
                val beaconLoc = Misc.getUnitVectorAtDegreeAngle(angle)
                beaconLoc.scale(radius)
                Vector2f.add(beaconLoc, anchor.location, beaconLoc)
                beacon.location.set(beaconLoc)
            }
        }

        Misc.setWarningBeaconColors(beacon, glowColor, pingColor)
        return beacon
    }
}