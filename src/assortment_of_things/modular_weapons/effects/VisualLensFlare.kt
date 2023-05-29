package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicLensFlare


class VisualLensFlare : ModularWeaponEffect() {
    override fun getName(): String {
        return "Lens Flare"
    }

    override fun getCost(): Int {
        return 0
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Creates a small lens-flare effect on projectile impact.", 0f)
    }

    override fun getResourceCost(data: SectorWeaponData) {
       
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Visual
    }


    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)

        MagicLensFlare.createSharpFlare(engine, projectile!!.weapon.ship, projectile.location, 5f, 200f, MathUtils.getRandomNumberInRange(0f, 360f), projectile.projectileSpec.fringeColor, projectile.projectileSpec.coreColor)
    }

}