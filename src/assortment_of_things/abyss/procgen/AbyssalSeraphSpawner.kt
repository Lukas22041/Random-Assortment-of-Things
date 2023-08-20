package assortment_of_things.abyss.procgen

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.util.WeightedRandomPicker
import java.util.*

object AbyssalSeraphSpawner {

    fun addSeraphsToFleet(fleet: CampaignFleetAPI, random: Random, max: Int, chancePer: Float) {



        var picker = WeightedRandomPicker<String>()
        picker.random = random

        picker.add("rat_sariel_Attack", 5f)
        picker.add("rat_sariel_Strike", 2f)

        for (i in 0 until max) {
           if (random.nextFloat() >= chancePer) continue

            var pick = picker.pick()
            var variant = Global.getSettings().getVariant(pick)

            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant.hullVariantId)
            member.repairTracker.cr = member.repairTracker.maxCR

            fleet.fleetData.addFleetMember(member)
        }
    }

}