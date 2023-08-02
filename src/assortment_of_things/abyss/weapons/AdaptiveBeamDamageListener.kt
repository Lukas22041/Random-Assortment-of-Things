package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lwjgl.util.vector.Vector2f
/*

class AdaptiveBeamDamageListener : DamageDealtModifier {
    override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?, point: Vector2f?,shieldHit: Boolean): String? {
        if (param !is BeamAPI) return null
        if (param.weapon.id != "rat_asterias" && param.weapon.id != "rat_asterina") return null

        var dps = damage!!.dpsDuration
        var modID = "RAT_AdaptiveDamageMod"

        if (dps <= 0) return null

        val dam = (damage.damage * damage.dpsDuration) * 0.33f
        var engine = Global.getCombatEngine()

        if (shieldHit) {
            engine!!.applyDamage(target, point, dam, DamageType.KINETIC, 0f, false, true, null)
        }
        else {
            engine!!.applyDamage(target, point, dam, DamageType.HIGH_EXPLOSIVE, 0f, false, true, null)
        }

        damage.modifier.modifyMult(modID, 0.67f)

        return modID
    }
}*/
