package assortment_of_things.abyss.boss

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import org.lwjgl.util.vector.Vector2f
import java.util.*

class GenesisBossScript(var ship: ShipAPI) : CombatLayeredRenderingPlugin, HullDamageAboutToBeTakenListener {

    var phase = Phases.P1
    enum class Phases {
        P1, P2, P3
    }






    override fun init(entity: CombatEntityAPI?) {
        ship.addListener(this)
    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS, CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

    }


    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {

        if (phase == Phases.P1 || phase == Phases.P2) {

            if (ship!!.hitpoints - damageAmount <= 0) {

                if (phase == Phases.P1) {
                    phase = Phases.P2
                    ship.hitpoints = 1f
                }

                return true
            }

        }


        return false

    }
}