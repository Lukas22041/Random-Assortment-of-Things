package assortment_of_things.campaign.skills

import assortment_of_things.campaign.skills.util.RATBaseFleetSkill
import assortment_of_things.campaign.skills.util.SkillManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FleetStatsSkillEffect
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.LevelBasedEffect.ScopeDescription
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class PocketDimensionSkill : RATBaseFleetSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return ScopeDescription.FLEET
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?,width: Float) {

        if (SkillManager.addUnavailableTooltip(info!!, skill!!.id)) return

        info!!.addPara("Unlocks the \"Pocket Dimension\" Ability, which moves the fleet towards another dimension. This alternate dimension holds some abandoned structure that can be used for " +
                "storage applications.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
    }

    override fun apply(stats: MutableFleetStatsAPI?, id: String?, level: Float) {
        if (Global.getSector() == null || Global.getSector().playerFleet == null) return
        if (!Global.getSector().playerFleet.hasAbility("rat_pocket_dimension_ability"))
        {
            Global.getSector().getCharacterData().addAbility("rat_pocket_dimension_ability")
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_pocket_dimension_ability", true, 0f);
            //Global.getSector().playerFleet.addAbility("rat_pocket_dimension_ability")
        }
    }

    override fun unapply(stats: MutableFleetStatsAPI?, id: String?) {

    }
}