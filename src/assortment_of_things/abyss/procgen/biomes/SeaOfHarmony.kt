package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalColossalPhotosphere
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.*
import assortment_of_things.abyss.scripts.AbyssFleetScript
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.campaign.scripts.SimUnlockerListener
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//System with a large Photosphere illuminating it
class SeaOfHarmony() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "sea_of_harmony"
    }

    override fun getDisplayName(): String {
        return "Sea of Harmony"
    }

    private var biomeColor = Color(255, 64, 50)
    private var darkBiomeColor = Color(102, 25, 20)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun getCombatNebulaTex() = "graphics/terrain/rat_combat/rat_combat_depths_harmony.png"
    override fun getCombatNebulaMapTex() = "graphics/terrain/rat_combat/rat_combat_depths_map_harmony.png"

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A biome indiscrible with any other word than \"warm\". Colossal photospheres are spread throughout, illuminating almost every location within.", 0f)
    }

    override fun getVignetteLevel(): Float {
        return 0.5f
    }

    override fun getSaturation(): Float {
        return 1.1f
    }


    /*//Dont Spawn any photosphere to close to primordial waters
    fun pickAndClaimAroundNoOtherBiomeAndNoPrim(radius: Int) : BiomeCellData? {
        var list = ArrayList<BiomeCellData>()
        for (cell in getUnclaimedCells()) {
            if (cell.getAround(radius).any { it.claimed || it.getBiome() != cell.getBiome() }) continue
            if (cell.getAround(radius+2).any { it.getBiome() is PrimordialWaters}) continue
            list.add(cell)
        }
        var pick = list.randomOrNull() ?: return null

        pick.claimed = true
        pick.getAround(radius).forEach { it.claimed = true }

        return pick
    }*/

    /** Called after all cells are generated */
    override fun init() {
        var system = AbyssUtils.getSystem()

        generateFogTerrain("rat_sea_of_harmony", "rat_terrain", "depths1", 0.6f)

        var photosphereNum = (MathUtils.getRandomNumberInRange(8, 8) * manager.scaleMult).toInt()

        //Spawn an even larger colossal first.
        var first = true
        for (i in 0 until photosphereNum) {

            //Claim all cells around a larger pattern
            var cell: BiomeCellData? = null

            if (first) {
                cell = pickAndClaimDeep() ?: break
                cell!!.getAround(3).forEach {
                    it.claimed = true
                }
            } else {
                cell = pickAndClaimAroundNoOtherBiome(3) ?: break
            }

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_colossal_photosphere_${Misc.genUID()}", "Colossal Photosphere", "rat_abyss_colossal_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 600f
            if (first) entity.radius += 400f

            var plugin = entity.customPlugin as AbyssalLight
            var lightRadius = MathUtils.getRandomNumberInRange(entity.radius + 41500f, entity.radius + 43000f)
            if (first) lightRadius += 4000f
            plugin.radius = lightRadius

            majorLightsources.add(entity)

            //Have some photospheres with cleared terrain, some not.
            AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(entity.radius + 200f, entity.radius + 1200f))

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/

            if (first) {
                entity.addTag("rat_supersized_colossal")
            }

            first = false

        }

        generateLightsourceOrbits()
        populateEntities()
    }

    override fun generateLightsourceOrbits() {
        for (lightsource in majorLightsources) {
            var orbit = lightsource.radius + MathUtils.getRandomNumberInRange(100f, 150f)
            var days = 50f
            var cell = manager.getCell(lightsource)
            var depth = cell.intDepth
            for (i in 0 until 7) {
                orbit += MathUtils.getRandomNumberInRange(200f, 500f)
                days += MathUtils.getRandomNumberInRange(40f, 45f)
                lightsourceOrbits.add(LightsourceOrbit(this, lightsource, orbit, days, depth, i))
            }
        }
    }


    fun populateEntities() {

        var wreckFaction = "rat_abyssals_harmony"
        var random = Random()

        //Spawn Orbital fleets around lightsources
        for (lightsource in majorLightsources) {
            var maxFleets = 3
            var spawnChancePer = /*0.75f*/ 0.45f
            for (i in 0 until maxFleets) {
                if (random.nextFloat() >= spawnChancePer) continue
                spawnDefenseFleet(lightsource)
            }
        }

        //Sensor Array can be either orbit or random loc, to make them slightly more difficult to find
        var sensor = AbyssProcgenUtils.createSensorArray(system, this)
        if (random.nextFloat() >= 0.5f) {
            var researchOrbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor()  })
            if (researchOrbit != null) {
                researchOrbit.setClaimedByMajor()
                sensor.setCircularOrbit(researchOrbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), researchOrbit.distance, researchOrbit.orbitDays)
            }
        } else {
            var pick = pickAndClaimCell()
            if (pick != null) {
                var loc = pick.getRandomLocationInCell()
                sensor.setLocation(loc.x, loc.y)
                if (random.nextFloat() >= 0.25f) {
                    spawnDefenseFleet(sensor)
                }
            }
        }

        //Research station can be either orbit or random loc
        var station = AbyssProcgenUtils.createResearchStation(system, this)
        if (random.nextFloat() >= 0.5f) {
            var researchOrbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor() })
            if (researchOrbit != null) {
                researchOrbit.setClaimedByMajor()
                station.setCircularOrbit(researchOrbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), researchOrbit.distance, researchOrbit.orbitDays)
            }
        } else {
            var pick = pickAndClaimCell()
            if (pick != null) {
                var loc = pick.getRandomLocationInCell()
                station.setLocation(loc.x, loc.y)
            }
        }

        var orbitPicks = WeightedRandomPicker<String>(random)
        orbitPicks.add("rat_abyss_fabrication",0.75f)
        orbitPicks.add("rat_abyss_accumalator",0.75f)
        orbitPicks.add("rat_abyss_drone",3.5f)
        orbitPicks.add("rat_abyss_transmitter",0.75f)
        orbitPicks.add("wreck",0.5f)

        //Iterate over remaining orbits, randomly place things within them.
        for (orbit in ArrayList(lightsourceOrbits)) {
            if (random.nextFloat() > /*0.25f*/ 0.3f) {
                lightsourceOrbits.remove(orbit)

                var entityPick = orbitPicks.pick()
                var entity: SectorEntityToken? = null

                if (entityPick != "wreck") {
                    entity = AbyssProcgenUtils.spawnEntity(system, this, entityPick)
                } else {
                    entity = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, wreckFaction)
                }
                entity.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)

                if (entityPick == "rat_abyss_fabrication" || entityPick == "rat_abyss_accumalator") {
                    if (random.nextFloat() >= 0.75f) {
                        spawnDefenseFleet(entity)
                    }
                }
            }
        }

        var unclaimedCellPicks = WeightedRandomPicker<String>(random)
        unclaimedCellPicks.add("rat_abyss_fabrication",1f)
        unclaimedCellPicks.add("rat_abyss_accumalator",1f)
        unclaimedCellPicks.add("rat_abyss_drone",0.5f)
        unclaimedCellPicks.add("rat_abyss_transmitter",1f)
        unclaimedCellPicks.add("wreck",1f)

        //Populate locations without anything major near them.
        //Fabricators, Transmitters, Droneships, Abyssal Wrecks
        var picks = (MathUtils.getRandomNumberInRange(11, 13) * manager.scaleMult).toInt()
        for (i in 0 until picks) {
            var pick = pickAndClaimCellIncludingBorder() ?: continue //Populate Border regions too
            var loc = pick.getRandomLocationInCell()

            var entityPick = unclaimedCellPicks.pick()

            var entity: SectorEntityToken? = null

            if (entityPick != "wreck") {
                entity = AbyssProcgenUtils.spawnEntity(system, this, entityPick)
            } else {
                entity = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, wreckFaction)
            }

            entity.setLocation(loc.x, loc.y)

            if (entityPick == "rat_abyss_fabrication" || entityPick == "rat_abyss_accumalator") {
                if (random.nextFloat() >= 0.33f) {
                    spawnDefenseFleet(entity)
                }
            }
        }

    }






    override fun spawnDefenseFleet(mainSource: SectorEntityToken, fpMult: Float) : CampaignFleetAPI {
        var random = Random()
        var factionID = "rat_abyssals_harmony"
        var fleetType = FleetTypes.PATROL_MEDIUM

        var sourcePlugin = mainSource.customPlugin

        //Create a token that can have different radius to the photosphere, this allows defense fleets that patrol from further away
        var source: SectorEntityToken? = null
        if (sourcePlugin is AbyssalColossalPhotosphere) {
            /*source  = system.addCustomEntity("rat_abyss_token${Misc.genUID()}", "", "rat_abyss_token", Factions.NEUTRAL)
            source.setCircularOrbit(mainSource, 0.1f, 0.1f, 999f)
            source.radius = MathUtils.clamp(MathUtils.getRandomNumberInRange(0f, sourcePlugin.radius / 10 - mainSource.radius), mainSource.radius, Float.MAX_VALUE)*/
            source = AbyssProcgenUtils.createPatrolToken(mainSource, system, this, mainSource.radius/2, sourcePlugin.radius / 10, 5f, 10f)
        } else {
            source = mainSource
        }

        var loc = source!!.location
        var homeCell = manager.getCell(loc.x, loc.y)
        var depth = homeCell.intDepth

        //var depthLevel = getDepthLevel(depth)

        var basePoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.HARMONY_MIN_BASE_FP, AbyssFleetStrengthData.HARMONY_MAX_BASE_FP)
        //var scaledPoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.SERENITY_MIN_SCALED_FP, AbyssFleetStrengthData.SERENITY_MAX_SCALED_FP) * depthLevel

        var points = (basePoints /*+ scaledPoints*/) * fpMult

        var factionAPI = Global.getSector().getFaction(factionID)

        val params = FleetParamsV3(null,
            source.locationInHyperspace,
            factionID,
            5f,
            fleetType,
            points,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )
        params.random = random
        params.withOfficers = false

        var doctrine = Global.getSector().getFaction(factionID).doctrine.clone()
        doctrine.numShips = 2
        params.doctrineOverride = doctrine

        val fleet = FleetFactoryV3.createFleet(params)

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }

        fleet.inflateIfNeeded()

        AbyssUtils.initAbyssalFleetBehaviour(fleet, random)

        //Stronger cores on border
        AbyssFleetEquipUtils.addAICores(fleet, AbyssFleetStrengthData.HARMONY_AI_CORE_CHANCE)

        var alterationChancePerShip = AbyssFleetStrengthData.HARMONY_ALTERATION_CHANCE + (0.05f * depth)
        AbyssFleetEquipUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        var zeroSmodWeight = AbyssFleetStrengthData.HARMONY_ZERO_SMODS_WEIGHT
        var oneSmodWeight = AbyssFleetStrengthData.HARMONY_ONE_SMODS_WEIGHT
        var twoSmodWeight = AbyssFleetStrengthData.HARMONY_TWO_SMODS_WEIGHT
        AbyssFleetEquipUtils.inflate(fleet, zeroSmodWeight, oneSmodWeight, twoSmodWeight)

        fleet.addEventListener(SimUnlockerListener("rat_abyssals_sim"))

        system.addEntity(fleet)
        fleet.setLocation(loc.x, loc.y)

        fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f

        system.addScript(AbyssFleetScript(fleet, source, this))

        return fleet
    }




}