package assortment_of_things.modular_weapons.util

import assortment_of_things.modular_weapons.data.RATModifieableProjectileSpec
import assortment_of_things.modular_weapons.data.RATModifieableProjectileWeaponSpec
import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.effects.ModularWeaponEffect
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import lunalib.lunaDelegates.LunaMemory
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.lang.Exception


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

        data.isPD = false

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

    fun applyStatsToSpec(data: SectorWeaponData, first: Boolean = true)
    {
        try {
            var spec = Global.getSettings().getWeaponSpec(data.id)
            var modableSpec = RATModifieableProjectileWeaponSpec(spec)
            var modableProjectileSpec = RATModifieableProjectileSpec(modableSpec.getProjectileSpec()!!)

            /* var desc = Global.getSettings().getDescription(data.id, Description.Type.WEAPON)
             desc.text1 = data.description*/




         /*   var testStat = MutableStat(10f)

            testStat.modifyMult("Test", 2.5f)
            testStat.modifyMult("Test2", 1.1f)
            testStat.modifyFlat("Test", 20f)

            var value = testStat.modifiedValue*/

            modableSpec.setWeaponName(data.name)

            modableSpec.setEnergyPerShot(data.energyPerShot.getValue())

            var test = spec.tags

            if (data.isPD)
            {
                spec.aiHints.add(WeaponAPI.AIHints.PD)
                spec.aiHints.add(WeaponAPI.AIHints.ANTI_FTR)
                spec.primaryRoleStr = "Point Defense"
            }
            else
            {
                if (spec.aiHints.contains(WeaponAPI.AIHints.PD))
                {
                    spec.aiHints.remove(WeaponAPI.AIHints.PD)
                    spec.aiHints.remove(WeaponAPI.AIHints.ANTI_FTR)
                    spec.primaryRoleStr = "General"
                }
            }

            var test2 = spec.aiHints

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

            modableSpec.setGlowColor(data.color.darker().setAlpha(200))
            var muzzle = modableSpec.getMuzzleFlashSpec()

            muzzle.particleCount = data.muzzleParticles
            muzzle.particleDuration = data.muzzleDuration
            muzzle.particleColor = data.color.darker().darker().setAlpha(100)

            //need to change this value or the display is fucked up
            modableSpec.fluxPerDamage(data.energyPerShot.getValue() / (data.damagePerShot.getValue() / data.burstSize.getValue()))
        } catch (e: Throwable)
        {
            if (first)
            {
                resetAllData()
                applyStatToSpecsForAll()
            }
            else
            {
                throw Exception("Failure while loading weapon data for RAT modular weapons")
            }
        }
    }
}