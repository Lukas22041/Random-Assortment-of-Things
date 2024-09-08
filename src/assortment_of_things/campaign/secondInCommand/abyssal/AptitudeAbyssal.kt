package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.specs.SCAptitudeSection
import second_in_command.specs.SCBaseAptitudePlugin

class AptitudeAbyssal : SCBaseAptitudePlugin() {

    companion object {

    }

    override fun getOriginSkillId(): String {
        return "rat_abyssal_abyssal_ships"
    }

    override fun createSections() {

        var section1 = SCAptitudeSection(true, 0, "technology1")
        section1.addSkill("rat_abyssal_mending")
        section1.addSkill("rat_abyssal_supercharged")
        section1.addSkill("rat_abyssal_submerge")
        section1.addSkill("rat_abyssal_stressed_grid")
        section1.addSkill("rat_abyssal_interlinked")
        addSection(section1)

        var section2 = SCAptitudeSection(true, 2, "technology3")
        section2.addSkill("rat_abyssal_angelic_presence")
        section2.addSkill("rat_abyssal_equivalent_exchange")
        section2.addSkill("rat_abyssal_forgotten_pact")
        addSection(section2)

        var section3 = SCAptitudeSection(true, 4, "technology5")
        section3.addSkill("rat_abyssal_abyssal_symphony")
        addSection(section3)

    }

    override fun getNPCFleetSpawnWeight(data: SCData, fleet: CampaignFleetAPI)  : Float {
        if (fleet.flagship?.isAutomated() == true) return Float.MAX_VALUE

        if (fleet.fleetData.membersListCopy.any { it.isAutomated() }) return 3f

        return 0f
    }

}