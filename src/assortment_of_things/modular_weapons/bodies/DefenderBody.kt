package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
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

        stats.op.baseValue = 5f

        stats.range.baseValue = 250f

        stats.damagePerShot.baseValue = 25f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 1f

        stats.projectileLength.baseValue = 30f
        stats.projectileWidth.baseValue = 3.5f
        stats.projectileSpeed.baseValue = 800f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 0.05f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0f

        stats.minSpread.baseValue = 0f
        stats.maxSpread.baseValue = 15f

        stats.spreadBuildup.baseValue = 1f
        stats.spreadDecay.baseValue = 15f

        stats.turnrate.baseValue = 75f

        stats.fadeTime = 0.3f

        stats.muzzleDuration = 0.3f
        stats.muzzleParticles = 3

        stats.rngAttempts.changeBase(-2)

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        var label = tooltip.addPara("The defender is a small body that causes the weapon to behave as a point-defense weapon. " +
                "This body is very similar to a \"Vulcan\" type Weapon. It does however have a way lower chance to trigger rng effects than other bodies.",
            0f, Misc.getTextColor(), Misc.getHighlightColor())

        label.setHighlight("defender", "point-defense", "Vulcan", "lower chance to trigger effects")
    }



    override fun getHardpointSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_h00.png"
    }

    override fun getTurretSprite(): String {
        return "graphics/weapons/body_defender/rat_modular_t00.png"
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

    override fun getFireTwoSound(): String {
        return "vulcan_cannon_fire"
    }

}