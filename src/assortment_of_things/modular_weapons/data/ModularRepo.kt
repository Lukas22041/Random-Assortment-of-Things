package assortment_of_things.modular_weapons.data

import assortment_of_things.modular_weapons.bodies.BlasterBody
import assortment_of_things.modular_weapons.bodies.DefenderBody
import assortment_of_things.modular_weapons.bodies.MarksmanBody
import assortment_of_things.modular_weapons.bodies.PulserBody
import assortment_of_things.modular_weapons.effects.*

object ModularRepo {

    var bodies = listOf(BlasterBody(), DefenderBody(), PulserBody(), MarksmanBody())

    var modifiers = listOf(OnHitExplosiveCharge(), VisualTrail(), PassiveGuidance(), OnHitOvercharged(),  StatDampener(),
        StatAmplifier(), StatHeavyMunition(), StatEscapeVelocity(), StatDoubleBarrel(), StatAutoloader(), RiftEmitter(),
        StatImprovedCoils(), StatEfficientGyro(), PassiveClover(), OnHitPayload(),
        OnHitLifesteal(), OnHitBreach(), VisualLensFlare())

}