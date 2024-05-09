package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.misc.addPara
import assortment_of_things.relics.skills.MaintaningMomentumListener
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
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class PrimordialCoreSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.ALL_SHIPS
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
     /*   info!!.addPara("Whenever the ship destroys or disables an opponent, it gains a stack of \"Momentum\". \nA stack of momentum lasts for 60/60/45/30 seconds depending on hullsize and gives the following benefits:" , 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addSpacer(5f)
        info.addPara("+10%% damage dealt per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+10%% flux dissipation per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        info.addPara("+10%% max speed per stack", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())*/

        info!!.addPara("Whenever the ship destroys or disables an opponent, it gains a temporary increase in damage dealt and maximum speed of 10%%. This effect stacks and lasts for 60/60/45/30 seconds depending on the ships hullsize. " +
                "Additionaly, each time this effect is triggered it restores 10%% of the ships armor.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "destroys or disables", "damage dealt", "maximum speed", "10%", "60", "60", "45", "30", "10%")
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        var ship = stats!!.entity
        if (ship is ShipAPI) {

            var hasMomentum = false
            if (ship.captain != null) {
                if (ship.captain.stats.hasSkill("rat_maintaining_momentum")) {
                    hasMomentum = true
                }
            }

            if (ship.listenerManager?.hasListenerOfClass(PrimordialCoreListener::class.java) != true && !hasMomentum) {
                var listener = PrimordialCoreListener(ship)
                Global.getCombatEngine().listenerManager.addListener(listener)
                ship.addListener(listener)
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

        var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (ship.listenerManager?.hasListenerOfClass(PrimordialCoreListener::class.java) == true) {
                var listener = ship.listenerManager.getListeners(PrimordialCoreListener::class.java).first()
                listener.stacks.clear()
                listener.advance(0f)

                Global.getCombatEngine().listenerManager.removeListener(listener)
                ship.removeListener(listener)
            }
        }

    }

}

class PrimordialCoreListener(var pilotedShip: ShipAPI) : HullDamageAboutToBeTakenListener, AdvanceableListener {

    class PrimordialStacks(var duration: Float)

    var duration = 60f
    var stacks = ArrayList<PrimordialStacks>()

    var id = "rat_primordial_core"

    init {
        duration = when(pilotedShip.hullSize) {
            ShipAPI.HullSize.FRIGATE -> 60f
            ShipAPI.HullSize.DESTROYER -> 60f
            ShipAPI.HullSize.CRUISER -> 45f
            ShipAPI.HullSize.CAPITAL_SHIP -> 30f
            else -> 60f
        }
    }

    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?,  point: Vector2f?, damageAmount: Float): Boolean {

        if (ship == pilotedShip) {
            return false
        }

        if (param is ShipAPI) {
            if (param != pilotedShip) return false
            if (ship!!.isFighter) return false
            if (ship.owner == pilotedShip.owner) return false
            if (ship!!.hitpoints <= 0 && !ship.hasTag("rat_primordial_counted")) {
                ship.addTag("rat_primordial_counted")
                stacks.add(PrimordialStacks(duration))

                var armorGrid = pilotedShip.armorGrid
                for (x in armorGrid.grid.indices) {
                    for (y in armorGrid.grid.indices) {

                        var added = armorGrid.maxArmorInCell * 0.1f
                        var current = armorGrid.getArmorValue(x, y)
                        var new = added + current
                        new = MathUtils.clamp(new, 0f, armorGrid.maxArmorInCell)

                        armorGrid.setArmorValue(x, y, new)
                    }
                }

                pilotedShip.syncWithArmorGridState()
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

        stats.damageToCapital.modifyMult(id, 1f + (0.1f * count))
        stats.damageToCruisers.modifyMult(id, 1f + (0.1f * count))
        stats.damageToDestroyers.modifyMult(id, 1f + (0.1f * count))
        stats.damageToFrigates.modifyMult(id, 1f + (0.1f * count))

        stats.maxSpeed.modifyMult(id, 1 + (0.1f * count))
    }

}