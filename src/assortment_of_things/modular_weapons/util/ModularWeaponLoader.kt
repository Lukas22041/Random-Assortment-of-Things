package assortment_of_things.modular_weapons.util

import assortment_of_things.modular_weapons.data.RATModifieableProjectileSpec
import assortment_of_things.modular_weapons.data.RATModifieableProjectileWeaponSpec
import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.effects.ModularWeaponEffect
import com.fs.starfarer.api.Global
import lunalib.lunaDelegates.LunaMemory
import java.awt.Color


object ModularWeaponLoader
{

    var sectorDataMemory: MutableMap<String, SectorWeaponData>? by LunaMemory("rat_sectorWeaponData", HashMap())

    fun saveDataToSector(newData: SectorWeaponData)
    {
        var data = sectorDataMemory
        data!!.put(newData.id, newData)
        sectorDataMemory = data
    }

    fun getData(specId: String) : SectorWeaponData
    {
        var data = sectorDataMemory!!.get(specId)

        if (data == null)
        {
            data = SectorWeaponData(specId)
            data = data as SectorWeaponData

            data.name = "Modular Weapon"

            saveDataToSector(data)
        }

        return data
    }

    fun getAllData() : MutableMap<String,SectorWeaponData>
    {
        var data = sectorDataMemory
        return data!!
    }

    fun getAllDataAsList() : List<SectorWeaponData>
    {
        var data = sectorDataMemory!!.map { it.value }.sortedBy { it.id }
        return data
    }

    fun resetAllData()
    {
        sectorDataMemory = HashMap()
    }

    fun getWeaponEffects(specId: String) : List<ModularWeaponEffect>
    {
        return getData(specId).effects.sortedBy { it.getOrder() }
    }

    fun applyStatToSpecsForAll()
    {
        var weapons =Global.getSettings().allWeaponSpecs.filter { it.hasTag("rat_modular_weapon") }
        for (weapon in weapons)
        {
            var data = getData(weapon.weaponId)
            applyStatsToSpec(data)
        }
    }

    fun calculateEffectStats(data: SectorWeaponData)
    {

        data.range.clear()
        data.turnrate.clear()

        data.projectileLength.clear()
        data.projectileWidth.clear()
        data.projectileSpeed.clear()

        data.damagePerShot.clear()
        data.energyPerShot.clear()
        data.empDamage.clear()

        data.maxAmmo.clear()
        data.ammoPerSecond.clear()
        data.reloadSize.clear()

        data.rngAttempts = 0

        data.chargeUp.clear()
        data.chargeDown.clear()
        data.burstSize.clear()
        data.burstDelay.clear()

        data.minSpread.clear()
        data.maxSpread.clear()
        data.spreadBuildup.clear()
        data.spreadDecay.clear()

        for (effect in data.effects)
        {
            effect.addStats(data)
        }
    }

    fun applyStatsToSpec(data: SectorWeaponData)
    {
        try {
            var spec = Global.getSettings().getWeaponSpec(data.id)
            var modableSpec = RATModifieableProjectileWeaponSpec(spec)
            var modableProjectileSpec = RATModifieableProjectileSpec(modableSpec.getProjectileSpec()!!)

            /* var desc = Global.getSettings().getDescription(data.id, Description.Type.WEAPON)
             desc.text1 = data.description*/


            modableSpec.setWeaponName(data.name)

            modableSpec.setEnergyPerShot(data.energyPerShot.getValue())


            modableSpec.setSize(data.weaponSize)
            modableSpec.setMountType(data.mountType)
            modableSpec.setDamageType(data.damageType)

            modableSpec.setMaxAmmo(data.maxAmmo.getValue())
            modableSpec.setAmmoPerSecond(data.ammoPerSecond.getValue())
            modableSpec.setReloadSize(data.reloadSize.getValue())

            modableSpec.setChargeDown(data.chargeDown.getValue())
            modableSpec.setChargeUp(data.chargeUp.getValue())
            modableSpec.setBurstSize(data.burstSize.getValue())
            modableSpec.setBurstDelay(data.burstDelay.getValue())

            modableSpec.setMinSpread(data.minSpread.getValue())
            modableSpec.setMaxSpread(data.maxSpread.getValue())
            modableSpec.setSpreadBuildup(data.spreadBuildup.getValue())
            modableSpec.setSpreadDecayRate(data.spreadDecay.getValue())

            modableSpec.setMaxRange(data.range.getValue())
            modableSpec.setTurnRate(data.turnrate.getValue())

            modableProjectileSpec.setLength(data.projectileLength.getValue())
            modableProjectileSpec.setWidth(data.projectileWidth.getValue())
            modableProjectileSpec.setMoveSpeed(data.projectileSpeed.getValue())

            modableProjectileSpec.setDamage(data.damagePerShot.getValue())
            modableProjectileSpec.setEmpDamage(data.empDamage.getValue())

            modableProjectileSpec.setFringeColor(data.color)
            modableProjectileSpec.setCoreColor(Color.white)

            //need to change this value or the display is fucked up
            modableSpec.fluxPerDamage(data.energyPerShot.getValue() / (data.damagePerShot.getValue() / data.burstSize.getValue()))
        } catch (e: Throwable)
        {
            resetAllData()
            applyStatToSpecsForAll()
        }
    }
}