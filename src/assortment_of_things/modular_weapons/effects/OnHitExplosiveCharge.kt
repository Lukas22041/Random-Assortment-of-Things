package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f


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

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.SUPPLIES, 100f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Onhit
    }

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)

    }

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)

        if (ModularWeaponLoader.getData(projectile!!.weapon.id).rngCheck(0.25f, 0))
        {

            var color = projectile!!.projectileSpec.fringeColor.darker().darker().darker().darker().darker()
            var spec = DamagingExplosionSpec(2f, projectile.projectileSpec.width * 2, projectile.projectileSpec.width * 2.5f, projectile!!.damage.damage, 0f,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS, CollisionClass.HITS_SHIPS_AND_ASTEROIDS, 1f, 10f, 3f, 20,
                color, color)
            engine!!.spawnDamagingExplosion(spec, projectile.weapon!!.ship, projectile.location, true)
        }
    }


}