package assortment_of_things.campaign.skills

import activators.ActivatorManager
import assortment_of_things.campaign.skills.util.RATBaseShipSkill
import assortment_of_things.campaign.skills.util.SkillManager
import assortment_of_things.combat.activators.TemporalAssaultActivator
import com.fs.starfarer.api.characters.*
import com.fs.starfarer.api.combat.FighterWingAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TemporalAssaultSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_FIGHTERS
    }

    override fun hasCustomDescription(): Boolean {
        return true
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {

        if (SkillManager.addUnavailableTooltip(info!!, skill!!.id)) return

        info!!.addPara("All fighters from carriers piloted by an officer receive the \"Temporal Assault\" secondary shipsystem, which temporarily increases their timeflow.", 0f,
        Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(5f)

        info.addPara(" - Timeflow is increased by a maximum of 50% while the system is active.", 0f)
        info.addSpacer(2f)
        info.addPara(" - Does not effect ships with AI-Core officers.", 0f)
    }


    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var ship = stats!!.entity


        if (ship is ShipAPI)
        {
            if (ship.wing != null)
            {
                var source = ship.wing.sourceShip
                if (source.captain == null || source.captain.name.first == "") return
                if (source.captain.isAICore) return


                ActivatorManager.addActivator(ship, TemporalAssaultActivator(ship))
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }
}