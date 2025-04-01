package assortment_of_things.exotech.entities

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.ExoshipIntel
import assortment_of_things.exotech.interactions.HyperNavBeaconInteraction
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.GregorianCalendar

class ExoshipNPCModule(var exoship: ExoshipEntity, var exoshipEntity: SectorEntityToken) {


    data class WarpDestination(var destination: SectorEntityToken, var daysTilWarp: Float)

    var isPlayerOwned = false

    var currentWarp: WarpDestination? = null

    var minDays = 45f
    var maxDays = 90f

    init {
        currentWarp = findNewDestination()
    }


    fun advance(amount: Float) {
        if (isPlayerOwned) return
        if (Global.getSector().isPaused)  return

        var days = Misc.getDays(amount)

        if (currentWarp == null && !exoship.isInTransit && !ExoUtils.getExoData().preventNPCWarps) {
            currentWarp = findNewDestination()
        }

        if (currentWarp != null && !exoship.isInTransit && !ExoUtils.getExoData().preventNPCWarps) {

            currentWarp!!.daysTilWarp -= days

            if (currentWarp!!.daysTilWarp <= 0 && !exoship.isInTransit) {

                //Make the ship select a destination right as it chooses to move if the player does not have its intel yet.
                //This is to enable it being a bit more likely to move to a system the player is in.
                var intel = Global.getSector().intelManager
                if (!intel.hasIntelOfClass(ExoshipIntel::class.java)) {
                    currentWarp = findNewDestination()
                    if (currentWarp == null) return
                }

                exoship.warpModule.warp(currentWarp!!.destination, false) @JvmSerializableLambda {
                    currentWarp = null
                }

                //currentWarp = null
            }
        }

    }

    fun setWarp(destination: SectorEntityToken, daysTilWarp: Float) {
        currentWarp = WarpDestination(destination, daysTilWarp)
    }

    fun findNewDestination(timeOverwrite: Float? = null) : WarpDestination? {

        if (Global.getSector()?.playerFleet == null) return null

        var systems = Global.getSector().starSystems.filter { it != exoshipEntity.containingLocation && !it.hasTag(Tags.THEME_CORE) && /*!it.hasTag(Tags.THEME_REMNANT) &&*/ !it.hasPulsar() && !it.hasTag(
            Tags.THEME_HIDDEN)}

        var filtered = systems.filter { system ->
            system.planets.none { Global.getSector().economy.marketsCopy.contains(it.market) }
        }

        //var random = filtered.randomOrNull() ?: return null
        //var planet = random.planets.filter { !it.isStar }.randomOrNull() ?: return null


        var picker = WeightedRandomPicker<PlanetAPI>()
        for (system in filtered) {
            var baseWeight = 1f

            if (system.hasTag(Tags.THEME_RUINS)) baseWeight += 0.5f
            if (system.hasTag(Tags.THEME_DERELICT)) baseWeight += 0.25f
            if (system.hasTag(Tags.THEME_MISC)) baseWeight -= 0.1f
            if (system.hasTag(Tags.THEME_REMNANT)) baseWeight -= 0.25f

            if (Global.getSector().playerFleet.containingLocation == system) baseWeight * 1.5f
            else if (system.lastPlayerVisitTimestamp == 0L) {
                baseWeight *= 0.75f
            }

            for (planet in system.planets) {
                if (planet.isStar) continue

                var weight = baseWeight

                picker.add(planet, weight)
            }
        }

        var pick = picker.pick() ?: return null

        var days = MathUtils.getRandomNumberInRange(minDays, maxDays)

        if (timeOverwrite != null) {
            days = timeOverwrite
        }

        currentWarp = WarpDestination(pick, days)
        return currentWarp
    }
}