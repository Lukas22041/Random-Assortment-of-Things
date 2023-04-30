package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatBurst : ModularWeaponEffect() {
    override fun getName(): String {
        return "Burst"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The weapon can shoot 3 more shots at once, at the cost of now requiring its shots to recharge over time. Additionaly, the Weapon looses 20% of its damage, the projectiles get slightly smaller" +
                " and the weapons accuracy becomes much worse. ", 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("- Adds 30 charges to the ammo capacity.", 0f)

    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.burstSize.modifyFlat(getName(), 3)
        stats.burstDelay.modifyMult(getName(), 0.2f)
       // stats.chargeDown.modifyMult(getName(), 1.5f)

        stats.damagePerShot.modifyMult(getName(), 0.80f)
        stats.empDamage.modifyMult(getName(), 0.80f)

        stats.projectileWidth.modifyMult(getName(), 0.8f)
        stats.projectileLength.modifyMult(getName(), 0.8f)

        stats.maxAmmo.changeBase(0)
        stats.ammoPerSecond.modifyFlat(getName(),1f)
        stats.maxAmmo.modifyFlat(getName(), 30)

        stats.minSpread.modifyFlat(getName(), 10f)
        stats.maxSpread.modifyFlat(getName(), 15f)

    }

}