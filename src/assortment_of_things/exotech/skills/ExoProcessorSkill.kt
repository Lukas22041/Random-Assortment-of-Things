package assortment_of_things.exotech.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.AdaptivePhaseCoils
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ExoProcessorSkill : RATBaseShipSkill() {

    val missileAmmoBonus = 25f
    val missileSpeedMult = 1.33f
    var modID = "rat_exo_processor"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {

        info!!.addPara("Increases the required hardflux to reach the minimum phase speed by 200%%. " +
                "This effectively reduces the maximum amount of speed reduction from phase coil stress, while also making it take longer to get to that point.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "200%", "speed reduction")
        //info.addPara("+25%% missile weapon ammo capacity", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
       // info.addPara("+33%% missile speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.dynamic.getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD)
            .modifyPercent(id, 200f)

      /*  stats!!.missileAmmoBonus.modifyPercent(id, missileAmmoBonus)

        stats!!.missileMaxSpeedBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileAccelerationBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileMaxTurnRateBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileTurnAccelerationBonus.modifyMult(id, missileSpeedMult)*/

    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}