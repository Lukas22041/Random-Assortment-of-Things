package assortment_of_things.modular_weapons.data

import assortment_of_things.modular_weapons.bodies.BlasterBody
import assortment_of_things.modular_weapons.bodies.DefenderBody
import assortment_of_things.modular_weapons.bodies.MarksmanBody
import assortment_of_things.modular_weapons.bodies.PulserBody
import assortment_of_things.modular_weapons.effects.*
import lunalib.lunaDelegates.LunaMemory

object ModularRepo {

    var bodies = listOf(DefenderBody(), BlasterBody(), PulserBody(), MarksmanBody())

    var modifiers = listOf(OnHitExplosiveCharge(), VisualTrail(), PassiveGuidance(), OnHitOvercharged(),  StatDampener(),
        StatAmplifier(), StatHeavyMunition(), StatEscapeVelocity(), StatDoubleBarrel(), StatAutoloader(), RiftEmitter(),
        StatImprovedCoils(), StatEfficientGyro(), PassiveClover(), OnHitPayload(),
        OnHitLifesteal(), OnHitBreach(), VisualLensFlare())


    private var unlockedModifiers: MutableList<String>? by LunaMemory("rat_unlocked_modifiers",
        mutableListOf("Overcharged", "Explosive Charges", "Heavy Munition", "Autoloader", "Trail", "Lens Flare"))

    fun getUnlockedModifier() : List<ModularWeaponEffect> {
        return modifiers.filter { unlockedModifiers!!.contains(it.getName()) }
    }

    fun unlockRandom() : ModularWeaponEffect?
    {
        if (unlockedModifiers!!.size == modifiers.size) {
            return null
        }

        var available = modifiers.filter { !unlockedModifiers!!.contains(it.getName()) }
        if (available.isEmpty()) return null
        var pick = available.random()

        unlockedModifiers!!.add(pick.getName())

        return pick
    }
}