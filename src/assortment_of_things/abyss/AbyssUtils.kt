package assortment_of_things.abyss

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap


enum class AbyssDifficulty {
    Normal, Hard
}

data class LinkedFracture(val fracture1: SectorEntityToken, var fracture2: SectorEntityToken)

object AbyssUtils {

    var ABYSS_COLOR = Color(255, 0, 50)
    var DARK_ABYSS_COLOR = Color(77, 0, 15) // 30% Brightness
    var GENESIS_COLOR = Color(140, 0, 250)
    var SIERRA_COLOR = Color(205,155,255,255)
    var FACTION_ID = "rat_abyssals"

    var ABYSS_DATA_KEY = "\$rat_abyss_data"

    fun getData() : AbyssData {
        var data = Global.getSector().memoryWithoutUpdate.get(ABYSS_DATA_KEY) as AbyssData?
        if (data == null) {
            data = AbyssData()
            Global.getSector().memoryWithoutUpdate.set(ABYSS_DATA_KEY, data)
        }
        return data
    }

    fun isShowFog() : Boolean {
        return true
        return !Global.getSettings().isDevMode
    }

    fun isPlayerInAbyss() = isInAbyss(Global.getSector()?.playerFleet)

    fun isInAbyss(entity: SectorEntityToken?) : Boolean {
        if (entity == null) return false
        return entity.containingLocation == getData().system
    }

    fun isAnyFleetTargetingPlayer() : Boolean {
        if (Global.getSettings().isDevMode) return false
        val playerFleet = Global.getSector().playerFleet

        for (fleet in playerFleet.containingLocation.fleets) {
            if (fleet.ai == null) continue
            if (fleet.isStationMode) continue

            var ai = fleet.ai
            if (ai is ModularFleetAIAPI) {
                var tactical= ai.tacticalModule ?: continue
                var target = tactical.target ?: continue

                if (tactical.isFleeing) continue
                if (fleet.visibilityLevelOfPlayerFleet == SectorEntityToken.VisibilityLevel.NONE) continue
                if (playerFleet.fleetPoints >= fleet.fleetPoints * 2) continue

                return true
            }
        }

        return false
    }

    fun getDifficulty() : AbyssDifficulty {
        if (RATSettings.abyssDifficulty == "Hard") return AbyssDifficulty.Hard
        return AbyssDifficulty.Normal
    }



    fun addAlterationsToFleet(fleet: CampaignFleetAPI, chancePerShip: Float, random: Random) {

        var members = fleet.fleetData.membersListCopy

        fleet.inflateIfNeeded()
        for (member in members)
        {
            if (random.nextFloat() > chancePerShip) continue

            var alterations = HashMap<String,Float>()
            alterations.put("rat_qualityAssurance", 1f)
            alterations.put("rat_timegear", 1f)
            alterations.put("rat_overloaded_systems", 1f)
            alterations.put("rat_advanced_flux_crystal", 1f)
            alterations.put("rat_boost_redirector", 1f)
            alterations.put("rat_soft_shields", 1f)

            if (!member.isFrigate) {
                alterations.put("rat_preperation", 1f)
            }

            var hasBay = member.variant.baseOrModSpec().hints.contains(ShipHullSpecAPI.ShipTypeHints.CARRIER)

            var hasBallistic = false
            var hasEnergy = false
            var hasMissile = false

            var weapons = member.variant.fittedWeaponSlots.map { member.variant.getWeaponSpec(it) }
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.BALLISTIC }) hasBallistic = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.ENERGY }) hasEnergy = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.MISSILE }) hasMissile = true

            if (hasBay) alterations.putAll(mapOf("rat_temporalAssault" to 0.5f, "rat_perseverance" to 1f, "rat_magneticStorm" to 1f, "rat_plasmaticShield" to 1f))

            if (hasBallistic) alterations.putAll(mapOf("rat_ballistic_focus" to 1.5f))
            if (hasEnergy) alterations.putAll(mapOf("rat_energy_focus" to 1.5f))
            if (hasMissile) alterations.putAll(mapOf("rat_missile_reserve" to 0.5f))

            var picker = WeightedRandomPicker<String>()
            alterations.forEach { picker.add(it.key, it.value) }

            var pick = picker.pick()

            if (member.variant.source != VariantSource.REFIT)
            {
                var variant = member.variant.clone();
                variant.originalVariant = null;
                variant.hullVariantId = Misc.genUID()
                variant.source = VariantSource.REFIT
                member.setVariant(variant, false, true)
            }
            member.variant.addMod(pick)
            member.updateStats()

        }
        fleet.inflateIfNeeded()

    }


}