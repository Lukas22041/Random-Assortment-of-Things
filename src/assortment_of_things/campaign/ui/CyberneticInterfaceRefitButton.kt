package assortment_of_things.campaign.ui

import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaRefit.BaseRefitButton

class CyberneticInterfaceRefitButton : BaseRefitButton() {

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        var people = Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }.plus(Global.getSector().playerPerson)

        var personWithSkill = people.find { it.stats.hasSkill("rat_augmented") } ?: return "Move Augmented Officer"

        if (member!!.captain == personWithSkill) {
            return "Remove Interfaced Officer"
        }

        return "Move Interfaced Officer"
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/icons/skills/rat_augmented.png"
    }

    override fun getOrder(member: FleetMemberAPI?, variant: ShipVariantAPI?): Int {
        return 110
    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        var people = Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }.plus(Global.getSector().playerPerson)

        var personWithSkill = people.find { it.stats.hasSkill("rat_augmented") }
        if (variant!!.hasHullMod(HullMods.AUTOMATED) && personWithSkill != null) {
            return true
        }

        return false
    }

    override fun hasTooltip(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun addTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {
        var person = Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }.plus(Global.getSector().playerPerson).find { it.stats.hasSkill("rat_augmented") }?: return

        tooltip!!.addSpacer(2f)
        var img = tooltip!!.beginImageWithText(person.portraitSprite, 64f)



        if (member!!.captain == person) {
            img.addPara("Click to remove ${person.nameString} from the ${member.shipName}.", 0f)
        }
        else {
            var hasCaptain = member.captain != null && !member.captain.isDefault

            img.addPara("Click to move ${person.nameString} to the ${member.shipName}. Once moved the officer can not be removed until this button is pressed again or ${person.heOrShe} is replaced by another core.", 0f)

            if (hasCaptain) {
                img.addSpacer(5f)
                img.addNegativePara("This ship already has a core or person commanding it. Remove them to be able to move the officer towards it.")
            }
        }

        tooltip.addImageWithText(0f)
        tooltip.addSpacer(2f)

    }

    override fun isClickable(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        var person = Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }.plus(Global.getSector().playerPerson).find { it.stats.hasSkill("rat_augmented") }?: return false

        if (person != member!!.captain && member.captain != null && !member.captain.isDefault) {
            return false
        }

        return true
    }

    override fun getToolipWidth(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Float {
        return 400f
    }

    override fun onClick(member: FleetMemberAPI?, variant: ShipVariantAPI?, event: InputEventAPI?, market: MarketAPI?) {
        var person = Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }.plus(Global.getSector().playerPerson).find { it.stats.hasSkill("rat_augmented") }?: return
        var previous = Global.getSector().playerFleet.fleetData.membersListCopy.find { it.captain == person }
        var playerfleet = Global.getSector().playerFleet

        if (member!!.captain == person) {
            member.captain = null
        }
        else {

            if (previous != null) {
                previous.captain = null
            }

            if (person == Global.getSector().playerPerson) {
                playerfleet.fleetData.setFlagship(member)
                member!!.isFlagship = true
            }

            member!!.captain = person

        }

        refreshVariant()
        refreshButtonList()
    }



}