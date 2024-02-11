package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.AbyssUtils
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

class SpatialDisturbanceSystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    override fun apply(stats: MutableShipStatsAPI?,  id: String?,state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI

        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id


        var color = AbyssUtils.GENESIS_COLOR.setAlpha(75)

        var jitterColor = color.setAlpha(55)
        var jitterUnderColor = color.setAlpha(150)
        var mult = 3f

        ship!!.setJitter(this, jitterColor, effectLevel, 3, 0f, 0f )
        ship!!.setJitterUnder(this, jitterUnderColor, effectLevel, 25, 0f, 10f)

        val shipTimeMult = 1f + (mult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        stats.maxSpeed.modifyFlat(id, 120f * effectLevel)
       // stats.maxSpeed.modifyMult(id, 1 + (1f * effectLevel))
        stats.acceleration.modifyFlat(id, 80f * effectLevel)
       // stats.acceleration.modifyMult(id, 1 + (2f * effectLevel))
        stats.deceleration.modifyFlat(id, 80f * effectLevel)
        //stats.deceleration.modifyMult(id, 1 + (2f * effectLevel))
        stats.maxTurnRate.modifyFlat(id, 30f * effectLevel)
        //stats.maxTurnRate.modifyMult(id, 1 + (2f * effectLevel))
        stats.turnAcceleration.modifyFlat(id, 30 * effectLevel)
        //stats.turnAcceleration.modifyMult(id, 1 + (2f * effectLevel))

        ship!!.engineController.extendFlame(this, 0.3f * effectLevel, 0.2f * effectLevel, 0.4f * effectLevel)

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
}