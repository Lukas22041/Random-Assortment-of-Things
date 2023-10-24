package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class CyberneticAugmentationSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Allows this person to command Automated Ships. Does not apply a multipler to the \"Automated Ships\" skill points cost on the ship. \n\n" +
                "Can not be assigned to automated ships through the usual way, it is instead done through a button in \"Additional Options\" section in the refit screen for any elligable automated ship.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var fleet = Global.getSector().playerFleet
        var person = fleet?.fleetData?.officersCopy?.map { it.person }?.plus(Global.getSector().playerPerson)?.find { it.stats.hasSkill("rat_augmented") }?: return

        var member = Global.getSector().playerFleet.fleetData.membersListCopy.find { it.captain == person } ?: return
        if (member!!.variant.hasHullMod(HullMods.AUTOMATED)) Misc.setUnremovable(person, true)
        else Misc.setUnremovable(person, false)


        var count = fleet.fleetData.membersListCopy.count { it.captain == person }
        if (count >= 2) {
            fleet.fleetData.membersListCopy.forEach {
                if (it == member) return@forEach
                if (it.captain == person) {
                    member.captain = null
                }
            }
        }


        /*var deployCost = member.deploymentPointsCost
        person.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_VALUE, -deployCost)*/
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}