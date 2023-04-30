package assortment_of_things.modular_weapons.data

import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.loading.MuzzleFlashSpec
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import java.awt.Color

class RATModifieableProjectileWeaponSpec(var spec: WeaponSpecAPI)  {


    fun getProjectileSpec() : ProjectileSpecAPI? {
        return ReflectionUtils.invoke("getProjectileSpec", spec) as ProjectileSpecAPI?
    }

    fun setProjectileSpec(projSpec: ProjectileSpecAPI) {
        ReflectionUtils.invoke("setProjectileSpec", spec, projSpec)
    }

    //General

    fun setWeaponName(name: String) {
        ReflectionUtils.invoke("setWeaponName", spec, name)
    }

    fun setOrdnancePointCost(cost: Float) {
        ReflectionUtils.invoke("setOrdnancePointCost", spec, cost)
    }

    fun setGlowColor(color: Color) {
        ReflectionUtils.invoke("setGlowColor", spec, color)
    }

    fun getMuzzleFlashSpec() : MuzzleFlashSpec {
        return ReflectionUtils.invoke("getMuzzleFlashSpec", spec) as MuzzleFlashSpec
    }

    //Type

    fun setType(type: WeaponType) {
        ReflectionUtils.invoke("setType", spec, type)
    }

    fun setMountType(type: WeaponType) {
        ReflectionUtils.invoke("setMountType", spec, type)
    }

    fun setDamageType(type: DamageType) {
        ReflectionUtils.invoke("setDamageType", spec, type)
    }

    fun setSize(size: WeaponSize) {
        ReflectionUtils.invoke("setSize", spec, size)
    }


    fun fluxPerDamage(amount: Float) {
        var derived = ReflectionUtils.invoke("getDerivedStats", spec)
        ReflectionUtils.set("fluxPerDam", derived!!, amount)
    }

    //Fire Rate Stuff

    fun setChargeUp(chargeup: Float) {
        ReflectionUtils.invoke("setChargeTime", spec, chargeup)
    }

    fun setChargeDown(chargedown: Float) {
        ReflectionUtils.invoke("setRefireDelay", spec, chargedown)
    }

    fun setBurstSize(burstSize: Int) {
        ReflectionUtils.invoke("setBurstSize", spec, burstSize)
    }

    fun setBurstDelay(delay: Float) {
        ReflectionUtils.invoke("setBurstDelay", spec, delay)
    }


    //Cost

    fun setEnergyPerShot(cost: Float) {
        ReflectionUtils.invoke("setEnergyPerShot", spec, cost)
    }

    // Ammo

    fun setMaxAmmo(amount: Int) {
        ReflectionUtils.invoke("setMaxAmmo", spec, amount)
    }

    fun setAmmoPerSecond(amount: Float) {
        ReflectionUtils.invoke("setAmmoPerSecond", spec, amount)
    }

    fun setReloadSize(amount: Float) {
        ReflectionUtils.invoke("setReloadSize", spec, amount)
    }




    // Spread

    fun setMinSpread(min: Float) {
        ReflectionUtils.invoke("setMinSpread", spec, min)
    }

    fun setMaxSpread(max: Float) {
        ReflectionUtils.invoke("setMaxSpread", spec, max)
    }

    fun setSpreadBuildup(amount: Float) {
        ReflectionUtils.invoke("setSpreadBuildup", spec, amount)
    }

    fun setSpreadDecayRate(amount: Float) {
        ReflectionUtils.invoke("setSpreadDecayRate", spec, amount)
    }


    //range
    fun setMaxRange(range: Float) {
        var projSpec = getProjectileSpec()

        ReflectionUtils.invoke("setMaxRange", spec!!, range)
        ReflectionUtils.invoke("setMaxRange", projSpec!!, range)
    }

    fun setTurnRate(amount: Float) {
        ReflectionUtils.invoke("setTurnRate", spec, amount)
    }
}