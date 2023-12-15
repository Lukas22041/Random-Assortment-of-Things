package assortment_of_things.frontiers.scripts

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener

class SettlementManager(var settlement: SettlementData) : EveryFrameScript, EconomyTickListener {


    override fun advance(amount: Float) {
        var slots = settlement.facilitySlots
        for (slot in slots) {
            if (slot.facilityID == "") continue

            if (slot.isBuilding) {
                slot.updateDays()
                if (slot.daysRemaining <= 0.1) {
                    slot.finishConstruction()
                }
            }
            else {
                slot.getFacilityPlugin()?.advance(settlement, amount)
            }
        }
    }

    override fun reportEconomyTick(iterIndex: Int) {

    }

    override fun reportEconomyMonthEnd() {

    }


    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


}