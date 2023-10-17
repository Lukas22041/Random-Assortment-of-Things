package assortment_of_things.abyss.scripts

import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags

class AbyssDoctrineLearnedListener(var fleet: CampaignFleetAPI) {

    fun learnedAbyssalDoctrine() {
        for (member in fleet.fleetData.membersListWithFightersCopy) {
            if (member.baseOrModSpec().hasTag("rat_abyssals") && !member.baseOrModSpec().hasTag("rat_seraph")) {
                member.variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP)
            }
        }
    }

    fun learnedSeraphDoctrine() {
        for (member in fleet.fleetData.membersListWithFightersCopy) {
            if (member.baseOrModSpec().hasTag("rat_seraph")) {
                member.variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP)
            }
        }
    }

}