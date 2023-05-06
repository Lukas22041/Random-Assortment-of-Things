package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f

class PulserBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Pulser"
    }

    override fun getCapacity(): Float {
        return 140f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.MEDIUM
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.op.baseValue = 14f

        stats.range.baseValue = 700f

        stats.damagePerShot.baseValue = 40f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 25f

        stats.projectileLength.baseValue = 20f
        stats.projectileWidth.baseValue = 15f
        stats.projectileSpeed.baseValue = 800f

        stats.maxAmmo.changeBase(40)
        stats.ammoPerSecond.baseValue = 2f
        stats.reloadSize.baseValue = 2f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 0.4f
        stats.burstSize.changeBase(4)
        stats.burstDelay.baseValue = 0.05f

        stats.minSpread.baseValue = 5f
        stats.maxSpread.baseValue = 15f
        stats.spreadBuildup.baseValue = 2f
        stats.spreadDecay.baseValue = 6f

        stats.turnrate.baseValue = 20f

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The Pulser is able to shoot 4 projectiles at once, but is limited by ammo that it has to recharge.", 0f)
    }



    override fun getHardpointSprite(): String {
        return "graphics/weapons/body_pulser/rat_modular_h00.png"
    }

    override fun getTurretSprite(): String {
        return "graphics/weapons/body_pulser/rat_modular_t00.png"
    }



    override fun getHardpointGlowSprite(): String {
        return "graphics/weapons/body_pulser/rat_modular_hardpoint_glow.png"
    }

    override fun getTurretGlowSprite(): String {
        return "graphics/weapons/body_pulser/rat_modular_turret_glow.png"
    }


    override fun hardpointOffset(): Vector2f {
        return Vector2f(33f, 0f)
    }

    override fun getTurretOffset(): Vector2f {
        return Vector2f(22f, 0f)
    }

}