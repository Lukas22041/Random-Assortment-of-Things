package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.ui.TooltipMakerAPI


class StatHighValueMunition : ModularWeaponEffect() {
    override fun getName(): String {
        return "High Value Munition"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The weapon sees increased stats and now uses ammo, but it can not regenerate any. Negates effects that increase ammo recharge.", 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("- Adds 30 charges to the ammo capacity.", 0f)
        tooltip.addPara("- Doubles Ammo Capacity.", 0f)
        tooltip.addPara("- Increases Damage by a value of 25.", 0f)
        tooltip.addPara("- Increases EMP Damage by a value of 10.", 0f)
        tooltip.addPara("- Increases Damage by 50%.", 0f)
        tooltip.addPara("- Increases Projectile Size by 50%.", 0f)
        tooltip.addPara("- Increases Projectile Range by 100.", 0f)

    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.maxAmmo.changeBase(0)

        stats.ammoPerSecond.addMult(getName(), 0f)
        stats.maxAmmo.addFlat(getName(), 30)
        stats.maxAmmo.addMult(getName(), 2f)

        stats.damagePerShot.addFlat(getName(), 25f)
        stats.damagePerShot.addMult(getName(), 1.5f)

        stats.empDamage.addFlat(getName(), 10f)
        stats.empDamage.addMult(getName(), 1.5f)

        stats.projectileWidth.addMult(getName(), 1.5f)
        stats.projectileLength.addMult(getName(), 1.5f)

        stats.range.addFlat(getName(), 100f)

    }
}