package assortment_of_things.misc

import assortment_of_things.campaign.ui.MinimapUI
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener

object RATSettings : LunaSettingsListener
{

    //Legacy options, should no longer be used, only there to prevent old files from crashing.
    //Those old files remain to keep save compatibility from before the split in to a seperate mod.
    @JvmStatic
    var procgenScaleModifier = "Vanilla"
    @JvmStatic
    var procgenHyperspaceCloudMod = 0.8f





    var modID = "assortment_of_things"

    //Abyss
    var enableAbyss = LunaSettings.getBoolean(modID, "rat_abyssEnabled")
    var abyssScale = LunaSettings.getString(modID, "rat_abyssScale")
    var brighterAbyss = LunaSettings.getBoolean(modID, "rat_abyssBrighter")
    //var abyssDifficulty = LunaSettings.getString(modID, "rat_abyssDifficulty")

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



    /*var hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
    var shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")*/

    //Escprt
    var escortEnabled = LunaSettings.getBoolean(modID, "rat_escortEnabled")

    //Which Mod
    var whichModShips = LunaSettings.getBoolean(modID, "rat_whichModShips")
    var whichModWeapons = LunaSettings.getBoolean(modID, "rat_whichModWeapons")
    var whichModFighters = LunaSettings.getBoolean(modID, "rat_whichModFighters")
    var whichModHullmods = LunaSettings.getBoolean(modID, "rat_whichModHullmods")
    var whichModCargo = LunaSettings.getBoolean(modID, "rat_whichModCargo")
    var whichModIndustries = LunaSettings.getBoolean(modID, "rat_whichModIndustries")
    var whichModConditions = LunaSettings.getBoolean(modID, "rat_whichModConditions")
    var whichModSkills = LunaSettings.getBoolean(modID, "rat_whichModSkills")

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
        abyssScale = LunaSettings.getString(modID, "rat_abyssScale")
        brighterAbyss = LunaSettings.getBoolean(modID, "rat_abyssBrighter")
        //abyssDifficulty = LunaSettings.getString(modID, "rat_abyssDifficulty")

        exoEnabled = LunaSettings.getBoolean(modID, "rat_exotechEnabled")

        relicsEnabled = LunaSettings.getBoolean(modID, "rat_relicsEnabled")
        relicsEnabledStations = LunaSettings.getBoolean(modID, "rat_enableRelicsStations")
        relicsEnabledConditions = LunaSettings.getBoolean(modID, "rat_enableRelicsConditions")

        enableFrontiers = LunaSettings.getBoolean(modID, "rat_frontiersEnabled")
        frontiersIncomeMult = LunaSettings.getFloat(modID, "rat_frontiersIncomeMult")
        frontiersCostMult = LunaSettings.getFloat(modID, "rat_frontiersCostMult")

        backgroundsEnabled = LunaSettings.getBoolean(modID, "rat_backgroundsEnabled")
        backgroundsAbilityKeybind = LunaSettings.getInt(modID, "rat_backgroundSpecialHotkey")

        /*procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
        procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")*/

        escortEnabled = LunaSettings.getBoolean(modID, "rat_escortEnabled")

        whichModShips = LunaSettings.getBoolean(modID, "rat_whichModShips")
        whichModWeapons = LunaSettings.getBoolean(modID, "rat_whichModWeapons")
        whichModFighters = LunaSettings.getBoolean(modID, "rat_whichModFighters")
        whichModHullmods = LunaSettings.getBoolean(modID, "rat_whichModHullmods")
        whichModCargo = LunaSettings.getBoolean(modID, "rat_whichModCargo")
        whichModIndustries = LunaSettings.getBoolean(modID, "rat_whichModIndustries")
        whichModConditions = LunaSettings.getBoolean(modID, "rat_whichModConditions")

        parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
        parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

        disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")

        /*hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
        shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")*/

        enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
        minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
        minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
        minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")
        MinimapUI.reset = true

        //LootModifier.modifySpawns()
    }
}