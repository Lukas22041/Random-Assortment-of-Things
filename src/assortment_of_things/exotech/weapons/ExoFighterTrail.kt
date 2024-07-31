package assortment_of_things.exotech.weapons

import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.plugins.MagicTrailPlugin
import java.awt.Color

class ExoFighterTrail : EveryFrameWeaponEffectPlugin {

    var interval = IntervalUtil(0.01f, 0.01f)

    var sprite1 = Global.getSettings().getSprite("fx", "base_trail_smooth")
    var id1 = MagicTrailPlugin.getUniqueID()

    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {
        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        var ship = weapon!!.ship
        if (ship.system.isActive) return

        var color1 = Color(252,143,0)
        var color2 = Color(130,4,189)



        //Drift
        var projBodyVel = Vector2f(ship.velocity)
        projBodyVel = VectorUtils.rotate(projBodyVel, -ship.facing)
        val projLateralBodyVel = Vector2f(0.0f, projBodyVel.getY())
        var sidewayVel = Vector2f(projLateralBodyVel)
        sidewayVel = VectorUtils.rotate(sidewayVel, ship.facing).scale(0.9f) as Vector2f


        MagicTrailPlugin.addTrailMemberAdvanced(
            ship, id1, sprite1, weapon!!.location, 25f, 10f, weapon!!.currAngle, 0f, 0f,
            15f, 3f, color1, color2, 0.8f, 0f, 0.1f, 2f, true, -128f, -256f, sidewayVel, mutableMapOf(), CombatEngineLayers.CONTRAILS_LAYER, 0f)

    }
}