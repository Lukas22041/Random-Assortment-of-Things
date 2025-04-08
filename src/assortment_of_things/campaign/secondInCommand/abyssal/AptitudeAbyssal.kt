package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.specs.SCAptitudeSection
import second_in_command.specs.SCBaseAptitudePlugin

class AptitudeAbyssal : SCBaseAptitudePlugin() {

    companion object {

    }

    override fun addCodexDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The Abyssal aptitude is added by Random Assortment of Things. Its officer can be found in the wreck of an abyssal droneship near a major lightsource in the abyssal depths. The aptitude at large plays like an even more aggressive alternative to the Automation aptitude. ",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "Abyssal", "the wreck of an abyssal droneship near a major lightsource", "Automation")
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
        section1.addSkill("rat_abyssal_angelic_presence")
        addSection(section1)

        var section2 = SCAptitudeSection(true, 2, "technology3")
        section2.addSkill("rat_abyssal_equivalent_exchange")
        section2.addSkill("rat_abyssal_forgotten_pact")
        addSection(section2)

        var section3 = SCAptitudeSection(true, 4, "technology5")
        section3.addSkill("rat_abyssal_abyssal_symphony")
        addSection(section3)

    }

    override fun getNPCFleetSpawnWeight(data: SCData, fleet: CampaignFleetAPI)  : Float {
        return 0f
    }

}