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

    //Themes
    //@JvmStatic
    //var enableThemes = LunaSettings.getBoolean(modID, "rat_enableThemes")

    @JvmStatic
    var enableOutposts = LunaSettings.getBoolean(modID, "rat_enableOutposts")
    @JvmStatic
    var enableChiral = LunaSettings.getBoolean(modID, "rat_enableChiral")

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


    //UI
    var enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
    var minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
    var minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
    var minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")


    //Jokes
    var sillyContentEnabled = LunaSettings.getBoolean(modID, "rat_minimapCenter")
    //var skeleton = LunaSettings.getBoolean(modID, "rat_theSkeletonAppears")

    override fun settingsChanged(modID: String) {
        if (modID == RATSettings.modID)
        {
            loadSettings()
        }
    }

    @JvmStatic
    fun loadSettings()
    {
        enableOutposts = LunaSettings.getBoolean(modID, "rat_enableOutposts")
        enableChiral = LunaSettings.getBoolean(modID, "rat_enableChiral")

        procgenScaleModifier = LunaSettings.getString(modID, "rat_systemScaleV2")
       // procgenImprovedMisc = LunaSettings.getBoolean(modID, "rat_improvedMisc")
        procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")

        parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
        parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

        disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")
        sillyContentEnabled = LunaSettings.getBoolean(modID, "rat_sillyContent")

        hullmodLootFrequency = LunaSettings.getFloat(modID, "rat_hullmodBPLootFrequency")
        shipLootFrequency = LunaSettings.getFloat(modID, "rat_shipBPLootFrequency")

        enableModular = LunaSettings.getBoolean(modID, "rat_modularEnabled")

        enableMinimap = LunaSettings.getBoolean(modID, "rat_enableMinimap")
        minimapShape = LunaSettings.getString(modID, "rat_minimapShape")
        minimapStarscape = LunaSettings.getBoolean(modID, "rat_minimapStarscape")
        minimapFueloverlay = LunaSettings.getBoolean(modID, "rat_minimapFuel")
        MinimapUI.reset = true

        LootModifier.modifySpawns()
    }
}