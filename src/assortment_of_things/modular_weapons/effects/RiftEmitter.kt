package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.effects.plugins.ModularRiftEmitterPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


class RiftEmitter : ModularWeaponEffect() {
    override fun getName(): String {
        return "Rift Emitter"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Spawns rifts along the projectiles path that shoot EMP Arcs towards nearby enemies. The amount spawned is based on random chance.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "rifts", "EMP Arcs")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.VOLATILES, 10f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Passive
    }


    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)


        val trail = ModularRiftEmitterPlugin(projectile!!, "rifttorpedo_loop", projectile!!.projectileSpec.fringeColor)
        Global.getCombatEngine().addPlugin(trail)


    }
}