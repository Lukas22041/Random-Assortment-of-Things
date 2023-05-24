package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class PulserBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Pulser"
    }

    override fun getCapacity(): Float {
        return 130f
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        return WeaponAPI.WeaponSize.MEDIUM
    }

    override fun addStats(stats: SectorWeaponData) {
        stats.maxCapacity = getCapacity()
        stats.weaponSize = getSize()

        stats.op.baseValue = 13f

        stats.range.baseValue = 500f

        stats.damagePerShot.baseValue = 100f
        stats.empDamage.baseValue = 300f

        stats.energyPerShot.baseValue = 80f

        stats.projectileLength.baseValue = 75f
        stats.projectileWidth.baseValue = 20f
        stats.projectileSpeed.baseValue = 1000f

        stats.maxAmmo.changeBase(20)
        stats.ammoPerSecond.baseValue = 2f
        stats.reloadSize.baseValue = 2f

        stats.chargeUp.baseValue =  0.05f
        stats.chargeDown.baseValue = 0.05f
        stats.burstSize.changeBase(3)
        stats.burstDelay.baseValue = 0.1f

        stats.minSpread.baseValue = 3f
        stats.maxSpread.baseValue = 20f
        stats.spreadBuildup.baseValue = 1f
        stats.spreadDecay.baseValue = 4f

        stats.turnrate.baseValue = 20f

        stats.fadeTime = 0.25f

    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
        var label = tooltip.addPara("The pulser is similar to the \"Ion Pulser\" weapon. It allows to quickly unleash a burst of shots that deal both normal and emp damage, but is limited by its recharging ammunition.",
            0f, Misc.getTextColor(), Misc.getHighlightColor())

        label.setHighlight("pulser", "Ion Pulser", "burst of shots", "recharging ammunition")
    }

    override fun addCost(data: SectorWeaponData) {

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

    override fun getFireTwoSound(): String {
        return "ion_pulser_fire"
    }

}