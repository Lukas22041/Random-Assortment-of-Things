package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f

class MarksmanBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Marksman"
    }

    override fun getCapacity(): Float {
        return 160f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.LARGE
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.op.baseValue = 20f

        stats.range.baseValue = 1000f

        stats.damagePerShot.baseValue = 350f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 410f

        stats.projectileLength.baseValue = 35f
        stats.projectileWidth.baseValue = 35f
        stats.projectileSpeed.baseValue = 1000f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 1f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0.1f

        stats.minSpread.baseValue = 0f
        stats.maxSpread.baseValue = 0f
        stats.spreadBuildup.baseValue = 1f
        stats.spreadDecay.baseValue = 4f

        stats.turnrate.baseValue = 15f

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The Marksman is build for long distance fire at a low fire rate.", 0f)
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

}