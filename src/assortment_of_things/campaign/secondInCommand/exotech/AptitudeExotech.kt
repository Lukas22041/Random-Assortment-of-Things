package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.specs.SCAptitudeSection
import second_in_command.specs.SCBaseAptitudePlugin

class AptitudeExotech : SCBaseAptitudePlugin() {

    override fun addCodexDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The Exotech aptitude is added by Random Assortment of Things. It can be acquired by talking to Amelie after having finished the first mission in the Exotech questline. The aptitude hosts a mix of high tech, carrier and phase skills. ",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "Exotech", "acquired by talking to Amelie after having finished the first mission")
    }

    override fun getOriginSkillId(): String {
        return "rat_exotech_hyperspatial_reconfiguration"
    }

    override fun createSections() {

        var section1 = SCAptitudeSection(true, 0, "technology1")
        section1.addSkill("rat_exotech_undetectable")
        section1.addSkill("rat_exotech_voyager")
        section1.addSkill("rat_exotech_coil_efficiency")
        section1.addSkill("rat_exotech_space_superiority")
        section1.addSkill("rat_exotech_packed_capacitors")
        addSection(section1)

        var section2 = SCAptitudeSection(true, 2, "technology3")
        section2.addSkill("rat_exotech_phase_drift")
        section2.addSkill("rat_exotech_standardised")
        section2.addSkill("rat_exotech_intercept_formation")
        addSection(section2)

        var section3 = SCAptitudeSection(true, 4, "technology5")
        section3.addSkill("rat_exotech_dimensional_chain")
        addSection(section3)

    }

    override fun guaranteePick(fleet: CampaignFleetAPI): Boolean {
        if (fleet.faction.id == "rat_exotech") return true
        return false
    }

    override fun getNPCFleetSpawnWeight(data: SCData, fleet: CampaignFleetAPI)  : Float {
        return 0f
    }

}