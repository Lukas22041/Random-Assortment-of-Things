package assortment_of_things.backgrounds.commander

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel.ProductionData
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.util.*

class CommanderCustomProduction(var market: MarketAPI, var listener: CommanderStationListener) : CustomProductionPickerDelegate {

    override fun getAvailableShipHulls(): MutableSet<String> {
        var ships = Global.getSector().playerFaction.knownShips
        return ships
    }

    override fun getAvailableWeapons(): MutableSet<String> {
        var weapons = Global.getSector().playerFaction.knownWeapons
        return weapons
    }

    override fun getAvailableFighters(): MutableSet<String> {
        var fighters = Global.getSector().playerFaction.knownFighters
        return fighters
    }

    override fun getCostMult(): Float {
        return 1f
    }

    override fun getMaximumValue(): Float {
        return listener.currentProductionBudget
    }

    override fun withQuantityLimits(): Boolean {
        return false
    }

    override fun notifyProductionSelected(production: FactionProductionAPI) {
        convertProdToCargo(production)
    }


    protected fun convertProdToCargo(prod: FactionProductionAPI) {
        var cost = prod.totalCurrentCost
        Global.getSector().playerFleet.cargo.credits.subtract(cost.toFloat())

        listener.currentProductionBudget -= cost
        listener.currentProductionBudget = MathUtils.clamp(listener.currentProductionBudget, 0f, 100000000f)
        var data = ProductionData()

        var cargo = Global.getFactory().createCargo(false)

        val ships = Global.getFactory().createEmptyFleet(Factions.PLAYER, "temp", true)
        ships.commander = Global.getSector().playerPerson
        ships.fleetData.shipNameRandom = Random()
        val p = DefaultFleetInflaterParams()
        p.quality = 1.5f
        p.mode = ShipPickMode.PRIORITY_THEN_ALL
        p.persistent = false
        p.seed = Random().nextLong()
        p.timestamp = null

        val inflater = Misc.getInflater(ships, p)
        ships.inflater = inflater

        for (item in prod.current) {
            val count = item.quantity
            if (item.type == ProductionItemType.SHIP) {
                for (i in 0 until count) {
                    ships.fleetData.addFleetMember(item.specId + "_Hull")
                }
            } else if (item.type == ProductionItemType.FIGHTER) {
                cargo.addFighters(item.specId, count)
            } else if (item.type == ProductionItemType.WEAPON) {
                cargo.addWeapons(item.specId, count)
            }
        }

        if (cargo.mothballedShips == null) {
            cargo.initMothballedShips(Factions.PLAYER)
        }

        ships.inflateIfNeeded()
        for (member in ships.fleetData.membersListCopy) {
            if (member.variant.source == VariantSource.REFIT) {
                member.variant.clear()
            }
            cargo.mothballedShips.addFleetMember(member)
        }

        var level = (cost - 0f) / (listener.maxProductionCapacity - 0f)
        var days = 120 * level
        days = MathUtils.clamp(days, 10f, 60f)
        var intel = CommanderProductionIntel(market, cargo, days)
        Global.getSector().intelManager.addIntel(intel)
    }

}