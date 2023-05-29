package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


class StatHeavyMunition : ModularWeaponEffect() {
    override fun getName(): String {
        return "Heavy Munition"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        var para = tooltip.addPara("Increases the weapons damage and emp damage by 25%%. Also increases the projectiles width and length." +
                " In turn it decreases the weapons flux efficiency and firerate by 10%%", 0f)

        para.setHighlight("damage", "25%%", "width and length", "flux efficiency", "firerate", "10%")

        var h = Misc.getHighlightColor()
        var n = Misc.getNegativeHighlightColor()
        para.setHighlightColors(h, h, h, n, n, n)
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.SUPPLIES, 5f, this)
        data.addCraftingCost(RATItems.SALVAGED_WEAPON_COMPONENTS, 5f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.damagePerShot.modifyMult(getName(), 1.25f)
        stats.empDamage.modifyMult(getName(), 1.25f)

        stats.projectileWidth.modifyMult(getName(), 1.5f)
        stats.projectileLength.modifyMult(getName(), 1.5f)

        stats.energyPerShot.modifyMult(getName(), 1.1f)

        stats.burstDelay.modifyMult(getName(), 1.10f)
        stats.chargeDown.modifyMult(getName(), 1.10f)
        stats.chargeUp.modifyMult(getName(), 1.10f)
    }
}