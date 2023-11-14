package assortment_of_things.relics.industry

import assortment_of_things.campaign.industries.BaseConsumeableIndustry
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.Industry.AICoreDescriptionMode
import com.fs.starfarer.api.campaign.econ.Industry.ImprovementDescriptionMode
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*

class ExpeditionHubIndustry : BaseConsumeableIndustry(), EconomyTickListener {

    data class ExpeditionFleetResults(val planet: PlanetAPI, val duration: Float, val timestamp: Long, var hasTechmining: Boolean)

    var expeditionResults = ArrayList<ExpeditionFleetResults>()
    var deployTimestamp = Global.getSector().clock.timestamp
    var deploymentIntervalDays = 3f
    var lastingTime = 90f
    var baseRange = 15f

    var food_conditions = listOf(Conditions.FARMLAND_ADEQUATE, Conditions.FARMLAND_BOUNTIFUL, Conditions.FARMLAND_POOR, Conditions.FARMLAND_RICH)
    var ore_conditions = listOf(Conditions.ORE_ABUNDANT, Conditions.ORE_MODERATE, Conditions.ORE_RICH, Conditions.ORE_SPARSE, Conditions.ORE_ULTRARICH)
    var rare_ore_conditions = listOf(Conditions.RARE_ORE_ABUNDANT, Conditions.RARE_ORE_MODERATE, Conditions.RARE_ORE_RICH, Conditions.RARE_ORE_SPARSE, Conditions.RARE_ORE_ULTRARICH)
    var organics_conditions = listOf(Conditions.ORGANICS_ABUNDANT, Conditions.ORGANICS_COMMON, Conditions.ORGANICS_PLENTIFUL, Conditions.ORGANICS_TRACE)
    var volatiles_conditions = listOf(Conditions.VOLATILES_ABUNDANT, Conditions.VOLATILES_DIFFUSE, Conditions.VOLATILES_PLENTIFUL, Conditions.VOLATILES_TRACE)

    override fun apply() {
        updateIncomeAndUpkeep()
        Global.getSector().listenerManager.addListener(this, true);

        if (isFunctional) {
            getDemand(Commodities.SHIPS).quantity.modifyFlat("rat_expedition_hub_0", MathUtils.clamp(market.size - 2f, 1f, 10f), currentName)
            getDemand(Commodities.FUEL).quantity.modifyFlat("rat_expedition_hub_1", MathUtils.clamp(market.size - 3f, 1f, 10f), currentName)
        }
        else {
            getDemand(Commodities.SHIPS).quantity.unmodifyFlat("rat_expedition_hub_0")
            getDemand(Commodities.FUEL).quantity.unmodifyFlat("rat_expedition_hub_1")

        }

        refreshResourceState()
    }


    override fun unapply() {
        super.unapply()
        Global.getSector().listenerManager.removeListener(this);
    }


    fun getOrbitTime() : Float {
        return 10f
    }

    fun getExpeditionRange() : Float {
        var range = baseRange
        if (aiCoreId == Commodities.GAMMA_CORE || aiCoreId == Commodities.BETA_CORE || aiCoreId == Commodities.ALPHA_CORE) range += 10

        return range
    }

