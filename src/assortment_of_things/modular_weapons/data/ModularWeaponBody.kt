package assortment_of_things.modular_weapons.data

import assortment_of_things.modular_weapons.data.SectorWeaponData
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f

abstract class ModularWeaponBody {

    abstract fun getName() : String
    abstract fun getSize(): WeaponSize

    abstract fun getCapacity() : Float

    abstract fun addStats(stats: SectorWeaponData)

    abstract fun addTooltip(tooltip: TooltipMakerAPI)

    abstract fun addCost(data: SectorWeaponData)

    abstract fun getHardpointSprite(): String
    abstract fun getTurretSprite(): String

    abstract fun getHardpointGlowSprite() : String
    abstract fun getTurretGlowSprite(): String

    abstract fun hardpointOffset(): Vector2f
    abstract fun getTurretOffset(): Vector2f

    abstract fun getFireTwoSound() : String


}