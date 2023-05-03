package assortment_of_things.modular_weapons.data

import com.fs.starfarer.api.combat.DamageType
import java.awt.Color

enum class AvailableDamageTypes(var displayName: String, var damageType: DamageType, var cost: Float, var color: Color) {
    ENERGY("Energy", DamageType.ENERGY, 10f, Color(0, 150, 200)),
    HIGH_EXPLOSIVE("High Explosive", DamageType.HIGH_EXPLOSIVE, 20f, Color(255, 100, 0)),
    KINETIC("Kinetic", DamageType.KINETIC, 20f, Color(255, 150, 0)),
}
