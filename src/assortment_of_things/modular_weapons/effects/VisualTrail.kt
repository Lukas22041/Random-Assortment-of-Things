package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.magiclib.plugins.MagicTrailPlugin


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

    override fun getResourceCost(data: SectorWeaponData) {

    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Visual
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