package assortment_of_things.exotech.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
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

        if (currentWarp == null && !exoship.isInTransit) {
            currentWarp = findNewDestination()
        }

        if (currentWarp != null && !exoship.isInTransit) {

            currentWarp!!.daysTilWarp -= days

            if (currentWarp!!.daysTilWarp <= 0 && !exoship.isInTransit) {
                exoship.warpModule.warp(currentWarp!!.destination, false) {
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

        var systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(
            Tags.THEME_HIDDEN)}

        var filtered = systems.filter { system ->
            system.planets.none { Global.getSector().economy.marketsCopy.contains(it.market) }
        }

        var random = filtered.randomOrNull() ?: return null

        var planet = random.planets.filter { !it.isStar }.randomOrNull() ?: return null

        var days = MathUtils.getRandomNumberInRange(minDays, maxDays)

        if (timeOverwrite != null) {
            days = timeOverwrite
        }

        currentWarp = WarpDestination(planet, days)
        return currentWarp
    }
}