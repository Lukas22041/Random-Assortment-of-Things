package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetInflater
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*

class AbyssalFleetInflationListener : FleetInflationListener {

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


    override fun reportFleetInflated(fleet: CampaignFleetAPI?, inflater: FleetInflater?) {
        if (fleet == null) return
        if (fleet.faction.id == "rat_abyssals")
        {

            fleet.stats.sensorRangeMod.modifyMult("rat_abyssals_passive_detect_reduction", 0.90f)

            var tier = AbyssProcgen.Tier.High
            if (fleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))    {
                tier = AbyssUtils.getTier(fleet.containingLocation)
            }

            var difficulty = AbyssUtils.getDifficulty()
            var chance = WeightedRandomPicker<Int>()


            if (tier == AbyssProcgen.Tier.Low) {
                chance.add(0, 100f)
            }

            if (difficulty == AbyssDifficulty.Hard) {
                if (tier == AbyssProcgen.Tier.Mid) {
                    chance.add(0, 0.2f)
                    chance.add(1, 0.4f)
                    chance.add(2, 0.4f)

                }
                else if (tier == AbyssProcgen.Tier.High) {
                    chance.add(0, 0.2f)
                    chance.add(1, 0.5f)
                    chance.add(2, 0.5f)

                }
            }
            else {

                if (tier == AbyssProcgen.Tier.Mid) {
                    chance.add(0, 0.7f)
                    chance.add(1, 0.2f)
                }
                else if (tier == AbyssProcgen.Tier.High) {
                    chance.add(0, 0.5f)
                    chance.add(1, 0.2f)
                }
            }

            addAICores(fleet, tier, AbyssUtils.getDifficulty(), Random())

            for (member in fleet.fleetData.membersListCopy)
            {
                inflate(member, fleet.commander, chance)
            }
        }
    }

    private fun inflate(member: FleetMemberAPI, commander: PersonAPI, chance: WeightedRandomPicker<Int>)
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

    fun addAICores(fleet: CampaignFleetAPI, tier: AbyssProcgen.Tier, difficulty: AbyssDifficulty, random: Random)
    {

        var corePercentage = 0f

        if (tier == AbyssProcgen.Tier.Low) corePercentage = 0.4f
        if (tier == AbyssProcgen.Tier.Mid) corePercentage = 0.5f
        if (tier == AbyssProcgen.Tier.High) corePercentage = 0.7f

        if (difficulty == AbyssDifficulty.Hard) corePercentage += 0.3f

        var members = fleet.fleetData.membersListCopy

        corePercentage = MathUtils.clamp(corePercentage, 0f, 1f)

        var picker = WeightedRandomPicker<FleetMemberAPI>()
        picker.random = random
        for (member in members)
        {
            var weight = when(member.hullSpec.hullSize) {
                HullSize.FRIGATE -> 1f
                HullSize.DESTROYER -> 3f
                HullSize.CRUISER -> 6f
                HullSize.CAPITAL_SHIP -> 15f
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

            var core: PersonAPI? = null

            var variantID = pick.variant.hullVariantId.lowercase()

            if (variantID.contains("temporal") || variantID.contains("chronos"))
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else if (variantID.contains("cosmal") || variantID.contains("cosmos"))
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else if (random.nextFloat() > 0.5f)
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, AbyssUtils.FACTION_ID, random)
            }

            if (core != null)
            {
                pick.captain = core
            }
        }

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

    private fun getMaxVents(size: HullSize?, stats: MutableCharacterStatsAPI): Int {
        var maxVents = CoreAutofitPlugin.getBaseMax(size)
        if (stats != null) {
            maxVents = stats.getMaxVentsBonus().computeEffective(maxVents.toFloat()).toInt()
        }
        return maxVents
    }

    private fun getMaxCaps(size: HullSize?, stats: MutableCharacterStatsAPI): Int {
        var maxCapacitors = CoreAutofitPlugin.getBaseMax(size)
        if (stats != null) {
            maxCapacitors = stats.getMaxCapacitorsBonus().computeEffective(maxCapacitors.toFloat()).toInt()
        }
        return maxCapacitors
    }
}