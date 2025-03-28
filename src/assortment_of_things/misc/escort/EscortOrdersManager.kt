package assortment_of_things.misc.escort

import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils

class EscortOrdersManager : BaseEveryFrameCombatPlugin() {

    //FleetMemberID / Priority
    class EscortData(var isEscortMode: Boolean, var escorts: HashMap<String, Int>)

    companion object {

        //FleetMemberID / Data
        fun getEscortData() : HashMap<String, EscortData> {
            var key = "\$rat_escort_orders_data"
            var data = Global.getSector().memoryWithoutUpdate.get(key) as HashMap<String, EscortData>?
            if (data == null) {
                data = HashMap<String, EscortData>()
                Global.getSector().memoryWithoutUpdate.set(key, data)
            }
            return data
        }

        fun fillNewEntries() {
            var data = getEscortData()

            for (member in Global.getSector().playerFleet.fleetData.membersListCopy) {
                var entry = data.get(member.id)
                if (entry == null) {
                    entry = EscortData(false, HashMap())
                    data.set(member.id, entry)
                }

                /*if (entry!!.isEscortMode == true) {*/
                    for (other in Global.getSector().playerFleet.fleetData.membersListCopy) {
                        if (!entry!!.escorts.contains(other.id)) {
                            entry.escorts.put(other.id, 5)
                        }
                    }
                //}
            }
        }
    }

    init {
        if (RATSettings.escortEnabled!!) {
            fillNewEntries()
        }
    }

    var interval = IntervalUtil(0.4f, 0.5f)

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (!RATSettings.escortEnabled!!) return

        interval.advance(amount)
        if (interval.intervalElapsed()) {


            var data = getEscortData()
            for (ship in Global.getCombatEngine().ships) {

                if (ship.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP)) continue //To stabilise the script, dont swap if there is already an escort flag

                if (ship.fleetMember == null) continue

                var entry = data.get(ship.fleetMemberId) ?: continue
                if (!entry.isEscortMode) continue

                var escortCandidates = Global.getCombatEngine().ships
                escortCandidates = escortCandidates.filter { it.fleetMember != null && entry.escorts.contains(it.fleetMember.id) }
                escortCandidates = escortCandidates.filter { it.isAlive && it.owner == ship.owner }
                escortCandidates = escortCandidates.filter { it != ship }

                var escortValues = entry.escorts

                var toEscort = HashMap<ShipAPI, Int>()
                for (candidate in escortCandidates) {
                    var int = escortValues.get(candidate.fleetMember.id) ?: 0
                    toEscort.put(candidate, int)
                }

                //Filter out targets that are lower
                var max = toEscort.maxOf { it.value }
                toEscort = toEscort.filter { it.value == max } as HashMap<ShipAPI, Int>

                var escorts = toEscort.toList()
                escorts = escorts.sortedWith(compareByDescending<Pair<ShipAPI, Int>> { it.first.hullSize }.thenBy { MathUtils.getDistance(ship, it.first) })

                if (escorts.isEmpty()){
                    ship.aiFlags.unsetFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP)
                } else {
                    var target = escorts.first() ?: continue
                    ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 5f, target.first)
                }
            }

        }


        //ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 5f, other)

    }

}