package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.procgen.AbyssGenerator
import assortment_of_things.abyss.procgen.AbyssSystemData
import assortment_of_things.abyss.procgen.AbyssalSeraphSpawner
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*

//Spawns a single defensive fleet that attempts to orbit around a single location.

//If the player isnt around the fleet will despawn, and if the fleet is ever destroyed, the script is removed to prevent respawns.
//Will despawn if the player isnt in the abyss, or if the player is in the abyss but 2 connections away.
//Will always use the same seed for respawn.

//Its set to not to long pursuits, so once it looses the player it should go back to its spawn relatively soon.

class AbyssalDefendingFleetManager(source: SectorEntityToken, var depth: AbyssDepth) : SourceBasedFleetManager(source, 1f, 0, 1, 999f) {

    var starsystem = source.starSystem

    //Generates a single seed so that when it reconstructs the fleet, its basicly the same one.
    var randomSeed = Random().nextLong()

    enum class AbyssFleetType(var weight: Float, var deep: Boolean) {
        Normal(1.5f, false),
        Wolfpack(0.20f, false),
        MonoChronos(0.10f, false),
        MonoCosmos(0.10f, false),
        MonoSeraphShips(0.1f, true),
        MonoSeraphOnNormal(0.1f, true),
        Supersized(0.1f, false),
    }

    var type: AbyssFleetType? = null


