package assortment_of_things.exotech.weapons.grapple

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class GrapplingEffect: OnFireEffectPlugin, OnHitEffectPlugin {

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        var line = GrapplingLine(weapon!!, projectile!!)
        Global.getCombatEngine().addLayeredRenderingPlugin(line)

        projectile.setCustomData("rat_grapple_data", line)
    }

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        var line = projectile!!.customData.get("rat_grapple_data") as GrapplingLine
        line.target = target

        var offset = Vector2f.sub(point, target!!.location, Vector2f())
        offset = Misc.rotateAroundOrigin(offset, -target!!.facing)

        line.targetPoint = offset
    }
}