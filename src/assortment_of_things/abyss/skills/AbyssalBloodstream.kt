package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssalBloodstream : RATBaseShipSkill() {

    var modID = "rat_abyssal_bloodstream"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("+20%% ballistic & energy weapon damage.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(5f)

        info!!.addPara("-10%% ballistic & energy weapon range.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        info!!.addPara("+15%% damage taken.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.energyWeaponDamageMult.modifyMult(modID, 1.2f)
        stats.ballisticWeaponDamageMult.modifyMult(modID, 1.2f)

        stats.energyWeaponRangeBonus.modifyMult(modID, 0.9f)
        stats.ballisticWeaponRangeBonus.modifyMult(modID, 0.9f)

        stats.shieldAbsorptionMult.modifyMult(modID, 1.15f)
        stats.armorDamageTakenMult.modifyMult(modID, 1.15f)
        stats.hullDamageTakenMult.modifyMult(modID, 1.15f)

    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}