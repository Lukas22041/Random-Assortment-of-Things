package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TimeCoreSkill : RATBaseShipSkill() {

    var modID = "rat_core_time"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("+10%% timeflow", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("+20%% maneuverability", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("+5 su/second to zero flux speed boost", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        stats!!.timeMult.modifyMult(modID, 1.1f)
        stats.acceleration.modifyMult(modID, 1.20f)
        stats.deceleration.modifyMult(modID, 1.20f)
        stats.turnAcceleration.modifyMult(modID, 1.20f)
        stats.zeroFluxSpeedBoost.modifyFlat(modID, 5f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {
        stats!!.timeMult.unmodify(modID)
        stats.acceleration.unmodify(modID)
        stats.deceleration.unmodify(modID)
        stats.turnAcceleration.unmodify(modID)
        stats.zeroFluxSpeedBoost.unmodify(modID)
    }

}