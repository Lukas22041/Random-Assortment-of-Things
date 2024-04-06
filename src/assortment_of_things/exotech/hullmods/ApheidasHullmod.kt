package assortment_of_things.exotech.hullmods

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ApheidasHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.empDamageTakenMult.modifyMult(id, 0.75f)
        stats.weaponDamageTakenMult.modifyMult(id, 0.75f)

        stats.energyWeaponRangeBonus.modifyPercent(id, 20f)
        //stats.missileWeaponRangeBonus.modifyPercent(id, 33f)

        stats.weaponTurnRateBonus.modifyMult(id, 2f)


        stats.autofireAimAccuracy.modifyFlat(id, 0.5f);
        stats.recoilPerShotMult.modifyMult(id, 0.5f)
        stats.maxRecoilMult.modifyMult(id, 0.5f)

        //stats.maxSpeed.modifyMult(id, 0f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

       /* ship!!.velocity.set(Vector2f(ship!!.velocity.x * 0.99f, ship.velocity.y * 0.99f))
        ship!!.giveCommand(ShipCommand.TURN_RIGHT, null, 0);*/


        ship!!.blockCommandForOneFrame(ShipCommand.VENT_FLUX)
        ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_VENT)

       /* ship.blockCommandForOneFrame(ShipCommand.ACCELERATE)
        ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
        ship.blockCommandForOneFrame(ShipCommand.DECELERATE)
        ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
        ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)*/

        var contr = ship!!.engineController
        var engines = contr.shipEngines
        var botLeftEngine = engines.getOrNull(1)
        var botRightEngine = engines.getOrNull(0)
        var topRightEngine = engines.getOrNull(2)
        var topLeftEngine = engines.getOrNull(3)

        for (engine in engines) {
            contr.setFlameLevel(engine.engineSlot, 0.7f)
            contr.extendWidthFraction.shift(this, 0f, 0f, 0f, 0f)
        }

        if (contr.isAccelerating) {
            if (botLeftEngine!= null) contr.setFlameLevel(botLeftEngine.engineSlot, 1f)
            if (botRightEngine != null) contr.setFlameLevel(botRightEngine.engineSlot, 1f)
        }
        else if (contr.isAcceleratingBackwards) {
            if (topLeftEngine != null) contr.setFlameLevel(topLeftEngine.engineSlot, 0.95f)
            if (topRightEngine != null) contr.setFlameLevel(topRightEngine.engineSlot, 0.95f)
        }
        else if (contr.isStrafingLeft && !contr.isStrafingRight) {
            if (topRightEngine != null) contr.setFlameLevel(topRightEngine.engineSlot, 0.95f)
            if (botRightEngine != null) contr.setFlameLevel(botRightEngine.engineSlot, 0.95f)
        }
        else if (contr.isStrafingRight && !contr.isStrafingLeft) {
            if (topLeftEngine != null) contr.setFlameLevel(topLeftEngine.engineSlot, 0.95f)
            if (botLeftEngine!= null) contr.setFlameLevel(botLeftEngine.engineSlot, 0.95f)
        }

    }


    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("The apheidas-class platform does not operate like a normal ship. " +
                "It is continuously connected to its carrier, the \"Leanira\", its phase system allowing it to warp in and out of its vast hangar at will, where it can have mid-combat repairs made to its hull. Its armor can only be repaired out of combat.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "apheidas-class", "Leanira", "warp")

        tooltip.addSpacer(10f)

        tooltip.addPara("Build with this unique nature in mind, the platform comes with limited propulsion. " +
                "In exchange it is equipped with a unique targeting system that extends the range of energy weapons by 20%% while also having much improved weapon targeting and turn rate.\n\n" +
                "Missile weapons restore 20%% (or atleast 1) of their missiles per deployment. ", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "propulsion", "20%", "weapon targeting and turn rate", "20%", "1")

        tooltip.addSpacer(10f)

        tooltip.addPara("Due to having a limited amount of engine components exposed to the outer hull, the platform is able to have more consistent plating covering the ship, increasing the EMP resistance by 25%%.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "25%")
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }
}