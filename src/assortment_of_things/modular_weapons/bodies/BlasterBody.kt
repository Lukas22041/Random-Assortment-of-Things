package assortment_of_things.modular_weapons.bodies

import assortment_of_things.modular_weapons.data.ModularWeaponBody
import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class BlasterBody() : ModularWeaponBody() {
    override fun getName(): String {
        return "Blaster"
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
        Global.getCurrentState()
        stats.op.baseValue = 14f

        stats.range.baseValue = 600f

        stats.damagePerShot.baseValue = 500f
        stats.empDamage.baseValue = 0f

        stats.energyPerShot.baseValue = 720f

        stats.projectileLength.baseValue = 25f
        stats.projectileWidth.baseValue = 30f
        stats.projectileSpeed.baseValue = 1000f

        stats.maxAmmo.changeBase(Int.MAX_VALUE)
        stats.ammoPerSecond.baseValue = 1f
        stats.reloadSize.baseValue = 1f

        stats.chargeUp.baseValue = 0.0f
        stats.chargeDown.baseValue = 1f
        stats.burstSize.changeBase(1)
        stats.burstDelay.baseValue = 0.0f

        stats.minSpread.baseValue = 0f
        stats.maxSpread.baseValue = 0f
        stats.spreadBuildup.baseValue = 0f
        stats.spreadDecay.baseValue = 0f

        stats.turnrate.baseValue = 15f

        stats.fadeTime = 0.3f
    }

    override fun addTooltip(tooltip: TooltipMakerAPI) {
       var label = tooltip.addPara("The blaster has has stats matching that of the \"Heavy Blaster\" Weapon. It deals large damage in turn for its low fire rate.",
           0f, Misc.getTextColor(), Misc.getHighlightColor())

        label.setHighlight("blaster", "Heavy Blaster")
    }

    override fun addCost(data: SectorWeaponData) {
        data.addCraftingCost(RATItems.SALVAGED_WEAPON_COMPONENTS, 20f, this)
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

    override fun getFireTwoSound(): String {
        return "heavy_blaster_fire"
    }

}