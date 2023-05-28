package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class MarksmanBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Marksman"
    }

    override fun getCapacity(): Float {
        return 150f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.LARGE
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.op.baseValue = 24f

        stats.range.baseValue = 1100f

        stats.damagePerShot.baseValue = 700f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 1200f

        stats.projectileLength.baseValue = 120f
        stats.projectileWidth.baseValue = 9.5f
        stats.projectileSpeed.baseValue = 1200f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 1f
        stats.chargeDown.baseValue = 1f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0.1f

        stats.minSpread.baseValue = 0f
        stats.maxSpread.baseValue = 0f
        stats.spreadBuildup.baseValue = 1f
        stats.spreadDecay.baseValue = 4f

        stats.turnrate.baseValue = 3f

        stats.fadeTime = 0.3f
    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        var label = tooltip.addPara("The marksman fullfills a similar role to that of a \"Gauss Cannon\". It manages to pick at targets from a safe distance, at the cost of poor flux efficency.",
            0f, Misc.getTextColor(), Misc.getHighlightColor())

        label.setHighlight("marksman", "Gauss Cannon", "safe distance", "poor")
    }

    override fun addCost(data: SectorWeaponData) {
        data.addCraftingCost(RATItems.SALVAGED_WEAPON_COMPONENTS, 50f, this)

    }


    override fun getHardpointSprite(): String {
        return "graphics/weapons/body_marksman/rat_modular_h00.png"
    }

    override fun getTurretSprite(): String {
        return "graphics/weapons/body_marksman/rat_modular_t00.png"
    }



    override fun getHardpointGlowSprite(): String {
        return "graphics/weapons/body_marksman/rat_modular_hardpoint_glow.png"
    }

    override fun getTurretGlowSprite(): String {
        return "graphics/weapons/body_marksman/rat_modular_turret_glow.png"
    }


    override fun hardpointOffset(): Vector2f {
        return Vector2f(35f, 0f)
    }

    override fun getTurretOffset(): Vector2f {
        return Vector2f(25f, 0f)
    }

    override fun getFireTwoSound(): String {
        return "gauss_cannon_fire"
    }

}