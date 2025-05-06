package assortment_of_things.abyss.shipsystem.threat

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.PlasmaJetsStats
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import java.awt.Color
import java.util.*

class SaintShipSystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.15f, 0.15f)

    var addedRenderer = false



    var activated = false

    override fun apply(stats: MutableShipStatsAPI?, id: String,  state: ShipSystemStatsScript.State?,  effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity
        if (ship !is ShipAPI) return
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        var system = ship.system

        if (!addedRenderer) {
            addedRenderer = true
            Global.getCombatEngine().addLayeredRenderingPlugin(SaintGlowRenderer(ship))
        }

        var color = Color(196, 20, 35, 255)

        if (system.isActive) {


        }

    }


    class SaintGlowRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ships/rat_saint_glow.png")

        override fun getRenderRadius(): Float {
            return 100000f
        }

        var layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return layers
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

            sprite.angle = ship.facing -90f
            sprite.alphaMult = ship.system.effectLevel
            sprite.setNormalBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)

            sprite.alphaMult = ship.system.effectLevel * 0.5f
            sprite.setAdditiveBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)
        }
    }
}