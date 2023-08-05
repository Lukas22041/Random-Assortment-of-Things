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
        info!!.addSpacer(2f)
        info!!.addPara("+10%% damage to ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("+15%% ballistic and energy weapon range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("-10%% shield, armor & hull damage taken.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.damageToFighters.modifyMult(modID, 1.1f)
        stats.damageToFrigates.modifyMult(modID, 1.1f)
        stats.damageToDestroyers.modifyMult(modID, 1.1f)
        stats.damageToCruisers.modifyMult(modID, 1.1f)
        stats.damageToCapital.modifyMult(modID, 1.1f)

        stats.energyWeaponRangeBonus.modifyMult(modID, 1.1f)
        stats.ballisticWeaponRangeBonus.modifyMult(modID, 1.1f)

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