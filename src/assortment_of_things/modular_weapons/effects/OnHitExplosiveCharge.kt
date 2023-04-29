package assortment_of_things.modular_weapons.effects

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f
import kotlin.random.Random


class OnHitExplosiveCharge : ModularWeaponEffect() {
    override fun getName(): String {
        return "Explosive Charges"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Adds a 25% chance for the projectile to explode on hit.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Onhit
    }


    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)

        if (Random.nextFloat() > 0.75f)
        {

            var color = projectile!!.projectileSpec.fringeColor.darker().darker().darker().darker()
            var spec = DamagingExplosionSpec(3f, 30f, 50f, projectile!!.damage.damage, 0f,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS, CollisionClass.HITS_SHIPS_AND_ASTEROIDS, 1f, 10f, 1f, 20,
                color, color)
            engine!!.spawnDamagingExplosion(spec, projectile.weapon!!.ship, projectile.location, true)
        }

    }



}