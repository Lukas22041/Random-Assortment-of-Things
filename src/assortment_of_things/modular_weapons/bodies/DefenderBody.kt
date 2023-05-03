package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f

class DefenderBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Defender"
    }

    override fun getCapacity(): Float {
        return 80f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.SMALL
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.isPD = true

        stats.op.baseValue = 6f

        stats.range.baseValue = 300f

        stats.damagePerShot.baseValue = 20f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 15f

        stats.projectileLength.baseValue = 20f
        stats.projectileWidth.baseValue = 15f
        stats.projectileSpeed.baseValue = 1500f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 0.15f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0.05f

        stats.minSpread.baseValue = 0f
        stats.maxSpread.baseValue = 0f

        stats.spreadBuildup.baseValue = 0f
        stats.spreadDecay.baseValue = 10f

        stats.turnrate.baseValue = 40f

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The Defender is a small body that causes the AI to use the weapon as a Point-Defense weapon. It has a high fire rate, no spread but low damage and range.", 0f)
    }



    override fun getHardpointSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_t00.png"
    }

    override fun getTurretSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_h00.png"
    }



    override fun getHardpointGlowSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_hardpoint_glow.png"
    }

    override fun getTurretGlowSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_turret_glow.png"
    }


    override fun hardpointOffset(): Vector2f {
        return Vector2f(18f, 0f)
    }

    override fun getTurretOffset(): Vector2f {
        return Vector2f(10f, 0f)
    }

}