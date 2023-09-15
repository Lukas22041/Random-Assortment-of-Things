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

class MassInterfaceSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.SHIP_FIGHTERS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("The officer is connected to an extensive neural interface that is capable of transmitting data to all deployed fighters at the same time.\n\n" +
                "+100 weapon range.\n" +
                "+10%% weapon firerate\n" +
                "-20%% emp and shield damage taken.",
            0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.ballisticWeaponRangeBonus.modifyFlat(id, 100f)
        stats!!.energyWeaponRangeBonus.modifyFlat(id, 100f)
        stats!!.missileWeaponRangeBonus.modifyFlat(id, 100f)

        stats.ballisticRoFMult.modifyMult(id, 1.1f)
        stats.energyRoFMult.modifyMult(id, 1.1f)
        stats.missileRoFMult.modifyMult(id, 1.1f)

        stats.empDamageTakenMult.modifyMult(id, 0.8f)
        stats.shieldDamageTakenMult.modifyMult(id, 0.8f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}