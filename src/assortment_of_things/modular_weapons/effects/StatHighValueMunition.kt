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
        tooltip.addPara("- Increases Damage by 25%.", 0f)
        tooltip.addPara("- Increases Projectile Size by 25%.", 0f)
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

        stats.ammoPerSecond.modifyMult(getName(), 0f)
        stats.maxAmmo.modifyFlat(getName(), 30)
        stats.maxAmmo.modifyMult(getName(), 2f)

        stats.damagePerShot.modifyFlat(getName(), 25f)
        stats.damagePerShot.modifyMult(getName(), 1.25f)

        stats.empDamage.modifyFlat(getName(), 10f)
        stats.empDamage.modifyMult(getName(), 1.25f)

        stats.projectileWidth.modifyMult(getName(), 1.25f)
        stats.projectileLength.modifyMult(getName(), 1.25f)

        stats.range.modifyFlat(getName(), 100f)

    }
}