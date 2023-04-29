package assortment_of_things.modular_weapons.data

import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import java.awt.Color

class RATModifieableProjectileSpec(var spec: ProjectileSpecAPI)  {



    fun setLength(length: Float) {
        ReflectionUtils.invoke("setLength", spec, length)
    }

    fun setWidth(width: Float) {
        ReflectionUtils.invoke("setWidth", spec, width)
    }

    fun setDamage(damage: Float)
    {
        var damageClass = ReflectionUtils.invoke("getDamage", spec)
        ReflectionUtils.invoke("setDamage", damageClass!!, damage)
    }

    fun setEmpDamage(damage: Float)
    {
        var damageClass = ReflectionUtils.invoke("getDamage", spec)
        ReflectionUtils.invoke("setFluxComponent", damageClass!!, damage)
    }

    fun setFringeColor(color: Color) {
        ReflectionUtils.invoke("setFringeColor", spec, color)
    }

    fun setCoreColor(color: Color) {
        ReflectionUtils.invoke("setCoreColor", spec, color)
    }

    fun setMoveSpeed(speed: Float) {
        ReflectionUtils.invoke("setMoveSpeed", spec, speed)
    }


}