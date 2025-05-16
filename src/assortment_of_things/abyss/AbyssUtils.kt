package assortment_of_things.abyss

import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap


/*enum class AbyssDifficulty {
    Normal, Hard
}*/

object AbyssUtils {

    var ABYSS_COLOR = Color(255, 0, 50)
    var DARK_ABYSS_COLOR = Color(77, 0, 15) // 30% Brightness
    var SERAPH_COLOR = Color(196, 20, 35)
    var GENESIS_COLOR = Color(140, 0, 250)
    var SIERRA_COLOR = Color(205,155,255,255)
    var FACTION_ID = "rat_abyssals"

    var SYSTEM_TAG = "rat_abyssal_depths"

    var ABYSS_DATA_KEY = "\$rat_abyss_data"

    @JvmStatic
    fun getData() : AbyssData {
        var data = Global.getSector().memoryWithoutUpdate.get(ABYSS_DATA_KEY) as AbyssData?
        if (data == null) {
            data = AbyssData()
            Global.getSector().memoryWithoutUpdate.set(ABYSS_DATA_KEY, data)
        }
        return data
    }

    @JvmStatic
    fun getBiomeManager() = getData().biomeManager

    @JvmStatic
    fun getSystem() = getData().system!!

    fun isShowFog() : Boolean {
        //return true
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

   /* fun getDifficulty() : AbyssDifficulty {
        if (RATSettings.abyssDifficulty == "Hard") return AbyssDifficulty.Hard
        return AbyssDifficulty.Normal
    }*/





    fun initAbyssalFleetBehaviour(fleet: CampaignFleetAPI, random: Random)
    {
        fleet.removeAbility(Abilities.EMERGENCY_BURN)
        fleet.removeAbility(Abilities.SENSOR_BURST)
        fleet.removeAbility(Abilities.GO_DARK)

        // to make sure they attack the player on sight when player's transponder is off

        // to make sure they attack the player on sight when player's transponder is off
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
        //fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = true
        // fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
        fleet.memoryWithoutUpdate[MemFlags.DO_NOT_TRY_TO_AVOID_NEARBY_FLEETS] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
        fleet.memoryWithoutUpdate[MemFlags.FLEET_DO_NOT_IGNORE_PLAYER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true

        RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)

        val salvageSeed: Long = random.nextLong()
        fleet.memoryWithoutUpdate[MemFlags.SALVAGE_SEED] = salvageSeed
    }



}