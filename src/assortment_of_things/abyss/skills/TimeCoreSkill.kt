package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils

class TimeCoreSkill : RATBaseShipSkill() {

    var modID = "rat_core_time"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addPara("The base range of ballistic & energy weapons can no longer go beyond 500/600/700 units and weapon ranges are decreased by an additional 20%%. " +
                "In exchange the ship receives a 15%% increase in timeflow, 25%% more maneuverability and has its maximum speed increased by 10 units.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "ballistic", "energy", "500", "600", "700", "20%", "15%", "25%", "10")
        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {

        if (stats!!.entity is ShipAPI) {
            var ship = stats.entity as ShipAPI

            if (!ship.hasListenerOfClass(ChronosRangeModifier::class.java)) {
                ship.addListener(ChronosRangeModifier(ship))
            }
        }

        stats.timeMult.modifyMult(modID, 1.15f)
        stats.acceleration.modifyMult(modID, 1.25f)
        stats.deceleration.modifyMult(modID, 1.25f)
        stats.turnAcceleration.modifyMult(modID, 1.25f)
        stats.maxSpeed.modifyFlat(modID, 10f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {
        stats!!.timeMult.unmodify(modID)
        stats.acceleration.unmodify(modID)
        stats.deceleration.unmodify(modID)
        stats.turnAcceleration.unmodify(modID)
        stats.zeroFluxSpeedBoost.unmodify(modID)

        if (stats!!.entity is ShipAPI) {
            var ship = stats.entity as ShipAPI

            if (ship.hasListenerOfClass(ChronosRangeModifier::class.java)) {
                ship.removeListenerOfClass(ChronosRangeModifier::class.java)
            }
        }
    }
}

//Caps Base Range at 500
class ChronosRangeModifier(var ship: ShipAPI) : WeaponBaseRangeModifier, AdvanceableListener {

    override fun getWeaponBaseRangePercentMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        return 0f
    }

    override fun getWeaponBaseRangeMultMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        return 0.8f
    }

    override fun getWeaponBaseRangeFlatMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        if (weapon!!.type == WeaponAPI.WeaponType.MISSILE) return 0f

        var maxRange = when(weapon.size) {
            WeaponAPI.WeaponSize.SMALL -> 500
            WeaponAPI.WeaponSize.MEDIUM -> 600
            WeaponAPI.WeaponSize.LARGE -> 700
        }

        var range = weapon!!.spec.maxRange
        if (range > maxRange) {
            var difference = range - maxRange
            return -difference
        }

        return 0f
    }

    override fun advance(amount: Float) {
        var mod = 1.15f
        var modID = ship.id + "abyssal_adaptability"

        ship.mutableStats!!.timeMult.modifyMult(modID, mod);
        if (ship == Global.getCombatEngine().playerShip) {
            Global.getCombatEngine().timeMult.modifyMult(modID + ship.id, 1 / mod)
        }
        else {
            Global.getCombatEngine().timeMult.unmodify(modID + ship.id)
        }
    }
}