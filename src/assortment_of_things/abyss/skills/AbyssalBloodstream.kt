package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.combat.TemporarySlowdown
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.impl.combat.TemporalShellStats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

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

        /*var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (!ship.hasListenerOfClass(AbyssalBloodstreamListener::class.java)) {
                ship.addListener(AbyssalBloodstreamListener(ship))
            }
        }*/

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

    class AbyssalBloodstreamListener(var ship: ShipAPI) : HullDamageAboutToBeTakenListener {

        override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {

            if (ship!!.hitpoints - damageAmount <= 0) {

                var player = Global.getCombatEngine().playerShip == ship
                var id = ship.id + "rat_bloodstream"
                var stats = ship.mutableStats
                val shipTimeMult = 100f
                stats.getTimeMult().modifyMult(id, shipTimeMult)

                if (player)     {
                    Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
                } else {
                    Global.getCombatEngine().timeMult.unmodify(id)
                }

                for (i in 0..100) {
                    ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                    var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                    var angle = Misc.getAngleInDegrees(ship.location, from)
                    var to = MathUtils.getPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(100f, 300f) + ship.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                    Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, Color(196, 20, 35, 255), Color(196, 20, 35, 255))

                }

                ship.hitpoints = ship.maxHitpoints
                return true
            }

            return false
        }

    }
}