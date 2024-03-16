package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.AbyssUtils
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

class PrimordialCloakShipsystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.1f, 0.1f)
    var ship: ShipAPI? = null

    override fun apply(stats: MutableShipStatsAPI?,  id: String?,state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI

        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id


        var color = AbyssUtils.GENESIS_COLOR.setAlpha(75)
        var secondaryColor = AbyssUtils.GENESIS_COLOR.setAlpha(75)

        var jitterColor = color.setAlpha(55)
        var jitterUnderColor = color.setAlpha(150)
        var jitterLevel = effectLevel
        var jitterRangeBonus = 0f
        var mult = 1.5f



        val shipTimeMult = 1f + (mult - 1f) * effectLevel
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        var segments = mutableListOf(ship)
        segments.addAll(ship.childModulesCopy)

        for (segment in segments) {

            segment!!.setJitter(this, jitterColor, jitterLevel, 3, 0f, 0 + jitterRangeBonus)
            segment!!.setJitterUnder(this, jitterUnderColor, jitterLevel, 25, 0f, 7f + jitterRangeBonus)

            var segmentStats = segment.mutableStats

            segmentStats.timeMult.modifyMult(id, shipTimeMult)

            segmentStats.energyWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))
            segmentStats.ballisticWeaponDamageMult.modifyMult(id, 1 + (0.1f * effectLevel))

            segmentStats.shieldDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
            segmentStats.hullDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))
            segmentStats.armorDamageTakenMult.modifyMult(id, 1 - (0.2f * effectLevel))

            segment!!.engineController.extendFlame(this, 0.25f * effectLevel, 0.25f * effectLevel, 0.25f * effectLevel)

        }





        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)

            for (segment in segments) {
                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    AfterImageRenderer.addAfterimage(segment!!, color, secondaryColor.setAlpha(75), 1f, 2f, Vector2f().plus(segment!!.location))
                }
            }


        }


    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)
        stats.fluxDissipation.unmodify(id)
        stats.hardFluxDissipationFraction.unmodify(id)

    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State, effectLevel: Float): StatusData? {
        return null
    }


}