package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.abyss.misc.FlickerUtilV2Abyssal
import assortment_of_things.abyss.procgen.*
import assortment_of_things.abyss.scripts.AbyssFleetScript
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.campaign.scripts.SimUnlockerListener
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import second_in_command.SCUtils
import java.awt.Color
import java.util.*

class SeaOfSolitude() : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return "sea_of_solitude"
    }

    override fun getDisplayName(): String {
        return "Sea of Solitude"
    }

    private var biomeColor = Color(255, 0, 100)
    private var darkBiomeColor = Color(77, 0, 31)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun hasSeraphs(): Boolean {
        return true
    }

    override fun getCombatNebulaTex() = "graphics/terrain/rat_combat/rat_combat_depths_solitude.png"
    override fun getCombatNebulaMapTex() = "graphics/terrain/rat_combat/rat_combat_depths_map_solitude.png"

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Exotic matter moves violently and non-stop throughout the biome, leading to an enviroment that is constantly under the influence of charged particles. \n\n" +
                "" +
                "Extreme Storms, that span the entire biome, occur approximately every 3 days. Anything not hidden within the dense fog of the abyss will appear visible to the sensors of everything in its surroundings. ", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Extreme Storms", "3", "dense fog")
    }

    var stormHandler = SolitudeStormHandler(this)

    /** Called after all cells are generated */
    override fun init() {

        LunaCampaignRenderer.addRenderer(stormHandler)

        var system = AbyssUtils.getSystem()!!

        generateFogTerrain("rat_sea_of_solitude", "rat_terrain", "depths1", 0.45f)

        var photosphereNum = MathUtils.getRandomNumberInRange(12, 14)

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
            if (Random().nextFloat() >= 0.1f) {
                AbyssProcgenUtils.clearTerrainAround(terrain as BaseFogTerrain, entity, MathUtils.getRandomNumberInRange(550f, 1200f))
            }

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }

        generateLightsourceOrbits()
        populateEntities()
    }


    fun spawnMiniboss() {
        var orbit = pickOrbit(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 })
        if (orbit == null) return

        var complex = AbyssProcgenUtils.spawnEntity(system, this,"rat_abyss_research_complex")
        complex.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)

        var faction = "rat_abyssals_solitude"
        var fleet = Global.getFactory().createEmptyFleet(faction, "Protectors",false)
        complex.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        var gabriel = fleet.fleetData.addFleetMember("rat_gabriel_Attack")

        fleet.fleetData.addFleetMember("rat_sariel_Attack")
        fleet.fleetData.addFleetMember("rat_sariel_Attack")
        fleet.fleetData.addFleetMember("rat_sariel_Strike")

        fleet.fleetData.addFleetMember("rat_raguel_Attack")
        fleet.fleetData.addFleetMember("rat_raguel_Attack")
        fleet.fleetData.addFleetMember("rat_raguel_Attack")
        fleet.fleetData.addFleetMember("rat_raguel_Strike")
        fleet.fleetData.addFleetMember("rat_raguel_Strike")

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
            member.captain = SeraphCore().createPerson(RATItems.SERAPH_CORE, faction, Random())
        }

        AbyssFleetEquipUtils.inflate(fleet, 0.5f, 1f, 2f)

        for (member in fleet.fleetData.membersListCopy) {
            member.updateStats()
            member.repairTracker.cr = member.repairTracker.maxCR
        }

        if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
            SCUtils.getFleetData(fleet) //Generate Skills
        }
    }



    fun populateEntities() {

        var wreckFaction = "rat_abyssals_solitude"
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

        //Sensor Array can be either orbit or random loc, to make them slightly more difficult to find
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
      /*  var station = AbyssProcgenUtils.createResearchStation(system, this)
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
        }*/

        var orbitPicks = WeightedRandomPicker<String>(random)
        orbitPicks.add("rat_abyss_accumalator",1f)
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
            }
        }

        var unclaimedCellPicks = WeightedRandomPicker<String>(random)
        unclaimedCellPicks.add("rat_abyss_accumalator",0.75f)
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
                entity = AbyssProcgenUtils.createRandomDerelictAbyssalShip(system, wreckFaction)
            }

            entity.setLocation(loc.x, loc.y)

            if (entityPick == "rat_abyss_accumalator") {
                if (random.nextFloat() >= 0.5f) {
                    spawnDefenseFleet(entity)
                }
                //AbyssProcgenUtils.addLightsourceWithBiomeColor(entity, this, 2500f, 15)
            }
        }

    }





    override fun spawnDefenseFleet(source: SectorEntityToken, fpMult: Float) : CampaignFleetAPI {
        var random = Random()
        var factionID = "rat_abyssals_solitude"
        var fleetType = FleetTypes.PATROL_MEDIUM

        var loc = source.location
        var homeCell = manager.getCell(loc.x, loc.y)
        var depth = homeCell.intDepth

        var depthLevel = getDepthLevel(depth)

        var basePoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.SOLITUDE_MIN_BASE_FP, AbyssFleetStrengthData.SOLITUDE_MAX_BASE_FP)
        //var scaledPoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.SOLITUDE_MIN_SCALED_FP, AbyssFleetStrengthData.SOLITUDE_MAX_SCALED_FP) * depthLevel

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

        //Limited to non caps, prefers using frigs and destroyers and many ships per fleet
        params.maxShipSize = 3
        var doctrine = Global.getSector().getFaction(factionID).doctrine.clone()
        doctrine.shipSize = 3
        //if (random.nextFloat() >= 0.5f) doctrine.shipSize = 2
        doctrine.numShips = 5
        params.doctrineOverride = doctrine

        val fleet = FleetFactoryV3.createFleet(params)

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }

        fleet.inflateIfNeeded()

        AbyssUtils.initAbyssalFleetBehaviour(fleet, random)

        //Stronger cores on border
        AbyssFleetEquipUtils.addAICores(fleet, AbyssFleetStrengthData.SOLITUDE_AI_CORE_CHANCE)

        var alterationChancePerShip = AbyssFleetStrengthData.SOLITUDE_ALTERATION_CHANCE + (0.05f * depth)
        AbyssFleetEquipUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        var zeroSmodWeight = AbyssFleetStrengthData.SOLITUDE_ZERO_SMODS_WEIGHT
        var oneSmodWeight = AbyssFleetStrengthData.SOLITUDE_ONE_SMODS_WEIGHT
        var twoSmodWeight = AbyssFleetStrengthData.SOLITUDE_TWO_SMODS_WEIGHT
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

























    override fun spawnParticlesForCell(particleManager: BiomeParticleManager, cell: BiomeCellData) {
        //super.spawnParticlesForCell(particleManager, cell)

        var count = 7
        var fadeInOverwrite = false
        if (particleManager.particles.size <= 100) {
            count *= 4
            fadeInOverwrite = true
        }

        for (i in 0 until count) {
            var velocity = Vector2f(0f, 0f)
            velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

            var color = getParticleColor()

            //var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
            var spawnLocation = cell.getWorldCenter()

            var spread = AbyssBiomeManager.cellSize * 1f/** 0.75f*/
            if (cell.depth == BiomeDepth.BORDER) spread *= 0.9f
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 3.5f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.25f)

            var size = MathUtils.getRandomNumberInRange(25f, 50f)

            var alpha = MathUtils.getRandomNumberInRange(0.25f, 0.45f)

            particleManager.particles.add(BiomeParticleManager.AbyssalLightParticle(
                this,
                fadeIn,duration, fadeOut,
                color, alpha, size, spawnLocation, velocity,
                IntervalUtil(0.5f, 0.75f), MathUtils.getRandomNumberInRange(-1f, 1f), -1f, -1f))
        }
    }

}

