package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

object AbyssalSeraphSpawner {

    fun addSeraphsToFleet(fleet: CampaignFleetAPI, random: Random, min: Int, max: Int) {

        var picker = WeightedRandomPicker<String>()
        picker.random = random

        picker.add("rat_raguel_Attack", 6f)
        picker.add("rat_raguel_Strike", 4f)

        picker.add("rat_sariel_Attack", 5f)
        picker.add("rat_sariel_Strike", 2f)

        picker.add("rat_gabriel_Attack", 1.25f)
        picker.add("rat_gabriel_Burst", 1.25f)


        var amount = MathUtils.getRandomNumberInRange(min, max)
        for (i in 0 until amount) {

            var pick = picker.pick()
            var variant = Global.getSettings().getVariant(pick)

            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant.hullVariantId)
            member.repairTracker.cr = member.repairTracker.maxCR

            var core = SeraphCore().createPerson(RATItems.SERAPH_CORE, fleet.faction.id, random)
            member.captain = core

            fleet.fleetData.addFleetMember(member)
            fleet.fleetData.sort()
        }
    }

    fun sortWithSeraphs(fleet: CampaignFleetAPI) {
        var members = fleet.fleetData.membersListCopy.toMutableList()

        for (member in ArrayList(members)) {
            fleet.fleetData.removeFleetMember(member)
        }

        var seraphs = members.filter { it.baseOrModSpec().hasTag("rat_seraph") }
        seraphs = seraphs.sortedByDescending { it.variant.hullSize }

        for (member in seraphs) {
            fleet.fleetData.addFleetMember(member)
            members.remove(member)
        }

        members = members.sortedByDescending { it.variant.hullSize }.toMutableList()

        for (member in ArrayList(members)) {
            fleet.fleetData.addFleetMember(member)
            members.remove(member)
        }

    }

}