package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.misc.RATControllerHullmod
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
import org.lwjgl.opengl.GL11

class AutoEngineerSkill : RATBaseShipSkill() {

    var modID = "rat_auto_engineer"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Ship is almost always recoverable if lost in combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info!!.addPara("+10%% Maximum Combat Readiness", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        //RATControllerHullmod.ensureAddedControllerToFleet()
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {


    }
}