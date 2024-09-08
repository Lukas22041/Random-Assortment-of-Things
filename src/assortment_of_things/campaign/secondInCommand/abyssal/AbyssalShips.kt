package assortment_of_things.campaign.secondInCommand.abyssal

import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin

class AbyssalShips : SCBaseAutoPointsSkillPlugin() {
    override fun getProvidedPoints(): Int {
        return 120
    }
}