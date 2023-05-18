package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

enum class ModularEffectModifier(var displayName: String, var color: Color) {
    Onhit("On Hit", Color(230, 50, 100)),
    Passive("Passive", Color(0, 200, 150)),
    Stat("Stat", Color(255, 200, 0)),
    Multiple("Multiple", Color(0, 150, 255)),
    Negative("Negative", Color(255, 0, 0)),
    Visual("Visual", Color(0, 150, 255))
}

abstract class ModularWeaponEffect {

    abstract fun getName(): String

    abstract fun getCost(): Int

    abstract fun getIcon(): String

    abstract fun getTooltip(tooltip: TooltipMakerAPI)

    abstract fun getResourceCost(): MutableMap<String, Float>

    abstract fun getType(): ModularEffectModifier

    open fun getOrder() = 100f

    open fun addStats(stats: SectorWeaponData) {}

    open fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {}

    open fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {}

    open fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {}
}

class ModularTooltipCreator(var tooltipLambda: (TooltipMakerAPI) -> Unit = { } ) : TooltipCreator {

    override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
        return false
    }

    override fun getTooltipWidth(tooltipParam: Any?): Float {
        return 600f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
        /*var tooltipCreator = ModularTooltipCreator() {
            it.addPara("Test", 0f)
        }*/

        tooltipLambda(tooltip!!)
    }
}