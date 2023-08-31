package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class MaintainingMomentumSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Whenever the ship destroys or disables an opponent, it gains a stack of \"Momentum\". \nA stack of momentum lasts for 60 seconds and gives the following benefits:" , 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(5f)
        info.addPara("+20%% damage dealt per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+10%% flux dissipation per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+10%% max speed per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        var ship = stats!!.entity
        if (ship is ShipAPI) {

            if (!Global.getCombatEngine().listenerManager.hasListenerOfClass(MaintaningMomentumListener::class.java)) {
                var listener = MaintaningMomentumListener(ship)
                Global.getCombatEngine().listenerManager.addListener(listener)
                ship.addListener(listener)
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

}

class MaintaningMomentumListener(var pilotedShip: ShipAPI) : HullDamageAboutToBeTakenListener, AdvanceableListener {

    class MomentumStacks(var duration: Float)

    var duration = 60f
    var stacks = ArrayList<MomentumStacks>()

    var id = "rat_momentum"

    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?,  point: Vector2f?, damageAmount: Float): Boolean {

        if (ship == pilotedShip) {
            return false
        }

        if (param is ShipAPI) {
            if (param != pilotedShip) return false
            if (ship!!.hitpoints <= 0 && !ship.hasTag("rat_maverick_counted")) {
                ship.addTag("rat_maverick_counted")
                stacks.add(MomentumStacks(duration))
            }
        }

        return false
    }

    override fun advance(amount: Float) {

        for (stack in ArrayList(stacks)) {
            stack.duration -= 1 * amount

            if (stack.duration < 0f) {
                stacks.remove(stack)
            }
        }


        var stats = pilotedShip.mutableStats
        var count = stacks.count()

        stats.damageToCapital.modifyMult(id, 1f + (0.2f * count))
        stats.damageToCruisers.modifyMult(id, 1f + (0.2f * count))
        stats.damageToDestroyers.modifyMult(id, 1f + (0.2f * count))
        stats.damageToFrigates.modifyMult(id, 1f + (0.2f * count))

        stats.fluxDissipation.modifyMult(id, 1 + (0.1f * count))

        stats.maxSpeed.modifyMult(id, 1 + (0.1f * count))
    }

}