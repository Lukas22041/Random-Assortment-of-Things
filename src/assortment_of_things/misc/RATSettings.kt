package assortment_of_things.misc

import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.MinimapUI
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener

object RATSettings : LunaSettingsListener
{

    var modID = "assortment_of_things"

    //Abyss
    var enableAbyss = LunaSettings.getBoolean(modID, "rat_abyssEnabled")
    var brighterAbyss = LunaSettings.getBoolean(modID, "rat_abyssBrighter")
    var abyssDifficulty = LunaSettings.getString(modID, "rat_abyssDifficulty")

    //Exotech
    var exoEnabled = LunaSettings.getBoolean(modID, "rat_exotechEnabled")

    //Relics
    var relicsEnabled = LunaSettings.getBoolean(modID, "rat_relicsEnabled")
    var relicsEnabledStations = LunaSettings.getBoolean(modID, "rat_enableRelicsStations")
    var relicsEnabledConditions = LunaSettings.getBoolean(modID, "rat_enableRelicsConditions")

    //Frontiers
    var enableFrontiers = LunaSettings.getBoolean(modID, "rat_frontiersEnabled")
    var frontiersIncomeMult = LunaSettings.getFloat(modID, "rat_frontiersIncomeMult")
    var frontiersCostMult = LunaSettings.getFloat(modID, "rat_frontiersCostMult")

    //Backgrounds
    var backgroundsEnabled = LunaSettings.getBoolean(modID, "rat_backgroundsEnabled")
    var backgroundsAbilityKeybind = LunaSettings.getInt(modID, "rat_backgroundSpecialHotkey")

    //Procgen
    @JvmStatic
    var procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
    @JvmStatic
    var procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")

    var hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
    var shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")

    //Parallel Construction
    var parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
    var parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

    //Misc
    var disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")


    //UI

    var enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
    var minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
    var minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
    var minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")


    override fun settingsChanged(modID: String) {
        if (modID == RATSettings.modID)
        {
            loadSettings()
        }
    }

    @JvmStatic
    fun loadSettings()
    {
        enableAbyss = LunaSettings.getBoolean(modID, "rat_abyssEnabled")
        brighterAbyss = LunaSettings.getBoolean(modID, "rat_abyssBrighter")
        abyssDifficulty = LunaSettings.getString(modID, "rat_abyssDifficulty")

        exoEnabled = LunaSettings.getBoolean(modID, "rat_exotechEnabled")

        relicsEnabled = LunaSettings.getBoolean(modID, "rat_relicsEnabled")
        relicsEnabledStations = LunaSettings.getBoolean(modID, "rat_enableRelicsStations")
        relicsEnabledConditions = LunaSettings.getBoolean(modID, "rat_enableRelicsConditions")

        enableFrontiers = LunaSettings.getBoolean(modID, "rat_frontiersEnabled")
        frontiersIncomeMult = LunaSettings.getFloat(modID, "rat_frontiersIncomeMult")
        frontiersCostMult = LunaSettings.getFloat(modID, "rat_frontiersCostMult")

        backgroundsEnabled = LunaSettings.getBoolean(modID, "rat_backgroundsEnabled")
        backgroundsAbilityKeybind = LunaSettings.getInt(modID, "rat_backgroundSpecialHotkey")

        procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
        procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")

        parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
        parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

        disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")

        hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
        shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")

        enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
        minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
        minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
        minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")
        MinimapUI.reset = true

        LootModifier.modifySpawns()
    }
}