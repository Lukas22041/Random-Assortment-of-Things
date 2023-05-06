package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f

class BlasterBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Blaster"
    }

    override fun getCapacity(): Float {
        return 150f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.MEDIUM
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.op.baseValue = 14f

        stats.range.baseValue = 800f

        stats.damagePerShot.baseValue = 120f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 145f

        stats.projectileLength.baseValue = 30f
        stats.projectileWidth.baseValue = 25f
        stats.projectileSpeed.baseValue = 800f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 0.4f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0.1f

        stats.minSpread.baseValue = 1f
        stats.maxSpread.baseValue = 4f
        stats.spreadBuildup.baseValue = 1f
        stats.spreadDecay.baseValue = 4f

        stats.turnrate.baseValue = 20f

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The blaster has average stats, it doesnt particulary stand out, but can easily be formed in to a design of its own.", 0f)
    }



    override fun getHardpointSprite(): String {
        return "graphics/weapons/body_blaster/rat_modular_h00.png"
    }

    override fun getTurretSprite(): String {
        return "graphics/weapons/body_blaster/rat_modular_t00.png"
    }



    override fun getHardpointGlowSprite(): String {
        return "graphics/weapons/body_blaster/rat_modular_hardpoint_glow.png"
    }

    override fun getTurretGlowSprite(): String {
        return "graphics/weapons/body_blaster/rat_modular_turret_glow.png"
    }


    override fun hardpointOffset(): Vector2f {
        return Vector2f(15f, 0f)
    }

    override fun getTurretOffset(): Vector2f {
        return Vector2f(10f, 0f)
    }

}