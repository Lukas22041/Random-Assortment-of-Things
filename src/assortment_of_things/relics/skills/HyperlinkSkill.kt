package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class HyperlinkSkill1 : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {

    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        stats!!.fighterWingRange.modifyFlat(id, 500f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }
}

class HyperlinkSkill2 : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.SHIP_FIGHTERS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)

        info.addPara("+500 maximum engagement range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+50 weapon range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+5%% timeflow", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+15%% damage to ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.timeMult.modifyMult(id, 1.05f)
        stats.damageToFrigates.modifyMult(id, 1.15f)
        stats.damageToDestroyers.modifyMult(id, 1.15f)
        stats.damageToCruisers.modifyMult(id, 1.15f)
        stats.damageToCapital.modifyMult(id, 1.15f)

        stats.ballisticWeaponRangeBonus.modifyMult(id, 50f)
        stats.energyWeaponRangeBonus.modifyMult(id, 50f)
        stats.missileWeaponRangeBonus.modifyMult(id, 50f)

    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}