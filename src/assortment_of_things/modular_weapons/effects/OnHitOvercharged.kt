package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class OnHitOvercharged : ModularWeaponEffect() {
    override fun getName(): String {
        return "Overcharged"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Adds a 30% chance for the projectile to spawn an EMP Arc on hit.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Onhit
    }


    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)


        if (ModularWeaponLoader.getData(projectile!!.weapon.id).rngCheck(0.30f, 0))
        {
            val emp = projectile!!.empAmount
            val dam = projectile.damageAmount
            var color = projectile!!.projectileSpec.fringeColor.darker().darker()

            engine!!.spawnEmpArc(projectile!!.source, point, target, target, DamageType.ENERGY, dam, emp,  // emp
                100000f,  // max range
                "tachyon_lance_emp_impact", 20f,  // thickness
                color, Color(255, 255, 255, 255))
        }

    }



}