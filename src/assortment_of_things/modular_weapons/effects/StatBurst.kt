package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatBurst : ModularWeaponEffect() {
    override fun getName(): String {
        return "Burst"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Causes the weapon to shoot multiple 4 at once, but it now has to recharge ammo. Also drasticly worsens the weapons accuracy. ", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.burstSize.addFlat(getName(), 3)
        stats.burstDelay.addMult(getName(), 0.2f)
       // stats.chargeDown.addMult(getName(), 1.5f)

        //stats.damagePerShot.addMult(getName(), 0.50f)
        //stats.empDamage.addMult(getName(), 0.50f)

        stats.maxAmmo.changeBase(0)
        stats.ammoPerSecond.addFlat(getName(),1f)
        stats.maxAmmo.addFlat(getName(), 30)

        stats.minSpread.addFlat(getName(), 10f)
        stats.maxSpread.addFlat(getName(), 15f)

        stats.range
    }

}