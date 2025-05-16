package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.hyper.AbyssalFracture
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.procgen.*
import assortment_of_things.abyss.scripts.AbyssFleetScript
import assortment_of_things.abyss.terrain.AbyssTerrainInHyperspacePlugin
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.campaign.scripts.SimUnlockerListener
import assortment_of_things.misc.addPara
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
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
import org.magiclib.kotlin.setAlpha
import second_in_command.SCUtils
import java.awt.Color
import java.util.Random

//Starting Biome
class SeaOfTranquility() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_tranquility"
    }

    override fun getDisplayName(): String {
        return "Sea of Tranquility"
    }

    private var biomeColor = Color(255, 0, 50)
    private var darkBiomeColor = Color(77, 0, 15)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("An endless sea of exotic matter, being always in motion. It shows characteristics not to disimiliar to hyperspace, but it shows just as many differences. ")
    }

    /** Called after all cells are generated */
    override fun init() {

        var system = AbyssUtils.getSystem()!!

        generateFogTerrain("rat_sea_of_tranquility", "rat_terrain", "depths1", 0.55f)

        var entrance = generateHyperspaceEntrance() //Pick entrance first

       /* var fleet = spawnFleet(entrance)
        fleet.setLocation(fleet.location.x + 500f, fleet.location.y + 500f)*/

        var photosphereNum = MathUtils.getRandomNumberInRange(13, 16)

        for (i in 0 until photosphereNum) {

            var cell: BiomeCellData? = pickAndClaimAdjacentOrSmaller() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

            majorLightsources.add(entity)

            //Have some photospheres with cleared terrain, some not.
            if (Random().nextFloat() >= 0.4f) {
                AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(250f, 800f))
            }

            entity.sensorProfile = 1f
        }

        generateLightsourceOrbits()
        populateEntities()


    }

    //Worse than any other biome
    override fun getLootMult(): Float {
        return 0.75f
    }

    fun spawnMiniboss() {
        var orbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 })
        if (orbit == null) return

        var shipyard = AbyssProcgenUtils.spawnEntity(system, this,"rat_abyssal_shipyard")
        shipyard.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)

        var faction = "rat_abyssals"
        var fleet = Global.getFactory().createEmptyFleet(faction, "Protectors",false)
        shipyard.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        var morkoth = fleet.fleetData.addFleetMember("rat_morkoth_Anchor")
        var abolethM = fleet.fleetData.addFleetMember("rat_aboleth_m_Attack")
        var abolethF = fleet.fleetData.addFleetMember("rat_aboleth_Attack")
        var chull1 = fleet.fleetData.addFleetMember("rat_chuul_Attack")
        var chull2 = fleet.fleetData.addFleetMember("rat_chuul_Strike")
        var chull3 = fleet.fleetData.addFleetMember("rat_chuul_Support")
        var makara1 = fleet.fleetData.addFleetMember("rat_makara_Attack")
        var makara2 = fleet.fleetData.addFleetMember("rat_makara_Attack")
        var merrow1 = fleet.fleetData.addFleetMember("rat_merrow_Support")
        var merrow2 = fleet.fleetData.addFleetMember("rat_merrow_Support")

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }

        morkoth.captain = ChronosCore().createPerson(RATItems.CHRONOS_CORE, faction, Random())

        abolethM.captain = ChronosCore().createPerson(RATItems.CHRONOS_CORE, faction, Random())
        abolethF.captain = CosmosCore().createPerson(RATItems.COSMOS_CORE, faction, Random())

        chull1.captain = ChronosCore().createPerson(RATItems.COSMOS_CORE, faction, Random())
        chull2.captain = CosmosCore().createPerson(RATItems.COSMOS_CORE, faction, Random())

        morkoth.variant.addMod("rat_qualityAssurance")

        for (member in fleet.fleetData.membersListCopy) {
            member.updateStats()
            member.repairTracker.cr = member.repairTracker.maxCR
        }

        if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
            SCUtils.getFleetData(fleet) //Generate Skills
        }
    }

    //TODO Procgen
    // - Worse rewards than other biomes
    // - Abyssal XO
    // - Wrecks of Hegemony & Tritach fleets
    // - Fabricators
    // - Beacons
    // - Droneships
    // - No Seraphs

    fun populateEntities() {

        var wreckFaction = "rat_abyssals"
        var random = Random()

        //Spawn Orbital fleets around lightsources
        for (lightsource in majorLightsources) {
            var maxFleets = 2
            var spawnChancePer = /*0.75f*/ 0.66f
            for (i in 0 until maxFleets) {
                if (random.nextFloat() >= spawnChancePer) continue
                spawnDefenseFleet(lightsource)
            }
        }

        spawnMiniboss()

        /*var sensorOrbit = pickOrbitByDepth(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 })
        if (sensorOrbit != null) {
            sensorOrbit.setClaimedByMajor()
            var sensor = AbyssProcgenUtils.createSensorArray(system, this)
            sensor.setCircularOrbit(sensorOrbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), sensorOrbit.distance, sensorOrbit.orbitDays)
        }*/

        var sensor = AbyssProcgenUtils.createSensorArray(system, this)
        if (random.nextFloat() >= 0.5f) {
            var researchOrbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 })
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
            var researchOrbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 || it.index == 1 })
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

        //Abyssal XO wreck
        if (Global.getSettings().modManager.isModEnabled("second_in_command") && !Global.getSector().characterData.memoryWithoutUpdate.contains("\$rat_started_abyss")) {
            var abyssXOOrbit = pickOrbit(lightsourceOrbits.filter { it.index != 0 })
            if (abyssXOOrbit != null) {
                var drone = AbyssProcgenUtils.createAbyssalDrone(system, this)
                drone.addTag("rat_abyssal_xo_entity")
                drone.setCircularOrbit(abyssXOOrbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), abyssXOOrbit.distance, abyssXOOrbit.orbitDays)
            }
        }

        var orbitPicks = WeightedRandomPicker<String>(random)
        orbitPicks.add("rat_abyss_fabrication",1f)
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
                    entity = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, wreckFaction, isNoLarges = true)
                }
                entity.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)
            }
        }

        //Graveyard of hegemony and/or tritach ships
        var graveyardCell = pickAndClaimCell()
        if (graveyardCell != null) {

            var onslaught = AbyssProcgenUtils.createDerelictShip(system, "onslaught_Standard")
            var loc = graveyardCell.getWorldCenterWithCircleOffset(AbyssBiomeManager.cellSize*0.25f)
            onslaught.setLocation(loc.x, loc.y)

            var variants = Global.getSettings().hullIdToVariantListMap.get("rat_morkoth")
            var wreck = AbyssProcgenUtils.createDerelictAbyssalShip(system, variants!!.random())
            loc = MathUtils.getRandomPointOnCircumference(loc, MathUtils.getRandomNumberInRange(100f, 200f))
            wreck.location.set(loc.x, loc.y)

            for (i in 0 until MathUtils.getRandomNumberInRange(6, 7)) {
                //var loc = graveyardCell.getWorldCenterWithCircleOffset(AbyssBiomeManager.cellSize*0.9f)
                var loc = graveyardCell.getWorldCenterWithCircleOffset(AbyssBiomeManager.cellSize*0.8f)
                if (random.nextFloat() >= 0.5f) {
                    wreck = AbyssProcgenUtils.createRandomDerelictShip(system, "hegemony", isNoLarges = true)
                } else {
                    wreck = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, isNoLarges = true)
                }
                wreck.setLocation(loc.x, loc.y)
            }
        }


       /* //Guarantee a Morkoth wreck somewhere //Now part of the graveyard
        var morkothCell = pickAndClaimCell()
        if (morkothCell != null) {
            var loc = morkothCell.getRandomLocationInCell()
            var variants = Global.getSettings().hullIdToVariantListMap.get("rat_morkoth")
            var wreck = AbyssProcgenUtils.createDerelictAbyssalShip(system, variants!!.random())
            wreck.location.set(loc.x, loc.y)
        }*/

        var unclaimedCellPicks = WeightedRandomPicker<String>(random)
        unclaimedCellPicks.add("rat_abyss_fabrication",0.75f)
        unclaimedCellPicks.add("rat_abyss_drone",0.5f)
        unclaimedCellPicks.add("rat_abyss_transmitter",1f)
        unclaimedCellPicks.add("wreck",1f)

        //Populate locations without anything major near them.
        //Fabricators, Transmitters, Droneships, Abyssal Wrecks
        var picks = MathUtils.getRandomNumberInRange(7, 10)
        for (i in 0 until picks) {
            var pick = pickAndClaimCellIncludingBorder() ?: continue //Populate Border regions too
            var loc = pick.getRandomLocationInCell()

            var entityPick = unclaimedCellPicks.pick()

            var entity: SectorEntityToken? = null

            if (entityPick != "wreck") {
                entity = AbyssProcgenUtils.spawnEntity(system, this, entityPick)
            } else {
                entity = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, wreckFaction, isNoLarges = true)
            }

            entity.setLocation(loc.x, loc.y)

            if (entityPick == "rat_abyss_fabrication") {
                if (random.nextFloat() >= 0.5f) {
                    spawnDefenseFleet(entity)
                }
                //AbyssProcgenUtils.addLightsourceWithBiomeColor(entity, this, 2500f, 15)
            }
        }

    }









    override fun spawnDefenseFleet(source: SectorEntityToken, fpMult: Float) : CampaignFleetAPI {
        var random = Random()
        var factionID = "rat_abyssals"
        var fleetType = FleetTypes.PATROL_MEDIUM

        var loc = source.location
        var homeCell = manager.getCell(loc.x, loc.y)
        var depth = homeCell.intDepth

        var depthLevel = getDepthLevel(depth)
        var invertedlevel = 1-depthLevel


        var basePoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.TRANQUILITY_MIN_BASE_FP, AbyssFleetStrengthData.TRANQUILITY_MAX_BASE_FP)
        //For Tranquility, it should actually get harder further for the center, unlike other biomes.
        var scaledPoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.TRANQUILITY_MIN_SCALED_FP, AbyssFleetStrengthData.TRANQUILITY_MAX_SCALED_FP) * invertedlevel

        var points = (basePoints + scaledPoints) * fpMult

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

        //if (random.nextFloat() >= 0.7f) {
        //Tranquility should have no capitals
        params.maxShipSize = 3
        //}

        val fleet = FleetFactoryV3.createFleet(params)

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }

        fleet.inflateIfNeeded()

        AbyssUtils.initAbyssalFleetBehaviour(fleet, random)

        //Stronger cores on border
        AbyssFleetEquipUtils.addAICores(fleet, AbyssFleetStrengthData.TRANQUILITY_AI_CORE_CHANCE)

        var alterationChancePerShip = AbyssFleetStrengthData.TRANQUILITY_ALTERATION_CHANCE + (0.05f * invertedlevel)
        AbyssFleetEquipUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        var zeroSmodWeight = AbyssFleetStrengthData.TRANQUILITY_ZERO_SMODS_WEIGHT
        var oneSmodWeight = AbyssFleetStrengthData.TRANQUILITY_ONE_SMODS_WEIGHT + (0.5f*invertedlevel)
        var twoSmodWeight = AbyssFleetStrengthData.TRANQUILITY_TWO_SMODS_WEIGHT
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





















    fun generateHyperspaceEntrance() : SectorEntityToken {
        var manager = AbyssUtils.getBiomeManager()
        var system = AbyssUtils.getSystem()
        var hyperspace = Global.getSector().hyperspace
        var data = AbyssUtils.getData()

        //Reserve cell and claim surrounding tiles
        var cell = pickAndClaimDeep()!!
        cell.getSurrounding().forEach { it.claimed = true }

        var hyperspaceLocation = Vector2f(-30000f, -25000f)
        var orion = Global.getSector().hyperspace.customEntities.find { it.fullName.contains("Orion-Perseus") }
        if (orion != null) {
            hyperspaceLocation = orion.location.plus(Vector2f(0f, -1000f))
        }

        system!!.location.set(hyperspaceLocation) //Set the abyssal depths locations to match the hyperspace loc

        //Generates terrain around hyperspace
        generateAbyssTerrainInHyperspace(hyperspaceLocation)


        var entrance = hyperspace.addCustomEntity("abyss_fracture_entrance", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)
        var exit = system.addCustomEntity("abyss_fracture_exit", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)

        data.hyperspaceFracture = entrance
        data.abyssFracture = exit

        entrance.location.set(hyperspaceLocation)
        exit.location.set(cell.getWorldCenterWithCircleOffset(AbyssBiomeManager.cellSize/2f))

        var entrancePlugin = entrance.customPlugin as AbyssalFracture
        var exitPlugin = exit.customPlugin as AbyssalFracture

        var entranceCol = AbyssUtils.ABYSS_COLOR.setAlpha(40)
        var exitCol = getBiomeColor().setAlpha(40)

        entrancePlugin.connectedEntity = exit
        exitPlugin.connectedEntity = entrance

        entrancePlugin.colorOverride = entranceCol.darker()
        exitPlugin.colorOverride = exitCol.darker()

        AbyssProcgenUtils.addLightsource(entrance, 10000f, entranceCol)
        AbyssProcgenUtils.addLightsource(exit, 10000f, exitCol)

        AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, exit, 500f)

        //Set up fake system
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


        //Modify jump points
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
        entrancePoint.memoryWithoutUpdate.set("\$rat_jumpoint_destination_override", exit)

        var beacon = Global.getSector().hyperspace.addCustomEntity("", "Warning Beacon", "rat_abyss_warning_beacon", Factions.NEUTRAL)
        beacon.setCircularOrbit(entrancePoint, MathUtils.getRandomNumberInRange(0f, 360f), 320f, 120f)

        return exit
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
        editor.noisePrune(0.70f)
        editor.regenNoise()

        //Clear all but a part on the right to make it less even
        editor.clearArc(location.x, location.y, nebulaPlugin.range * 0.70f, 100000f, 165f, 225f)
        editor.clearArc(location.x, location.y, nebulaPlugin.range * 0.85f, 100000f, 50f, 350f)
        editor.clearArc(location.x, location.y, nebulaPlugin.range, 100000f, 0f, 360f)
        editor.clearArc(location.x, location.y, 0f, nebulaPlugin.centerClearRadius, 0f, 360f)

       /* var gate = hyper.addCustomEntity("rat_abyss_gate", "Abyssal Gate", "rat_abyss_gate", Factions.NEUTRAL)
        var gateLoc = location.plus(Vector2f(700f, -300f))
        gate.location.set(gateLoc)
        gate.addTag("rat_abyss_gate")

        editor.clearArc(gateLoc.x, gateLoc.y, 0f, 300f, 0f, 360f)*/

        var clearLeft = Vector2f(-700f, 250f)
        editor.clearArc(clearLeft.x, clearLeft.y, 0f, 200f, 0f, 360f)

        var clearRight = Vector2f(Vector2f(700f, -300f))
        editor.clearArc(clearRight.x, clearRight.y, 0f, 200f, 0f, 360f)

        var particleManager = hyper.addCustomEntity("rat_abyss_particle_manager_hyper_${Misc.genUID()}", "", "rat_abyss_in_hyper_particle_spawner", Factions.NEUTRAL)
        particleManager.location.set(location)
    }

}