package assortment_of_things.misc

import com.fs.starfarer.api.Global
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
    var procgenScaleModifier = LunaSettings.getInt(modID, "rat_systemScale")
    @JvmStatic
    var procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")
    @JvmStatic
    var procgenImprovedMisc = LunaSettings.getBoolean(modID, "rat_improvedMisc")

    //Parallel Construction
    var parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
    var parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

    //Misc
    var disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")

    override fun settingsChanged(modID: String) {
        if (modID == RATSettings.modID)
        {
            loadSettings()
        }
    }

    @JvmStatic
    fun loadSettings()
    {
        //enableThemes = LunaSettings.getBoolean(modID, "rat_enableThemes"

        enableOutposts = LunaSettings.getBoolean(modID, "rat_enableOutposts")
        enableChiral = LunaSettings.getBoolean(modID, "rat_enableChiral")

        procgenScaleModifier = LunaSettings.getInt(modID, "rat_systemScale")
        procgenImprovedMisc = LunaSettings.getBoolean(modID, "rat_improvedMisc")
        procgenHyperspaceCloudMod = LunaSettings.getFloat(modID, "rat_hyperspaceCloudsMod")

        parallelEnabled = LunaSettings.getBoolean(modID, "rat_parallelEnabled")
        parallelApplyToNPCs = LunaSettings.getBoolean(modID, "rat_parallelApplyToNPCs")

        disableHelp = LunaSettings.getBoolean(modID, "rat_forceDisableHelp")
    }
}