package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.combat.listeners.DamageListener
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class SarielLauncherListener(var ship: ShipAPI) : DamageDealtModifier {


    var requiredDamage = 5000
    var damageSoFar = 0f


    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?,  point: Vector2f?, shieldHit: Boolean): String? {

        if (target is ShipAPI && !target.isAlive) return null

        if (param is DamagingProjectileAPI) {
            if (param.weapon != null && param.weapon.id.contains("sariel_launcher"))   {
                return null
            }
        }


        if (param is MissileAPI) {
            if (param.weapon != null && param.weapon.id.contains("sariel_launcher")) {
                return null
            }
        }


        if (param is BeamAPI) {
            damageSoFar +=  damage!!.damage * damage.dpsDuration
        }
        else {
            damageSoFar +=  damage!!.damage
        }


        if (damageSoFar > requiredDamage) {
            damageSoFar = 0f

            for (weapon in ship.allWeapons) {
                if (weapon.id.contains("sariel_launcher")) {
                    var extra = weapon.ammo + 8
                    extra = MathUtils.clamp(extra, 0, weapon.maxAmmo)
                    weapon.ammo = extra
                }
            }
        }

        return null
    }
}