class SolitudeStormHandler(var solitude: SeaOfSolitude) : LunaCampaignRenderingPlugin {

    @Transient
    var thunder: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")

    override fun isExpired(): Boolean {
        return false
    }

    var stormSensorMultIncrease = 1.5f

    var stormInterval = IntervalUtil(27f, 33f)
    var stormDuration = 0f

    var flicker1 = FlickerUtilV2Abyssal(0.5f)
    var flicker2 = FlickerUtilV2Abyssal(0.7f)
    //var flicker2 = FlickerUtilV2(4f)

    init {
        flicker1.numBursts = 7
        flicker2.numBursts = 3
    }


    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()
        var cell = manager.getPlayerCell()
        var player = Global.getSector().playerFleet

        if (stormDuration <= 0 && dominant == solitude && cell.depth != BiomeDepth.BORDER) stormInterval.advance(amount) //Only advance if not storming
        if (stormInterval.intervalElapsed()) {
            stormDuration = MathUtils.getRandomNumberInRange(10f, 16f)
            stormInterval.advance(0f)
        }

        stormDuration -= 1 * amount

        //Only advance while in the biome, or if it has to finish flashing
        if (stormDuration <= 0f || dominant != solitude || cell.depth == BiomeDepth.BORDER) {
            flicker1.stopAll = true
            flicker2.stopAll = true
        } else {
            flicker1.stopAll = false
            flicker2.stopAll = false
        }

        flicker1.advance(amount * 0.15f)
        flicker2.advance(amount * 0.20f)

        var brightness = (flicker1.brightness * 0.7f) + (flicker2.brightness * 0.3f)
        if (brightness != 0f) {
            for (fleet in AbyssUtils.getSystem()!!.fleets) {
                var inClouds = (solitude.terrain as BaseFogTerrain).isInClouds(fleet)
                if (!inClouds) {
                    fleet.stats.addTemporaryModFlat(0.1f, "rat_extreme_storm", "Extreme Storm", 100 * (stormSensorMultIncrease * brightness), fleet.stats.detectedRangeMod)
                    fleet.stats.addTemporaryModMult(0.1f, "rat_extreme_storm", "Extreme Storm", 1f + (stormSensorMultIncrease * brightness), fleet.stats.detectedRangeMod)
                }
            }
        }

        //flicker2.advance(amount)

        if (flicker1.isPeakFrame) {
            Global.getSoundPlayer().playSound("rat_abyss_solitude_storm_sounds", 0.8f, 1.5f, player.location, Vector2f())
        }
    }

    var layers = EnumSet.of(CampaignEngineLayers.TERRAIN_5)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layers
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        if (thunder == null) {
            thunder = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")
        }

        var llx = viewport.llx - 100
        var lly = viewport.lly - 100

        var width = viewport.visibleWidth + 200
        var height = viewport.visibleHeight + 200

        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.175f * flicker1.brightness /** flicker2.brightness*/
        thunder!!.color = solitude.getBiomeColor()
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)


        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.05f * flicker2.brightness /** flicker2.brightness*/
        thunder!!.color = Color(255, 220, 220)
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)
    }

}