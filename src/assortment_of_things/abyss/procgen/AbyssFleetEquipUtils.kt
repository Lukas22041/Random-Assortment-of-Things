package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.HashMap

object AbyssFleetEquipUtils {

    fun addAlterationsToFleet(fleet: CampaignFleetAPI, chancePerShip: Float, random: Random) {

        var members = fleet.fleetData.membersListCopy

        fleet.inflateIfNeeded()
        for (member in members)
        {
            if (random.nextFloat() > chancePerShip) continue

            var alterations = HashMap<String,Float>()
            alterations.put("rat_qualityAssurance", 1f)
            alterations.put("rat_timegear", 1f)
            alterations.put("rat_overloaded_systems", 1f)
            alterations.put("rat_advanced_flux_crystal", 1f)
            alterations.put("rat_boost_redirector", 1f)
            alterations.put("rat_soft_shields", 1f)

            if (!member.isFrigate) {
                alterations.put("rat_preperation", 1f)
            }

            var hasBay = member.variant.baseOrModSpec().hints.contains(ShipHullSpecAPI.ShipTypeHints.CARRIER)

            var hasBallistic = false
            var hasEnergy = false
            var hasMissile = false

            var weapons = member.variant.fittedWeaponSlots.map { member.variant.getWeaponSpec(it) }
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.BALLISTIC }) hasBallistic = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.ENERGY }) hasEnergy = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.MISSILE }) hasMissile = true

            if (hasBay) alterations.putAll(mapOf("rat_temporalAssault" to 0.5f, "rat_perseverance" to 1f, "rat_magneticStorm" to 1f, "rat_plasmaticShield" to 1f))

            if (hasBallistic) alterations.putAll(mapOf("rat_ballistic_focus" to 1.5f))
            if (hasEnergy) alterations.putAll(mapOf("rat_energy_focus" to 1.5f))
            if (hasMissile) alterations.putAll(mapOf("rat_missile_reserve" to 0.5f))

            var picker = WeightedRandomPicker<String>()
            alterations.forEach { picker.add(it.key, it.value) }

            var pick = picker.pick()

            if (member.variant.source != VariantSource.REFIT)
            {
                var variant = member.variant.clone();
                variant.originalVariant = null;
                variant.hullVariantId = Misc.genUID()
                variant.source = VariantSource.REFIT
                member.setVariant(variant, false, true)
            }
            member.variant.addMod(pick)
            member.updateStats()

        }
        fleet.inflateIfNeeded()

    }

    fun addAICores(fleet: CampaignFleetAPI, extraChance: Float)
    {
        var random = Random()

        var corePercentage = 0.4f
        corePercentage += extraChance

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

            /*if (type == AbyssFleetType.MonoChronos) core = chronos.createPerson(RATItems.CHRONOS_CORE, fleet.faction.id, random)
            else if (type == AbyssFleetType.MonoCosmos) core = cosmos.createPerson(RATItems.COSMOS_CORE, fleet.faction.id, random)
            else if (type == AbyssFleetType.MonoSeraphOnNormal) core = seraph.createPerson(RATItems.SERAPH_CORE, fleet.faction.id, random)

            else */if (variantID.contains("temporal") || variantID.contains("chronos"))
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

    fun inflate(fleet: CampaignFleetAPI, zeroSmodWeight: Float, oneSmodWeight: Float, threeSmodWeight: Float) {

        var chance = WeightedRandomPicker<Int>()
        chance.add(0, zeroSmodWeight)
        chance.add(1, oneSmodWeight)
        chance.add(2, threeSmodWeight)

        for (member in fleet.fleetData.membersListCopy)
        {
            inflateShip(member, fleet.commander, chance)
        }
    }

    private fun inflateShip(member: FleetMemberAPI, commander: PersonAPI, chance: WeightedRandomPicker<Int>)
    {
        var smods = chance.pick()
        member.fixVariant()

        var stats = commander.stats
        var variant = member.variant

        var modsOnShip = variant.nonBuiltInHullmods.map { Global.getSettings().getHullModSpec(it) }.filter { it.id != HullMods.SAFETYOVERRIDES && it.id != HullMods.PHASE_ANCHOR }
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