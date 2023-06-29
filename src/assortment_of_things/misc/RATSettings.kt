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

    //Procgen
    @JvmStatic
    var procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
    @JvmStatic
    var procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")
    @JvmStatic
    //var procgenImprovedMisc = LunaSettings.getBoolean(modID, "rat_improvedMisc")

    var hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
    var shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")

    //Parallel Construction
    var parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
    var parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

    //Misc
    var disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")

    //Modular
    var enableModular = LunaSettings.getBoolean(modID, "rat_modularEnabled")
    var modularDevmode = LunaSettings.getBoolean(modID, "rat_modularDevmode")


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

        procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
       // procgenImprovedMisc = LunaSettings.getBoolean(modID, "rat_improvedMisc")
        procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")

        parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
        parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

        disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")

        hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
        shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")

        enableModular = LunaSettings.getBoolean(modID, "rat_modularEnabled")
        modularDevmode = LunaSettings.getBoolean(modID, "rat_modularDevmode")

        enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
        minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
        minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
        minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")
        MinimapUI.reset = true

        LootModifier.modifySpawns()
    }
}