package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.skills.SystemsExpertise
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class PerfectPlanningSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("The ship takes up to 25%% less hull and armor damage depending on how much damage it has already taken. The effect reaches its maximum strength at 30%% hitpoints remaining." , 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(5f)
        info.addPara("-40%% crew required for the ship.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        stats!!.minCrewMod.modifyMult(id, 0.6f)

        var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (!ship.hasListenerOfClass(PerfectPlanningListener::class.java)) {
                ship.addListener(PerfectPlanningListener(ship))
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}

class PerfectPlanningListener(var ship: ShipAPI) : AdvanceableListener {
    override fun advance(amount: Float) {
        var stats = ship.mutableStats


        var level = (ship.hitpoints - (ship.maxHitpoints)) / ((ship.maxHitpoints * 0.3f) - (ship.maxHitpoints))
        level = MathUtils.clamp(level, 0f, 1f)

        var mod = 1f - (0.25f * level)

        stats.armorDamageTakenMult.modifyMult("rat_perfect_planning", mod)
        stats.hullDamageTakenMult.modifyMult("rat_perfect_planning", mod)
    }

}