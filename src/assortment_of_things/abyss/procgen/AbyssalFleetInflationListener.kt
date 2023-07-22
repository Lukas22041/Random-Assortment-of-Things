package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetInflater
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils

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
            if (fleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
            {
                var difficulty = AbyssUtils.getDifficulty()
                var tier = AbyssUtils.getTier(fleet.containingLocation)

                var chance = WeightedRandomPicker<Int>()


                if (tier == AbyssProcgen.Tier.Low) {
                    chance.add(0, 100f)
                }

                if (difficulty == AbyssDifficulty.Hard) {
                    if (tier == AbyssProcgen.Tier.Mid) {
                        chance.add(0, 0.2f)
                        chance.add(1, 0.4f)
                        chance.add(2, 0.2f)

                    }
                    else if (tier == AbyssProcgen.Tier.High) {
                        chance.add(0, 0.2f)
                        chance.add(1, 0.5f)
                        chance.add(2, 0.3f)
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

                for (member in fleet.fleetData.membersListCopy)
                {
                    addSmods(member, fleet.commander, chance)
                }
            }
        }
    }

    private fun addSmods(member: FleetMemberAPI, commander: PersonAPI, chance: WeightedRandomPicker<Int>)
    {
        var smods = chance.pick()
        member.fixVariant()

        var stats = commander.stats
        var variant = member.variant

        var modsOnShip = variant.nonBuiltInHullmods.map { Global.getSettings().getHullModSpec(it) }
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