    fun getFleets() : ArrayList<CampaignFleetAPI> {
        var fleets = market.memoryWithoutUpdate.get("\$rat_expedition_hub_fleets") as ArrayList<CampaignFleetAPI>?
        if (fleets == null) {
            fleets = ArrayList<CampaignFleetAPI>()
            market.memoryWithoutUpdate.set("\$rat_expedition_hub_fleets", fleets)
        }
        return fleets
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (isFunctional) {

            var maxFleets = market.size - 1
            maxFleets = MathUtils.clamp(maxFleets, 3, 8)
            if (isImproved) maxFleets += 1
            var fleets = getFleets()

            for (fleet in ArrayList(fleets)) {
                if (fleet.isDespawning || !fleet.isAlive || fleet.isExpired) {
                    fleets.remove(fleet)
                    deployTimestamp = Global.getSector().clock.timestamp
                    deploymentIntervalDays = MathUtils.getRandomNumberInRange(2f, 5f)
                }
            }

            var currentFleetSize = fleets.size
            var daysSince = Global.getSector().clock.getElapsedDaysSince(deployTimestamp)
            if (daysSince > deploymentIntervalDays && currentFleetSize < maxFleets) {
                var planet = findSuitablePlanet()

                var shipDeficit = getMaxDeficit(Commodities.SHIPS).two != 0
                var fuelDeficit = getMaxDeficit(Commodities.FUEL).two != 0

                if (planet != null && !shipDeficit && !fuelDeficit) {
                    spawnFleet(planet)
                }
            }
        }
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, mode: Industry.IndustryTooltipMode?) {
        tooltip!!.addSpacer(10f)
        tooltip!!.addPara("The size of the deployed fleets depends on the planets fleet size modifier and the amount of deployed fleets scales with colony size." +
                "\n\n" +
                "Ressources unloaded from a fleet supply the colony for around ${lastingTime.toInt()} days. Fleets are send towards systems within a ${baseRange.toInt()} LY radius around the markets system.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "fleet size", "scales with colony size", "${lastingTime.toInt()}", "${baseRange.toInt()} LY")
    }



    fun spawnFleet(planet: PlanetAPI) : CampaignFleetAPI {

        var market = getMarket()
        var entity = market.primaryEntity

        var basePoints = MathUtils.getRandomNumberInRange(30f, 50f)
        var modifier = market.stats.dynamic.getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f)

        modifier =  MathUtils.clamp(modifier, 0.2f, 2f)

        var points = basePoints * modifier
        points = MathUtils.clamp(points, 30f, 110f)

        val random = Random()

        var type = FleetTypes.PATROL_SMALL
        if (points > 80) type = FleetTypes.PATROL_MEDIUM
        if (points > 140) type = FleetTypes.PATROL_LARGE

        var combatPoints = points * 0.40f
        var logisticPoints = points * 0.50f
        var tankerPoints = points * 0.10f

        val params = FleetParamsV3(market,
            entity.locationInHyperspace,
            market.factionId,
            null,
            type,
            combatPoints,  // combatPts
            logisticPoints,  // freighterPts
            tankerPoints,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )

        params.random = random
        val fleet = FleetFactoryV3.createFleet(params)
        val location = entity.containingLocation
        location.addEntity(fleet)

        //fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SCAVENGER] = true
        fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = true

        fleet.setLocation(entity.location.x, entity.location.y)
        fleet.facing = random.nextFloat() * 360f

        fleet.setFaction(market.factionId)
        fleet.name = "Expedition Fleet"

        getFleets().add(fleet)

        deployTimestamp = Global.getSector().clock.timestamp
        deploymentIntervalDays = MathUtils.getRandomNumberInRange(2f, 5f)

        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, planet, 999999f, "On an expedition towards ${planet.fullName}.") {
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, planet, getOrbitTime(), "Exploring ${planet.fullName}.") {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, market.primaryEntity, 999999f, "Returning to ${market.name}.") {

                    var techmining = false
                    if (Misc.hasRuins(planet.market)) techmining = true

                    expeditionResults.add(ExpeditionFleetResults(planet, lastingTime, Global.getSector().clock.timestamp, techmining))
                    refreshResourceState()
                }
            }
        }

        if (aiCoreId == Commodities.ALPHA_CORE || aiCoreId == Commodities.BETA_CORE) {
            fleet.stats.fleetwideMaxBurnMod.modifyFlat("rat_expedition_burn", 2f)
        }

        var timestamp = Global.getSector().clock.timestamp
        fleet.addScript( object: EveryFrameScript {

            override fun isDone(): Boolean {
                return fleet.isDespawning
            }


            override fun runWhilePaused(): Boolean {
                return false
            }


            override fun advance(amount: Float) {
                if (Global.getSector().clock.getElapsedDaysSince(timestamp) > 150 && Global.getSector().playerFleet.containingLocation != fleet.containingLocation) {
                    fleet.despawn()
                }
            }

        })

        return fleet
    }


    fun findSuitablePlanet() : PlanetAPI? {

        var range = getExpeditionRange()
        var location = market.locationInHyperspace

        var starsystems = Global.getSector().starSystems.filter { Misc.getDistanceLY(location, it.location) < range && !it.hasTag(Tags.THEME_HIDDEN) && !it.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) }

        var filteredSystems = ArrayList<StarSystemAPI>()



        for (system in starsystems) {

            if (!system.hasBlackHole() && !system.hasPulsar() && !system.hasTag(Tags.THEME_REMNANT_MAIN)) {
                filteredSystems.add(system)
            }
        }

        if (filteredSystems.isEmpty()) {
            for (system in starsystems) {
                if (!system.hasBlackHole() && !system.hasPulsar()) {
                    filteredSystems.add(system)
                }
            }
        }

        if (filteredSystems.isEmpty()) {
            filteredSystems.addAll(starsystems)
        }

        var planets = filteredSystems.flatMap { it.planets }.filter { !it.isStar && it.faction.id == Factions.NEUTRAL && it.market != null && it.market.conditions != null && !it.isGasGiant}
        if (planets.isEmpty()) return null

        var planetPicker = WeightedRandomPicker<PlanetAPI>()
        for (planet in planets) {
            var weight = 0.5f

            var count = 0
            for (condition in planet.market.conditions) {
                if (food_conditions.contains(condition.id) || ore_conditions.contains(condition.id) || rare_ore_conditions.contains(condition.id)
                    || organics_conditions.contains(condition.id) || volatiles_conditions.contains(id)) {
                    count += 1
                }
            }

            if (count == 2) weight += 3f
            if (count >= 3) weight += 5f

            if (Misc.hasFarmland(planet.market)) {
                weight += 5
            }
            if (Misc.hasRuins(planet.market)) {
                weight += 7
            }

            planetPicker.add(planet, weight)
        }

        var planet = planetPicker.pick()
        return planet
    }

    fun refreshResourceState() {
        var maxSupply = 2 + market.size
        maxSupply = MathUtils.clamp(maxSupply, 0, 10)

        for (result in ArrayList(expeditionResults)) {
            if (Global.getSector().clock.getElapsedDaysSince(result.timestamp) > result.duration && !result.hasTechmining) {
                expeditionResults.remove(result)
            }
        }

        var food = 0

        var ore = 0
        var rare_ore = 0
        var organics = 0
        var volatiles = 0

        var metals = 0
        var ships = 0
        var heavy_machinery = 0

        for (result in expeditionResults) {
            var planet = result.planet
            var market = planet.market
            var conditions = market.conditions

            for (condition in conditions) {
                when (condition.spec.id) {
                    Conditions.FARMLAND_POOR -> food += 2
                    Conditions.FARMLAND_ADEQUATE -> food += 3
                    Conditions.FARMLAND_RICH -> food += 4
                    Conditions.FARMLAND_BOUNTIFUL -> food += 5

                    Conditions.ORE_SPARSE -> ore += 1
                    Conditions.ORE_MODERATE -> ore += 1
                    Conditions.ORE_ABUNDANT -> ore += 2
                    Conditions.ORE_RICH -> ore += 2
                    Conditions.ORE_ULTRARICH -> ore += 3

                    Conditions.RARE_ORE_SPARSE -> rare_ore += 1
                    Conditions.RARE_ORE_MODERATE -> rare_ore += 1
                    Conditions.RARE_ORE_ABUNDANT -> rare_ore += 2
                    Conditions.RARE_ORE_RICH -> rare_ore += 2
                    Conditions.RARE_ORE_ULTRARICH -> rare_ore += 3

                    Conditions.ORGANICS_TRACE -> organics += 1
                    Conditions.ORGANICS_COMMON -> organics += 1
                    Conditions.ORGANICS_ABUNDANT -> organics += 2
                    Conditions.ORGANICS_PLENTIFUL -> organics += 3

                    Conditions.VOLATILES_TRACE -> volatiles += 1
                    Conditions.VOLATILES_DIFFUSE -> volatiles += 1
                    Conditions.VOLATILES_ABUNDANT -> volatiles += 2
                    Conditions.VOLATILES_PLENTIFUL -> volatiles += 3

                    Conditions.RUINS_SCATTERED -> { metals += 1; ships += 1; heavy_machinery += 0 }
                    Conditions.RUINS_WIDESPREAD -> { metals += 1; ships += 2; heavy_machinery += 1 }
                    Conditions.RUINS_EXTENSIVE -> { metals += 2; ships += 3; heavy_machinery += 1}
                    Conditions.RUINS_VAST -> { metals += 3; ships += 4; heavy_machinery += 2}
                }
            }

        }

        supply("rat_expedition_hub_0", Commodities.FOOD, MathUtils.clamp(food, 0, maxSupply), currentName)

        supply("rat_expedition_hub_1", Commodities.ORE, MathUtils.clamp(ore, 0, maxSupply), currentName)
        supply("rat_expedition_hub_2", Commodities.RARE_ORE, MathUtils.clamp(rare_ore, 0, maxSupply), currentName)
        supply("rat_expedition_hub_3", Commodities.ORGANICS, MathUtils.clamp(organics, 0, maxSupply), currentName)
        supply("rat_expedition_hub_4", Commodities.VOLATILES, MathUtils.clamp(volatiles, 0, maxSupply), currentName)

        supply("rat_expedition_hub_5", Commodities.METALS, MathUtils.clamp(metals, 0, maxSupply), currentName)
        supply("rat_expedition_hub_7", Commodities.SHIPS, MathUtils.clamp(ships, 0, maxSupply), currentName)
        supply("rat_expedition_hub_7", Commodities.HEAVY_MACHINERY, MathUtils.clamp(heavy_machinery, 0, maxSupply), currentName)
    }

    override fun reportEconomyTick(iterIndex: Int) {
        refreshResourceState()
    }

    override fun reportEconomyMonthEnd() {

    }

    override fun canInstallAICores(): Boolean {
        return true
    }

    override fun generateCargoForGatheringPoint(random: Random?): CargoAPI? {

        val dropRandom: MutableList<DropData> = ArrayList()
        val dropValue: MutableList<DropData> = ArrayList()


        var totalCargo = Global.getFactory().createCargo(true)

        for (result in expeditionResults) {
            if (result.hasTechmining) {

                var mult = 0f

                for (condition in result.planet.market.conditions) {
                    when (condition.id) {
                        Conditions.RUINS_SCATTERED -> mult += 0.5f
                        Conditions.RUINS_WIDESPREAD -> mult += 0.75f
                        Conditions.RUINS_EXTENSIVE -> mult += 0.8f
                        Conditions.RUINS_VAST -> mult += 1f
                    }
                }

                result.hasTechmining = false

                if (mult == 0f) continue

                mult += 0.2f

                var d = DropData()
                d.chances = 1
                d.group = "blueprints_low"
                dropRandom.add(d)

                d = DropData()
                d.chances = 1
                d.group = "rare_tech_low"
                d.valueMult = 0.2f
                dropRandom.add(d)

                d = DropData()
                d.chances = 1
                d.group = "ai_cores3"
                dropRandom.add(d)

                d = DropData()
                d.chances = 1
                d.group = "any_hullmod_low"
                dropRandom.add(d)

                d = DropData()
                d.chances = 5
                d.group = "weapons2"
                dropRandom.add(d)

                d = DropData()
                d.group = "basic"
                d.value = 10000
                dropValue.add(d)

                val result = SalvageEntity.generateSalvage(random, 1f, 1f, mult, 1f, dropValue, dropRandom)
                totalCargo.addAll(result)
            }
        }

        if (totalCargo.isEmpty) return null

        return totalCargo
    }

    override fun addAlphaCoreDescription(tooltip: TooltipMakerAPI?, mode: Industry.AICoreDescriptionMode?) {
        var tip = tooltip
        tooltip!!.addSpacer(10f)

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            val coreSpec = Global.getSettings().getCommoditySpec(aiCoreId)
            tip = tooltip.beginImageWithText(coreSpec.iconName, 48f)
        }

        tip!!.addPara("Increases the range by 10 LY and fleets have +2 burn speed. Performs limited amounts of tech-mining on planets with ruins.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "10 LY", "+2", "tech-mining")

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            tooltip.addImageWithText(0f)
        }
    }

    override fun addBetaCoreDescription(tooltip: TooltipMakerAPI?, mode: Industry.AICoreDescriptionMode?) {
        var tip = tooltip
        tooltip!!.addSpacer(10f)

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            val coreSpec = Global.getSettings().getCommoditySpec(aiCoreId)
            tip = tooltip.beginImageWithText(coreSpec.iconName, 48f)
        }

        tip!!.addPara("Increases the range by 10 LY and fleets have +2 burn speed.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "10 LY", "+2")

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            tooltip.addImageWithText(0f)
        }
    }

    override fun addGammaCoreDescription(tooltip: TooltipMakerAPI?, mode: Industry.AICoreDescriptionMode?) {
        var tip = tooltip
        tooltip!!.addSpacer(10f)

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            val coreSpec = Global.getSettings().getCommoditySpec(aiCoreId)
            tip = tooltip.beginImageWithText(coreSpec.iconName, 48f)
        }

        tip!!.addPara("Increases the range by 10 LY.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "10 LY")

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            tooltip.addImageWithText(0f)
        }
    }

    override fun canImprove(): Boolean {
        return true
    }

    override fun addImproveDesc(info: TooltipMakerAPI?, mode: Industry.ImprovementDescriptionMode?) {
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info!!.addPara("Increased the amount of fleets by 1", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1")
        } else {
            info!!.addPara("Increase the amount of fleets by 1", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1")
        }
        info!!.addSpacer(10f)
        super.addImproveDesc(info, mode)
    }

    override fun applyAlphaCoreSupplyAndDemandModifiers() {

    }

    override fun applyBetaCoreSupplyAndDemandModifiers() {

    }

    override fun applyGammaCoreModifiers() {

    }

}