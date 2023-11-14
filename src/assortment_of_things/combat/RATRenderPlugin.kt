package assortment_of_things.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RATRenderPlugin : CombatLayeredRenderingPlugin {


    class RATRenderCall() {
        var done: Boolean = false
        var layer: CombatEngineLayers = CombatEngineLayers.ABOVE_SHIPS_LAYER
        var callLambda: RATRenderCall.() -> Unit = {}
    }

    companion object {

        fun addRenderCall(id: Any, layer: CombatEngineLayers, lambda: RATRenderCall.() -> Unit) {
            var plugin = getPlugin()

            var existing = plugin.calls.get(id)
            if (existing != null) return

            var call = RATRenderCall()
            call.layer = layer
            call.callLambda = lambda
            plugin.calls.put(id, call)
        }

        private fun getPlugin() : RATRenderPlugin{
            var plugin: RATRenderPlugin? = Global.getCombatEngine().customData.get("rat_render_plugin") as RATRenderPlugin?
            if (plugin == null) {
                plugin = RATRenderPlugin()
                Global.getCombatEngine().addLayeredRenderingPlugin(plugin)
                Global.getCombatEngine().customData.set("rat_render_plugin", plugin)
            }
            return plugin
        }
    }

    var calls = HashMap<Any, RATRenderCall>()

    override fun init(entity: CombatEntityAPI?) {

    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        var set = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        var layers = calls.values.map { it.layer }.toSet()
        set.addAll(layers)
        set.distinct()
        return set
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        for (call in HashMap(calls)) {
            if (call.value.done) {
                calls.remove(call)
                continue
            }

            if (call.value.layer != layer) continue

            call.value.callLambda(call.value)
        }
    }
}