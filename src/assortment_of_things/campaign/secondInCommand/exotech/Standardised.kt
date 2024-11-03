package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin

class Standardised : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all ships in the fleet"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("All energy mount weapons acquire a minimum base range of 600 (before stat modifiers)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("   - Stat modifiers can still decrease the weapons range below this range", 0f, Misc.getTextColor(), Misc.getHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {



    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

        if (!ship.hasListenerOfClass(StandardisedRangeModifier::class.java)) {
            ship.addListener(StandardisedRangeModifier(ship))
        }
        
    }

    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}

//Caps Base Range at 500
class StandardisedRangeModifier(var ship: ShipAPI) : WeaponBaseRangeModifier {

    var minRange = 600f

    override fun getWeaponBaseRangePercentMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        return 0f
    }

    override fun getWeaponBaseRangeMultMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        return 1f
    }

    override fun getWeaponBaseRangeFlatMod(ship: ShipAPI?, weapon: WeaponAPI?): Float {
        if (weapon!!.type != WeaponAPI.WeaponType.ENERGY) return 0f

        var range = weapon.spec.maxRange
        if (range < minRange) {
            var difference = minRange - range
            return difference
        }

        return 0f
    }
}