    override fun advance(amount: Float) {
        super.advance(amount)

        if (!AbyssUtils.playerInNeighbourOrSystem(starsystem))
        {
            for (fleet in ArrayList(fleets))
            {
                fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null)
            }
        }
    }


    /*Remnant Fleet Points
    *
    * Remnant Medium Seeded Points (Randomly spawn in to the system)
    * Min: 32
    * Max: 96
    *
    * Remnant Medium Station Points (Spawned From Station)
    * Min: 48
    * Max: 96
    *
    * Remnant High Station Points (Spawned From Station)
    * Min: 64
    * Max: 192
    *
    * */

    override fun spawnFleet(): CampaignFleetAPI? {

        if (type == null) {
            var random = Random(randomSeed)
            var picker = WeightedRandomPicker<AbyssFleetType>()
            picker.random = random

            for (type in AbyssFleetType.values()) {
                if (depth == AbyssDepth.Shallow && type.deep) continue
                if (depth == AbyssDepth.Deep && type == AbyssFleetType.Wolfpack) continue //Seraph mono is kinda advanced wolfpack

                picker.add(type, type.weight)
            }
            type = picker.pick()
        }

        if (source.isExpired) return null

        //Prevents the game spawning fleets when the player isnt in the system or a neighbour
        if (!AbyssUtils.playerInNeighbourOrSystem(starsystem)) return null

        var data = AbyssUtils.getSystemData(starsystem)
        var difficulty = AbyssUtils.getDifficulty()
        val random = Random(randomSeed)

        var factionID = "rat_abyssals"
        if (depth == AbyssDepth.Deep) factionID = "rat_abyssals_deep"
        if (type == AbyssFleetType.MonoSeraphShips) factionID = "rat_abyssals_deep_seraph"

        /*var gen = StandardAbyssFleetGen(data)
        var fleet = gen.spawnFleet(source, factionID, random)*/

        var fleetType = FleetTypes.PATROL_MEDIUM
        var points = generateFleetPoints(data)
        if (type == AbyssFleetType.Supersized) {
            points *= 2f
            fleetType = FleetTypes.PATROL_LARGE
        }
        else if (type == AbyssFleetType.Wolfpack) {
            points *= 0.9f
            fleetType = FleetTypes.PATROL_SMALL
        }
        else if (type == AbyssFleetType.MonoSeraphShips) {
            points *= 0.9f
        }

        var factionAPI = Global.getSector().getFaction(factionID)

        /*factionAPI.knownShips.clear()
        factionAPI.knownShips.addAll(getAvailableShips(data))*/

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

        if (type == AbyssFleetType.Wolfpack) {
            params.maxShipSize = 2
        }

        val fleet = FleetFactoryV3.createFleet(params)
        fleet.inflateIfNeeded()

        if (factionID != "rat_abyssals_deep_seraph") {
            var minSeraphs = 0
            var maxSeraphs = 0
            if (depth == AbyssDepth.Deep) minSeraphs += 2
            if (depth == AbyssDepth.Deep) maxSeraphs += 3
            if (difficulty == AbyssDifficulty.Hard) minSeraphs += 1
            if (difficulty == AbyssDifficulty.Hard) maxSeraphs += 2

            AbyssalSeraphSpawner.addSeraphsToFleet(fleet, random, minSeraphs, maxSeraphs)
            AbyssalSeraphSpawner.sortWithSeraphs(fleet)

            for (member in fleet.fleetData.membersListCopy) {
                member.variant.addTag(Tags.TAG_NO_AUTOFIT)
            }
        }




        fleet.addTag("rat_fleet_type_${type!!.name}")

        if (type == AbyssFleetType.Supersized) {
            fleet.stats.fleetwideMaxBurnMod.modifyFlat("rat_supersize_debuff", -2f)
        }
        if (type == AbyssFleetType.MonoSeraphShips) {
            fleet.stats.fleetwideMaxBurnMod.modifyFlat("rat_supersize_debuff", -1f)
        }

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }

        addAICores(fleet, data, difficulty, random)

        val location = starsystem
        location.addEntity(fleet)

       // RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false)
        initAbyssalBehaviour(fleet, random)


        inflate(data, fleet)

        //fleet.addScript(RemnantAssignmentAI(fleet, source.containingLocation as StarSystemAPI, source))
        fleet.memoryWithoutUpdate["\$sourceId"] = source.id

        fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f

        addFollowThroughFractureScript(fleet)
        addMemKeyApplierScript(fleet)

        var alterationChancePerShip = 0.0f
        if (difficulty == AbyssDifficulty.Hard) alterationChancePerShip += 0.1f
        if (depth == AbyssDepth.Shallow) alterationChancePerShip += 0.1f
        if (depth == AbyssDepth.Deep) alterationChancePerShip += 0.2f

        AbyssUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        return fleet
    }

   /* fun getAvailableShips(data: AbyssSystemData) : MutableList<String> {
        var list = mutableListOf<String>()

        var base = mutableListOf("rat_merrow", "rat_makara", "rat_chuul", "rat_aboleth", "rat_aboleth_m", "rat_morkoth")
        var seraphs = mutableListOf("rat_raguel", "rat_sariel")

        if (type == AbyssFleetType.MonoSeraphShips) {
            return mutableListOf("rat_raguel", "rat_sariel")
        }

        list.addAll(mutableListOf("rat_merrow", "rat_makara", "rat_chuul", "rat_aboleth", "rat_aboleth_m", "rat_morkoth"))

        if (data.depth == AbyssDepth.Deep) {
            list.addAll(mutableListOf("rat_raguel", "rat_sariel"))
        }

        return list
    }*/

    fun addAICores(fleet: CampaignFleetAPI, data: AbyssSystemData, difficulty: AbyssDifficulty, random: Random)
    {

        var level = getStrengthLevelForStep(data)
        var corePercentage = 0f

        if (depth == AbyssDepth.Shallow) corePercentage = 0.4f + (0.1f * level)
        if (depth == AbyssDepth.Deep) corePercentage = 0.5f + (0.2f * level)

        if (type == AbyssFleetType.MonoSeraphOnNormal || type == AbyssFleetType.MonoCosmos || type == AbyssFleetType.MonoChronos) {
            corePercentage += 0.15f
        }

        if (difficulty == AbyssDifficulty.Hard) corePercentage += 0.3f

        var members = fleet.fleetData.membersListCopy.filter { it.captain == null || !it.captain.isAICore }

        corePercentage = MathUtils.clamp(corePercentage, 0f, 1f)

        var picker = WeightedRandomPicker<FleetMemberAPI>()
        picker.random = random
        for (member in members)
        {

            //Always give seraph ships seraph cores
            if (member.variant.baseOrModSpec().hasTag("rat_seraph")) {
                var core =  SeraphCore().createPerson(RATItems.SERAPH_CORE, fleet.faction.id, random)
                member.captain = core
                continue
            }


            var weight = when(member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 1f
                ShipAPI.HullSize.DESTROYER -> 3f
                ShipAPI.HullSize.CRUISER -> 6f
                ShipAPI.HullSize.CAPITAL_SHIP -> 15f
                else -> 0f
            }


            var variantID = member.variant.hullVariantId.lowercase()
            if (member.variant.hasHullMod(HullMods.SAFETYOVERRIDES)) weight *= 4f
            if (variantID.contains("chronos") || variantID.contains("cosmos")) weight *= 1.5f

            picker.add(member, weight)
        }

        var count = (members.size * corePercentage).toInt()
        for (i in 0 until count)
        {
            var pick = picker.pickAndRemove() ?: continue

            var chronos = ChronosCore()
            var cosmos = CosmosCore()
            var seraph = SeraphCore()

            var core: PersonAPI? = null

            var variantID = pick.variant.hullVariantId.lowercase()

            if (type == AbyssFleetType.MonoChronos) core = chronos.createPerson(RATItems.CHRONOS_CORE, fleet.faction.id, random)
            else if (type == AbyssFleetType.MonoCosmos) core = cosmos.createPerson(RATItems.COSMOS_CORE, fleet.faction.id, random)
            else if (type == AbyssFleetType.MonoSeraphOnNormal) core = seraph.createPerson(RATItems.SERAPH_CORE, fleet.faction.id, random)

            else if (variantID.contains("temporal") || variantID.contains("chronos"))
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, fleet.faction.id, random)
            }
            else if (variantID.contains("cosmal") || variantID.contains("cosmos"))
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, fleet.faction.id, random)
            }
            else if (random.nextFloat() > 0.5f)
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, fleet.faction.id, random)
            }
            else
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, fleet.faction.id, random)
            }

            pick.captain = core
        }

    }


    fun inflate(data: AbyssSystemData, fleet: CampaignFleetAPI) {
        if (depth == AbyssDepth.Shallow) {
            fleet.stats.sensorRangeMod.modifyMult("rat_abyssals_passive_detect_reduction", 0.90f)
        }
        else {
            // fleet.stats.sensorRangeMod.modifyMult("rat_abyssals_passive_detect_reduction", 1.10f)
            fleet.stats.detectedRangeMod.modifyMult("rat_abyssals_passive_detect_reduction", 0.90f)
        }

        var abyssData = AbyssUtils.getAbyssData()
        if (!abyssData.hasAbyssalDoctrine) {
            for (member in fleet.fleetData.membersListWithFightersCopy) {
                if (member.baseOrModSpec().hasTag("rat_abyssals") && !member.baseOrModSpec().hasTag("rat_seraph")) {
                    member.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
                }
            }
            abyssData.doctrineLearnedListeners.add(AbyssDoctrineLearnedListener(fleet))
        }

        if (!abyssData.hasSeraphDoctrine) {
            for (member in fleet.fleetData.membersListWithFightersCopy) {
                if (member.baseOrModSpec().hasTag("rat_seraph")) {
                    member.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
                }
            }
            abyssData.doctrineLearnedListeners.add(AbyssDoctrineLearnedListener(fleet))
        }

        var difficulty = AbyssUtils.getDifficulty()
        var chance = WeightedRandomPicker<Int>()


        if (difficulty == AbyssDifficulty.Hard) {
            if (depth == AbyssDepth.Shallow) {
                chance.add(0, 0.2f)
                chance.add(1, 0.4f)
                chance.add(2, 0.4f)

            }
            else if (depth == AbyssDepth.Deep) {
                chance.add(0, 0.2f)
                chance.add(1, 0.5f)
                chance.add(2, 0.5f)

            }
        }
        else {

            if (depth == AbyssDepth.Shallow) {
                chance.add(0, 0.7f)
                chance.add(1, 0.2f)
            }
            else if (depth == AbyssDepth.Deep) {
                chance.add(0, 0.5f)
                chance.add(1, 0.2f)
            }
        }

        for (member in fleet.fleetData.membersListCopy)
        {
            inflateShip(member, fleet.commander, chance)
        }
    }


    fun initAbyssalBehaviour(fleet: CampaignFleetAPI, random: Random)
    {
        fleet.removeAbility(Abilities.EMERGENCY_BURN)
        fleet.removeAbility(Abilities.SENSOR_BURST)
        fleet.removeAbility(Abilities.GO_DARK)

        // to make sure they attack the player on sight when player's transponder is off

        // to make sure they attack the player on sight when player's transponder is off
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
        //fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = true
       // fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
        fleet.memoryWithoutUpdate[MemFlags.DO_NOT_TRY_TO_AVOID_NEARBY_FLEETS] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
        fleet.memoryWithoutUpdate[MemFlags.FLEET_DO_NOT_IGNORE_PLAYER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true

        RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)

        val salvageSeed: Long = random.nextLong()
        fleet.memoryWithoutUpdate[MemFlags.SALVAGE_SEED] = salvageSeed
    }

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?,  param: Any?) {
        super.reportFleetDespawnedToListener(fleet, reason, param)
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            val sid = fleet!!.memoryWithoutUpdate.getString("\$sourceId")

            if (sid != null)
            {
                if (source != null)
                {
                    if (sid == source.id)
                    {
                        if (param is BattleAPI)
                        {
                            if (param.isPlayerInvolved)
                            {
                                AbyssalDepthsEventIntel.addFactorCreateIfNecessary(object: BaseOneTimeFactor(5) {
                                    override fun getDesc(intel: BaseEventIntel?): String {
                                        return "Defeated a defending fleet"
                                    }

                                    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
                                        return object : BaseFactorTooltip() {
                                            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                                                tooltip.addPara("The fleet defeated a fleet that was defending a location within the abyss.",
                                                    0f,
                                                    Misc.getHighlightColor(),
                                                    "")
                                            }
                                        }
                                    }
                                }, null)
                            }
                        }
                        starsystem.removeScript(this)
                    }
                }
            }
        }
    }


    //Allows the fleet to go through fractures
    //Only works one step away from its source
    fun addFollowThroughFractureScript(fleet: CampaignFleetAPI)
    {
        var script = object : EveryFrameScript {
            override fun isDone(): Boolean {
                return fleet.isDespawning
            }

            override fun runWhilePaused(): Boolean {
                return false
            }

            override fun advance(amount: Float) {
                if (source == null) return

                var player = Global.getSector().playerFleet

                var tactical = (fleet.ai as ModularFleetAIAPI).tacticalModule
                var target = tactical.target

                //If the fleet was targeting the player and they dissapeared in to another system, try finding a connecting fracture and move towards it, then jump
                if (target != null && fleet.containingLocation != target.containingLocation)
                {
                    var fractures = fleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalFracture }
                    var fracture: CustomCampaignEntityAPI? = null
                    for (frac in fractures)
                    {
                        var plug = frac.customPlugin as AbyssalFracture
                        if (plug.connectedEntity!!.containingLocation == target.containingLocation)
                        {
                            fracture = frac
                        }
                    }

                    if (fracture != null)
                    {
                        tactical.forceTargetReEval()
                        fleet.clearAssignments()
                        tactical.setPriorityTarget(fracture, 1f, true)
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, fracture, 2f) {
                            Global.getSector().doHyperspaceTransition(fleet, fracture,
                                JumpPointAPI.JumpDestination(((fracture!!.customPlugin as AbyssalFracture).connectedEntity), ""))
                        }
                        fleet.addAssignment(FleetAssignment.INTERCEPT, target, 2f)
                    }

                }
                //If the fleet is in another system than its source, isnt targeting anything and has no assignments, move back towards its source location.
                else if (target == null && fleet.containingLocation != starsystem && !fleet.isCurrentAssignment(FleetAssignment.GO_TO_LOCATION) && !fleet.isCurrentAssignment(FleetAssignment.INTERCEPT))
                {
                    var fractures = fleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalFracture }
                    var fracture: CustomCampaignEntityAPI? = null
                    for (frac in fractures)
                    {
                        var plug = frac.customPlugin as AbyssalFracture
                        if (plug.connectedEntity!!.containingLocation == starsystem)
                        {
                            fracture = frac
                        }
                    }

                    if (fracture != null)
                    {
                        tactical.forceTargetReEval()
                        fleet.clearAssignments()
                        tactical.setPriorityTarget(fracture, 0.25f, true)
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, fracture, 0.25f) {
                            Global.getSector().doHyperspaceTransition(fleet, fracture,
                                JumpPointAPI.JumpDestination(((fracture.customPlugin as AbyssalFracture).connectedEntity), ""))
                        }
                        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
                    }
                }
                else if (source.isExpired && fleet.starSystem == starsystem) {
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, source, 9999999f)
                }
            }

        }
        fleet.addScript(script)
    }

    fun addMemKeyApplierScript(fleet: CampaignFleetAPI) {
        var script = object : EveryFrameScript {


            override fun isDone(): Boolean {
                return fleet.isDespawning
            }

            override fun runWhilePaused(): Boolean {
                return false
            }

            override fun advance(amount: Float) {
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
            }
        }
        fleet.addScript(script)
    }


    fun getStrengthLevelForStep(data: AbyssSystemData) : Float {
        var level = data.step.toFloat() / (AbyssGenerator.systemsOnMainBranch - 1f)
        return level
    }

    fun getMinPointsForDepth(data: AbyssSystemData) : Float {
        var minPoints = 0f
        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep(data)

        minPoints = 84f
        if (difficulty == AbyssDifficulty.Hard) minPoints += 50f
        minPoints += 84 * level

        return minPoints
    }

    fun getMaxPointsForDepth(data: AbyssSystemData) : Float {
        var maxPoints = 0f
        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep(data)

        maxPoints = 148f
        if (difficulty == AbyssDifficulty.Hard) maxPoints += 70f
        maxPoints += 84 * level

        return maxPoints
    }

    fun getFleetScalingPoints(data: AbyssSystemData, points: Float) : Float {

        var difficulty = AbyssUtils.getDifficulty()
        var level = getStrengthLevelForStep(data)

        if (difficulty == AbyssDifficulty.Hard)
        {
            var playerFleet = Global.getSector().playerFleet
            var playerFP = playerFleet.fleetPoints.toFloat()
            var scalingPointsCap = 250f

            if (points < playerFP) {

                var difference = playerFP - points

                var scalingMult = when(depth) {
                    AbyssDepth.Shallow -> 0.45f
                    AbyssDepth.Deep -> 0.5f
                }

                var pointsForScaling = difference * scalingMult
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, playerFP)
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, scalingPointsCap)

                var adjusted = (pointsForScaling * 0.5f) + (pointsForScaling * 0.5f * level)

                return pointsForScaling
            }
        }

        return 0f
    }

    fun generateFleetPoints(data: AbyssSystemData) : Float {
        var min = getMinPointsForDepth(data)
        var max = getMaxPointsForDepth(data)
        var result = MathUtils.getRandomNumberInRange(min, max)

        result += getFleetScalingPoints(data, result)
        return result
    }


    private val availableMods = listOf(
        HullMods.HEAVYARMOR,
        HullMods.HARDENED_SHIELDS,
        HullMods.UNSTABLE_INJECTOR,
        HullMods.ACCELERATED_SHIELDS,
        HullMods.ARMOREDWEAPONS,
        HullMods.HARDENED_SUBSYSTEMS,
        HullMods.FLUXBREAKERS,
        HullMods.STABILIZEDSHIELDEMITTER,
        HullMods.AUTOREPAIR,
        HullMods.FLUX_COIL,
        HullMods.FLUX_DISTRIBUTOR,
        HullMods.AUXILIARY_THRUSTERS,
        HullMods.TURRETGYROS
    )


    private fun inflateShip(member: FleetMemberAPI, commander: PersonAPI, chance: WeightedRandomPicker<Int>)
    {
        var smods = chance.pick()
        member.fixVariant()

        var stats = commander.stats
        var variant = member.variant

        var modsOnShip = variant.nonBuiltInHullmods.map { Global.getSettings().getHullModSpec(it) }.filter { it.id !=  HullMods.SAFETYOVERRIDES }
        var potentialSmods = WeightedRandomPicker<HullModSpecAPI>()
        for (mod in modsOnShip) {
            potentialSmods.add(mod, mod.getCostFor(variant.hullSize).toFloat())
        }

        for (i in 0 until smods) {
            if (potentialSmods.isEmpty) break


            variant.addPermaMod(potentialSmods.pickAndRemove().id, true)

        }

        addHullmods(variant, stats)

        addVents(variant, stats)
        addCaps(variant, stats)

    }


    private fun getRemainingOP(variant: ShipVariantAPI, stats: MutableCharacterStatsAPI) : Int {
        var opCost = variant.computeOPCost(stats)
        var opMax = variant.hullSpec.getOrdnancePoints(stats)
        var opLeft = opMax - opCost
        return opLeft
    }

    private fun addHullmods(variant: ShipVariantAPI, stats: MutableCharacterStatsAPI) {
        var modSpecs = availableMods.map { Global.getSettings().getHullModSpec(it) }

        modSpecs = modSpecs.filter { !variant.hasHullMod(it.id) }
        if (modSpecs.isEmpty()) return

        var opLeft = getRemainingOP(variant, stats)

        modSpecs = modSpecs.filter { it.getCostFor(variant.hullSize) < opLeft }
        if (modSpecs.isEmpty()) return

        var pick = modSpecs.random()
        variant.addMod(pick.id)

        var opLeftAfterAddition = getRemainingOP(variant, stats)
        modSpecs = modSpecs.filter { it.getCostFor(variant.hullSize) < opLeft }

        if (modSpecs.isNotEmpty()) {
            addHullmods(variant, stats)
        }
    }

    private fun addVents(variant: ShipVariantAPI, stats: MutableCharacterStatsAPI) {

        var ventsToAdd = 1000

        var opLeft = getRemainingOP(variant, stats)

        var availableVents = getMaxVents(variant.hullSize, stats) - variant.numFluxVents

        ventsToAdd = MathUtils.clamp(ventsToAdd, 0, availableVents)
        ventsToAdd = MathUtils.clamp(ventsToAdd, 0, opLeft)

        variant.numFluxVents += ventsToAdd
    }

    private fun addCaps(variant: ShipVariantAPI, stats: MutableCharacterStatsAPI) {

        var capsToAdd = 1000

        var opLeft = getRemainingOP(variant, stats)

        var availableVents = getMaxCaps(variant.hullSize, stats) - variant.numFluxCapacitors

        capsToAdd = MathUtils.clamp(capsToAdd, 0, availableVents)
        capsToAdd = MathUtils.clamp(capsToAdd, 0, opLeft)

        variant.numFluxCapacitors += capsToAdd
    }

    private fun getMaxVents(size: ShipAPI.HullSize?, stats: MutableCharacterStatsAPI): Int {
        var maxVents = CoreAutofitPlugin.getBaseMax(size)
        if (stats != null) {
            maxVents = stats.getMaxVentsBonus().computeEffective(maxVents.toFloat()).toInt()
        }
        return maxVents
    }

    private fun getMaxCaps(size: ShipAPI.HullSize?, stats: MutableCharacterStatsAPI): Int {
        var maxCapacitors = CoreAutofitPlugin.getBaseMax(size)
        if (stats != null) {
            maxCapacitors = stats.getMaxCapacitorsBonus().computeEffective(maxCapacitors.toFloat()).toInt()
        }
        return maxCapacitors
    }
}