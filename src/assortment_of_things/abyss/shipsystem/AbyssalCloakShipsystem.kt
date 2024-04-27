package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.plugins.MagicTrailPlugin
import java.awt.Color

class AbyssalCloakShipsystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.2f, 0.2f)
    var afterimageFighterInterval = IntervalUtil(0.05f, 0.05f)

    var ship: ShipAPI? = null

    override fun apply(stats: MutableShipStatsAPI?,  id: String?,state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI

        if (state == ShipSystemStatsScript.State.ACTIVE) {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.enableBlink()
        }
        else {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.disableBlink()
        }

        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id


        var color = AbyssalsAdaptabilityHullmod.getColorForCore(ship!!).setAlpha(75)
        var secondaryColor = AbyssalsAdaptabilityHullmod.getSecondaryColorForCore(ship).setAlpha(75)

        var jitterColor = color.setAlpha(55)
        var jitterUnderColor = color.setAlpha(150)
        var jitterLevel = effectLevel
        var jitterRangeBonus = 0f
        var mult = 2f

        ship!!.setJitter(this, jitterColor, jitterLevel, 3, 0f, 0 + jitterRangeBonus)
        ship!!.setJitterUnder(this, jitterUnderColor, jitterLevel, 25, 0f, 7f + jitterRangeBonus)

        val shipTimeMult = 1f + (mult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        stats.energyWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))
        stats.ballisticWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))

        stats.shieldDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
        stats.hullDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
        stats.armorDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))


        for (wing in ship.allWings) {
            for (fighter in wing.wingMembers) {
                var fighterStats = fighter.mutableStats

                fighterStats.timeMult.modifyMult(id, shipTimeMult)

                fighterStats.energyWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))
                fighterStats.ballisticWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))

                fighterStats.shieldDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
                fighterStats.hullDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
                fighterStats.armorDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))

                fighter.engineController.extendFlame(this, 0.25f * effectLevel, 0.25f * effectLevel, 0.25f * effectLevel)

                fighter!!.setJitter(this, jitterColor, jitterLevel, 3, 0f, 0f)
                fighter!!.setJitterUnder(this, jitterUnderColor.setAlpha(200), jitterLevel, 10, 0f, 7f)

                if (fighter.baseOrModSpec().hullId == "rat_ceto") {
                    fighter.engineController.fadeToOtherColor("rat_abyssals_enginefade", color.setAlpha(255), color, 1f, 1f)
                }
            }
        }

        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, color, secondaryColor.setAlpha(75), 2f, 2f, Vector2f().plus(ship!!.location))
            }
            afterimageFighterInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageFighterInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                for (wing in ship.allWings) {
                    for (fighter in wing.wingMembers) {
                        AfterImageRenderer.addAfterimage(fighter, color, secondaryColor.setAlpha(75), 1f, 1f, Vector2f().plus(fighter.location))
                    }
                }
            }
        }

        ship!!.engineController.extendFlame(this, 0.25f * effectLevel, 0.25f * effectLevel, 0.25f * effectLevel)

    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)
        stats.fluxDissipation.unmodify(id)
        stats.hardFluxDissipationFraction.unmodify(id)

        AbyssalsAdaptabilityHullmod.getRenderer(ship)?.disableBlink()
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State, effectLevel: Float): StatusData? {
        return null
    }


}