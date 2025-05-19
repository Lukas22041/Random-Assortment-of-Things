package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.abyss.procgen.*
import assortment_of_things.abyss.scripts.AbyssFleetScript
import assortment_of_things.campaign.scripts.SimUnlockerListener
import assortment_of_things.misc.addPara
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager.FabricatorEscortStrength
import com.fs.starfarer.api.impl.combat.threat.ThreatFIDConfig
import com.fs.starfarer.api.impl.combat.threat.ThreatFleetBehaviorScript
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//System with no fog, threat is rampant and its generaly very dark
class AbyssalWastes() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "abyssal_wastes"
    }

    override fun getDisplayName(): String {
        return "Abyssal Wastes"
    }

    private var biomeColor = Color(30, 30, 30)
    private var darkBiomeColor = Color(10, 10, 10)
    private var tooltipColor = Color(150, 140, 140)
    private var systemLightColor = Color(25, 25, 25)
    private var particleColor = Color(168, 146, 145)

    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    override fun getTooltipColor(): Color {
        return tooltipColor
    }

    override fun getSystemLightColor(): Color {
        return systemLightColor
    }

    override fun getParticleColor(): Color {
        return particleColor
    }

    override fun hasCombatNebula(): Boolean {
        return false
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A biome almost devoid any signs of activity. Irregular but frequent pulses of energy can be faintly detected from multiple sources.")
    }

    override fun getGridAlphaMult(): Float {
        return 0.25f
    }

    override fun getSaturation(): Float {
        return 0.75f
    }

    override fun getMaxDarknessMult(): Float {
        return 0.5f
    }

    override fun hasDeactivatedDroneshipObjective(): Boolean {
        return false
    }

    override fun getMusicKeyId(): String {
        return "music_campaign_abyssal"
    }

    /** Called after all cells are generated */
    override fun init() {

        var system = AbyssUtils.getSystem()

       //generateFogTerrain("rat_abyss_test", "rat_terrain", "depths1", 0.6f)


        var photosphereNum = MathUtils.getRandomNumberInRange(10, 13)

        for (i in 0 until photosphereNum) {

            //Unlike other biomes, try to force more spacing
            var cell: BiomeCellData? = pickAndClaimSurroundingOrSmaller() ?: break

            var loc = cell!!.getWorldCenter().plus(MathUtils.getRandomPointInCircle(Vector2f(), AbyssBiomeManager.cellSize * 0.5f))

            var entity = system!!.addCustomEntity("rat_abyss_decyaing_photosphere_${Misc.genUID()}", "Decaying Photosphere", "rat_abyss_decaying_photosphere", Factions.NEUTRAL)
            entity.setLocation(loc.x, loc.y)
            entity.radius = 100f

            var plugin = entity.customPlugin as AbyssalLight
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)

            majorLightsources.add(entity)

            entity.sensorProfile = 1f
            /*entity.setDiscoverable(true)
            entity.detectedRangeMod.modifyFlat("test", 5000f)*/
        }

        generateLightsourceOrbits()
        populateEntities()

    }












    fun populateEntities() {

        var wreckFaction = "rat_abyssals_wastes"
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

        //Sensor Array should always be near a photosphere in the wastes
        var sensorOrbit = pickOrbitByDepth(lightsourceOrbits.filter { !it.isClaimedByMajor() && it.index == 0 })
        if (sensorOrbit != null) {
            sensorOrbit.setClaimedByMajor()
            var sensor = AbyssProcgenUtils.createSensorArray(system, this)
            sensor.setCircularOrbit(sensorOrbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), sensorOrbit.distance, sensorOrbit.orbitDays)
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

        var orbitPicks = WeightedRandomPicker<String>(random)
        orbitPicks.add("rat_abyss_fabrication",1f)
        orbitPicks.add("rat_abyss_drone",3.5f)
        orbitPicks.add("rat_abyss_transmitter",0.75f)
        //orbitPicks.add("wreck",0.5f)

        //Iterate over remaining orbits, randomly place things within them.
        for (orbit in ArrayList(lightsourceOrbits)) {
            if (random.nextFloat() > /*0.25f*/ 0.3f) {
                lightsourceOrbits.remove(orbit)

                var entityPick = orbitPicks.pick()
                var entity = AbyssProcgenUtils.spawnEntity(system, this, entityPick)

                entity.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)
            }
        }

        var unclaimedCellPicks = WeightedRandomPicker<String>(random)
        unclaimedCellPicks.add("rat_abyss_fabrication",0.75f)
        unclaimedCellPicks.add("rat_abyss_drone",0.5f)
        unclaimedCellPicks.add("rat_abyss_transmitter",1f)
        //unclaimedCellPicks.add("wreck",1f)

        //Populate locations without anything major near them.
        //Fabricators, Transmitters, Droneships, Abyssal Wrecks
        var picks = MathUtils.getRandomNumberInRange(7, 10)
        for (i in 0 until picks) {
            var pick = pickAndClaimCell() ?: continue //Should not spawn things of this biome near the border
            var loc = pick.getRandomLocationInCell()

            var entityPick = unclaimedCellPicks.pick()

            var entity = AbyssProcgenUtils.spawnEntity(system, this, entityPick)

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
        var factionID = "rat_abyssals_wastes"
        var fleetType = FleetTypes.PATROL_MEDIUM

        var loc = source.location
        var homeCell = manager.getCell(loc.x, loc.y)
        var depth = homeCell.intDepth

        var depthLevel = getDepthLevel(depth)

        var basePoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.SOLITUDE_MIN_BASE_FP, AbyssFleetStrengthData.SOLITUDE_MAX_BASE_FP)
        //var scaledPoints = MathUtils.getRandomNumberInRange(AbyssFleetStrengthData.SOLITUDE_MIN_SCALED_FP, AbyssFleetStrengthData.SOLITUDE_MAX_SCALED_FP) * depthLevel

        var points = (basePoints /*+ scaledPoints*/) * fpMult

        var factionAPI = Global.getSector().getFaction(factionID)

        var picker = WeightedRandomPicker<FabricatorEscortStrength>()
        picker.add(FabricatorEscortStrength.LOW, 5f)
        picker.add(FabricatorEscortStrength.MEDIUM, 5.5f)
        picker.add(FabricatorEscortStrength.HIGH, 4.5f)
        var strength = picker.pick()

        val fleet = createThreatFleet(0, 0, 0, strength, null)

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)

            if (member.baseOrModSpec().hasTag("rat_abyss_threat")) {
                var core =  SeraphCore().createPerson(RATItems.SERAPH_CORE, fleet.faction.id, random)
                member.captain = core
                member.repairTracker.cr += 0.15f


            }

        }

        fleet.inflateIfNeeded()

        AbyssUtils.initAbyssalFleetBehaviour(fleet, random)

        //TODO Add Seraph cores to the seraph threat

        var alterationChancePerShip = 0.25f
        AbyssFleetEquipUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        var zeroSmodWeight = 0.4f
        var oneSmodWeight = 1.25f
        var twoSmodWeight = 1f
        //Do not inflate every ship since threat botes have 1k OP.
        //AbyssFleetEquipUtils.inflate(fleet, zeroSmodWeight, oneSmodWeight, twoSmodWeight)
        var chance = WeightedRandomPicker<Int>()
        chance.add(0, zeroSmodWeight)
        chance.add(1, oneSmodWeight)
        chance.add(2, twoSmodWeight)
        for (member in fleet.fleetData.membersListCopy) {
            if (member.baseOrModSpec().hasTag("rat_abyss_threat")) {
                AbyssFleetEquipUtils.inflateShip(member, fleet.commander, chance)
            }
        }

        fleet.addEventListener(SimUnlockerListener("rat_abyssals_sim"))

        system.addEntity(fleet)
        fleet.setLocation(loc.x, loc.y)

        fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f

        system.addScript(AbyssFleetScript(fleet, source, this))
        fleet.addScript(AbyssalThreatSensorScript(fleet))

        return fleet
    }


    //Copied from Threat code, slightly modified
    fun createThreatFleet(numFabricators: Int,  minOtherCapitals: Int, maxOtherCapitals: Int,  escorts: FabricatorEscortStrength, random: Random?): CampaignFleetAPI {
        var random = random
        if (random == null) random = Misc.random

        var minSaints = 0
        var maxSaints = 0
        var minPrayers = 0
        var maxPrayers = 0
        var minHives = 0
        var maxHives = 0
        var minOverseers = 0
        var maxOverseers = 0
        var minCruisers = 0
        var maxCruisers = 0
        var minDestroyers = 0
        var maxDestroyers = 0
        var minFrigates = 0
        var maxFrigates = 0

        when (escorts) {
            FabricatorEscortStrength.NONE -> {}
            FabricatorEscortStrength.LOW -> {
                minOverseers = 1
                maxOverseers = 2
                minDestroyers = 1
                maxDestroyers = 2
                minFrigates = 2
                maxFrigates = 4
                if (numFabricators <= 0) {
                    minOverseers = 1
                }
            }

            FabricatorEscortStrength.MEDIUM -> {
                minPrayers = 1
                maxPrayers = 3
                minHives = 1
                maxHives = 1
                minOverseers = 2
                maxOverseers = 3
                minCruisers = 0
                maxCruisers = 1
                minDestroyers = 1
                maxDestroyers = 2
                minFrigates = 2
                maxFrigates = 4
            }

            FabricatorEscortStrength.HIGH -> {
                minSaints = 1
                maxSaints = 2
                minPrayers = 1
                maxPrayers = 3
                minHives = 1
                maxHives = 2
                minOverseers = 2
                maxOverseers = 3
                minCruisers = 0
                maxCruisers = 1
                minDestroyers = 2
                maxDestroyers = 4
                minFrigates = 3
                maxFrigates = 5
            }

            FabricatorEscortStrength.MAXIMUM -> {
                minHives = 3
                maxHives = 4
                minOverseers = 4
                maxOverseers = 5
                minCruisers = 4
                maxCruisers = 5
                minDestroyers = 5
                maxDestroyers = 6
                minFrigates = 5
                maxFrigates = 6
            }
        }

        val params = WastesThreatFleetCreationParams()
        params.numSaint = minSaints + random!!.nextInt(maxSaints - minSaints + 1)
        params.numPrayer = minPrayers + random!!.nextInt(maxPrayers - minPrayers + 1)
        params.numHives = minHives + random!!.nextInt(maxHives - minHives + 1)
        params.numOverseers = minOverseers + random!!.nextInt(maxOverseers - minOverseers + 1)
        params.numCapitals = minOtherCapitals + random!!.nextInt(maxOtherCapitals - minOtherCapitals + 1)
        params.numCruisers = minCruisers + random!!.nextInt(maxCruisers - minCruisers + 1)
        params.numDestroyers = minDestroyers + random!!.nextInt(maxDestroyers - minDestroyers + 1)
        params.numFrigates = minFrigates + random!!.nextInt(maxFrigates - minFrigates + 1)

        if (escorts == FabricatorEscortStrength.LOW) params.fleetType = FleetTypes.PATROL_SMALL
        if (escorts == FabricatorEscortStrength.MEDIUM) params.fleetType = FleetTypes.PATROL_MEDIUM
        if (escorts == FabricatorEscortStrength.HIGH) params.fleetType = FleetTypes.PATROL_LARGE
        if (escorts == FabricatorEscortStrength.MAXIMUM) params.fleetType = FleetTypes.PATROL_LARGE

       /* params.fleetType = FleetTypes.PATROL_SMALL
        if (numFabricators >= 3 || (numFabricators >= 2 && escorts.ordinal >= FabricatorEscortStrength.HIGH.ordinal)) {
            params.fleetType = FleetTypes.PATROL_LARGE
        } else if (numFabricators >= 2 || (numFabricators >= 1 && escorts.ordinal >= FabricatorEscortStrength.HIGH.ordinal)) {
            params.fleetType = FleetTypes.PATROL_MEDIUM
        }*/

        return createThreatFleet(params, random)
    }

    fun createThreatFleet(params: WastesThreatFleetCreationParams, random: Random?): CampaignFleetAPI {
        val f = Global.getFactory().createEmptyFleet("rat_abyssals_wastes", "Host", true)
        f.inflater = null
        f.memoryWithoutUpdate[MemFlags.MEMORY_KEY_FLEET_TYPE] = params.fleetType

        addShips(f, params.numSaint, "rat_threatSaint", random)
        addShips(f, params.numPrayer, "rat_threatPrayer", random)
        addShips(f, params.numHives, ShipRoles.THREAT_HIVE, random)
        addShips(f, params.numOverseers, ShipRoles.THREAT_OVERSEER, random)
        addShips(f, params.numCapitals, ShipRoles.COMBAT_CAPITAL, random)
        addShips(f, params.numCruisers, ShipRoles.COMBAT_LARGE, random)
        addShips(f, params.numDestroyers, ShipRoles.COMBAT_MEDIUM, random)
        addShips(f, params.numFrigates, ShipRoles.COMBAT_SMALL, random)

        f.fleetData.setSyncNeeded()
        f.fleetData.syncIfNeeded()
        f.fleetData.sort()

        for (curr in f.fleetData.membersListCopy) {
            curr.repairTracker.cr = curr.repairTracker.maxCR
        }

        val faction = Global.getSector().getFaction("rat_abyssals_wastes")
        f.name = faction.getFleetTypeName(params.fleetType)

        f.memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = ThreatFIDConfig()
        f.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
        f.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
        f.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
        f.memoryWithoutUpdate[MemFlags.MAY_GO_INTO_ABYSS] = true

        //Handled by another script.
        /*f.detectedRangeMod.modifyMult(DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID,
            DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT,
            "Low emission drives")*/


        return f
    }

    fun addShips(fleet: CampaignFleetAPI, num: Int, role: String?, random: Random?) {
        val faction = Global.getSector().getFaction("rat_abyssals_wastes")

        val p = ShipPickParams(ShipPickMode.ALL)
        p.blockFallback = true
        p.maxFP = 1000000

        for (i in 0..<num) {
            val picks = faction.pickShip(role, p, null, random)
            for (pick in picks) {
                fleet.fleetData.addFleetMember(pick.variantId)
            }
        }
    }






    class WastesThreatFleetCreationParams {
        var numSaint: Int = 0
        var numPrayer: Int = 0
        var numHives: Int = 0
        var numOverseers: Int = 0
        var numCapitals: Int = 0
        var numCruisers: Int = 0
        var numDestroyers: Int = 0
        var numFrigates: Int = 0

        var fleetType: String = FleetTypes.PATROL_SMALL
    }




    class AbyssalThreatSensorScript(var fleet: CampaignFleetAPI) : EveryFrameScript {

        //var THREAT_DETECTED_RANGE_MULT = 0.9f
        var ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT = 1.5f
        var seenByPlayerTimeout = 0f

        init {
            /*fleet.getDetectedRangeMod().modifyMult(DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID,
                THREAT_DETECTED_RANGE_MULT,
                "Low emission drives")*/
        }

        override fun isDone(): Boolean {
            return fleet.isExpired
        }


        override fun runWhilePaused(): Boolean {
            return false
        }


        override fun advance(amount: Float) {
            seenByPlayerTimeout -= amount

            val player = Global.getSector().playerFleet ?: return

            val playerHasSensorMods =
                Global.getSector().playerMemoryWithoutUpdate.getBoolean(DisposableThreatFleetManager.SENSOR_MODS_KEY)

            //playerHasSensorMods = true;
            if (playerHasSensorMods) {
                fleet.stats.dynamic.getStat(Stats.DETECTED_BY_PLAYER_RANGE_MULT)
                    .modifyMult(DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID,
                       ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT)
            } else {
                fleet.stats.dynamic.getStat(Stats.DETECTED_BY_PLAYER_RANGE_MULT)
                    .unmodifyMult(DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID)
            }

            var visibleToPlayer = fleet.isVisibleToPlayerFleet && player.isVisibleToSensorsOf(fleet)
            if (!Global.getSettings().isCampaignSensorsOn && fleet.isInCurrentLocation) {
                var dist = Misc.getDistance(fleet, player)
                dist -= fleet.radius + player.radius
                if (playerHasSensorMods) {
                    dist /= ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT
                }
                val asb = player.getAbility(Abilities.SENSOR_BURST) != null && player.getAbility(Abilities.SENSOR_BURST).isActive
                visibleToPlayer = dist < 600f || asb && dist < 900f
            }


            if (visibleToPlayer) {
                setSeenByPlayer()
            }
            if (seenByPlayerTimeout > 0f) {
                visibleToPlayer = true
            }


            //visibleToPlayer = false;
            if (!visibleToPlayer) {
                if (fleet.ai is ModularFleetAIAPI) {
                    val ai = fleet.ai as ModularFleetAIAPI
                    for (i in 0..2) {
                        //ai.navModule.avoidEntity(player, 1000f, 1500f, 0.2f)
                        ai.navModule.avoidEntity(player, 200f, 300f, 0.2f) //Make it easier to run in to their range

                    }

                    fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = false
                    fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE] = true
                }
            } else {
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE] = false
            }
        }

        fun setSeenByPlayer() {
            seenByPlayerTimeout = ThreatFleetBehaviorScript.MIN_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER + (ThreatFleetBehaviorScript.MAX_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER - ThreatFleetBehaviorScript.MIN_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER) * Math.random()
                    .toFloat()
        }

    }












    override fun spawnParticlesForCell(particleManager: BiomeParticleManager, cell: BiomeCellData) {
        //super.spawnParticlesForCell(particleManager, cell) //Replace the particle spawner

        var count = 3
        var fadeInOverwrite = false
        if (particleManager.particles.size <= 100) {
            count *= 4
            fadeInOverwrite = true
        }

        for (i in 0 until count) {
            var velocity = Vector2f(0f, 0f)
            velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(100f, 150f), MathUtils.getRandomNumberInRange(0f, 360f)))

            var color = getParticleColor()

            //var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
            var spawnLocation = cell.getWorldCenter()

            var spread = AbyssBiomeManager.cellSize * 0.75f
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 3f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 5f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 3f)

            var size = MathUtils.getRandomNumberInRange(25f, 45f)

            var alpha = MathUtils.getRandomNumberInRange(0.15f, 0.25f)

            particleManager.particles.add(BiomeParticleManager.AbyssalLightParticle(
                this,
                fadeIn,duration, fadeOut,
                color, alpha, size, spawnLocation, velocity,
                IntervalUtil(0.5f, 0.75f), MathUtils.getRandomNumberInRange(-5f, 5f), -20f, 20f))
        }
    }

}