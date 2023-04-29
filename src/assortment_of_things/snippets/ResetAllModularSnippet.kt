package assortment_of_things.snippets

import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDebug.LunaSnippet
import lunalib.lunaDebug.SnippetBuilder
import lunalib.lunaExtensions.isNotNull

class ResetAllModularSnippet : LunaSnippet()
{
    override fun getName(): String {
        return "Reset Modular Weapon Data"
    }

    override fun getDescription(): String {
        return "Resets all modular weapon data on this save, which allows you to modify them again. Keep in mind any weapon of a slot that is already in a ship mount or in an inventory will get changed to default aswell."
    }

    override fun getModId(): String {
        return RATSettings.modID
    }

    override fun getTags(): MutableList<String> {
        return mutableListOf(LunaSnippet.SnippetTags.Debug.toString())
    }

    override fun addParameters(parameter: SnippetBuilder?) {

    }

    override fun execute(parameter: MutableMap<String, Any>?, output: TooltipMakerAPI) {
        ModularWeaponLoader.resetAllData()
        ModularWeaponLoader.applyStatToSpecsForAll()
    }
}