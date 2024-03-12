package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SpaceCoreSkill : RATBaseShipSkill() {

    var modID = "rat_core_space"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {

        info!!.addPara("The ships max speed and maneuverability is reduced by 25%% and the weapon fire rate is reduced by 20%%. " +
                "In exchange it gains an 15%% increase in ballistic and energy weapon range, 10%% increased damage towards ships, and takes 10%% less damage from all sources.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "25%", "20%", "15%", "10%", "10%")
        info.addSpacer(2f)

    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.maxSpeed.modifyMult(modID, 0.75f)
        stats.acceleration.modifyMult(modID, 0.75f)
        stats.deceleration.modifyMult(modID, 0.75f)
        stats.turnAcceleration.modifyMult(modID, 0.75f)
        stats.maxTurnRate.modifyMult(modID, 0.75f)

        stats!!.damageToFighters.modifyMult(modID, 1.1f)
        stats.damageToFrigates.modifyMult(modID, 1.1f)
        stats.damageToDestroyers.modifyMult(modID, 1.1f)
        stats.damageToCruisers.modifyMult(modID, 1.1f)
        stats.damageToCapital.modifyMult(modID, 1.1f)

        stats.energyWeaponRangeBonus.modifyPercent(modID, 15f)
        stats.ballisticWeaponRangeBonus.modifyPercent(modID, 15f)

        stats.ballisticRoFMult.modifyMult(modID, 0.8f)
        stats.energyRoFMult.modifyMult(modID, 0.8f)
        stats.missileRoFMult.modifyMult(modID, 0.8f)

        stats.shieldAbsorptionMult.modifyMult(modID, 0.90f)
        stats.armorDamageTakenMult.modifyMult(modID, 0.90f)
        stats.hullDamageTakenMult.modifyMult(modID, 0.90f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {
        stats!!.damageToFighters.unmodify(modID)
        stats.damageToFrigates.unmodify(modID)
        stats.damageToDestroyers.unmodify(modID)
        stats.damageToCruisers.unmodify(modID)
        stats.damageToCapital.unmodify(modID)

        stats.energyWeaponRangeBonus.unmodify(modID)
        stats.ballisticWeaponRangeBonus.unmodify(modID)

        stats.shieldAbsorptionMult.unmodify(modID)
        stats.armorDamageTakenMult.unmodify(modID)
        stats.hullDamageTakenMult.unmodify(modID)
    }

}