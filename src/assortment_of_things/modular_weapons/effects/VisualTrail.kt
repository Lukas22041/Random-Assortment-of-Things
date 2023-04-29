package assortment_of_things.modular_weapons.effects

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import data.scripts.plugins.MagicTrailPlugin


class VisualTrail : ModularWeaponEffect() {
    override fun getName(): String {
        return "Trail"
    }

    override fun getCost(): Int {
        return 0
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Causes the projectile to leave a trail behind its path.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Visual
    }


    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)

        var data = engine!!.customData.get("rat_modular_trail_projectiles") as MutableList<DamagingProjectileAPI>?
        if (data == null)
        {
            data = ArrayList()
        }
        data.add(projectile!!)
        projectile.setCustomData("rat_trail_id", MagicTrailPlugin.getUniqueID())
        engine!!.customData.set("rat_modular_trail_projectiles", data)
    }

}