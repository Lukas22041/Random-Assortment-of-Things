package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class BiomodifiedSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("This officer has been bioengineered in to a new form, improving reflexes and decision making. However their decisions appear to always be of the reckless kind. \n\n" +
                "This officer will always be of \"Reckless\" personality, any attempts at changing it will be reverted \n\n" +
                "+5%% timeflow\n" +
                "+5%% damage dealt\n" +
                "+10 max speed\n" +
                "+25%% ship maneuverability\n" +
                "+10%% damage reduction.",
            0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(5f)
        info.addNegativePara("-10%% max combat readiness")

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var ship = stats!!.entity

        var timeMod = 1.05f
        stats!!.timeMult.modifyMult(id, timeMod);


        if (ship is ShipAPI) {

            ship.addListener(object: AdvanceableListener {
                override fun advance(amount: Float) {
                    if (ship == Global.getCombatEngine().playerShip)
                    {
                        Global.getCombatEngine().timeMult.modifyMult(id + ship.id, 1 / timeMod)
                    }
                    else
                    {
                        Global.getCombatEngine().timeMult.unmodify(id + ship.id)
                    }
                }

            })
        }


        if (stats.fleetMember != null && stats.fleetMember.captain != null && stats.fleetMember.captain.stats.hasSkill("rat_biomutant")) {
            stats.fleetMember.captain.setPersonality(Personalities.RECKLESS)
        }


        stats.damageToFrigates.modifyMult(id, 1.05f)
        stats.damageToDestroyers.modifyMult(id, 1.05f)
        stats.damageToCruisers.modifyMult(id, 1.05f)
        stats.damageToCapital.modifyMult(id, 1.05f)

        stats.maxSpeed.modifyFlat(id, 10f)
        stats.acceleration.modifyMult(id, 1.25f)
        stats.deceleration.modifyMult(id, 1.25f)
        stats.maxTurnRate.modifyMult(id, 1.25f)
        stats.turnAcceleration.modifyMult(id, 1.25f)

        stats.hullDamageTakenMult.modifyMult(id, 0.9f)
        stats.shieldDamageTakenMult.modifyMult(id, 0.9f)
        stats.armorDamageTakenMult.modifyMult(id, 0.9f)

        stats.maxCombatReadiness.modifyFlat(id, -0.1f, "Biomutant")

    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}