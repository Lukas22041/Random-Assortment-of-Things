package assortment_of_things.snippets

import assortment_of_things.misc.RATSettings
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDebug.LunaSnippet
import lunalib.lunaDebug.SnippetBuilder
import lunalib.lunaExtensions.isNotNull

class ProcgenDebugSnippet : LunaSnippet()
{
    override fun getName(): String {
        return "Print Procedual Generation Data"
    }

    override fun getDescription(): String {
        return "Displays some data related to the Sector generation."
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

        var highlightColor = Misc.getHighlightColor()
        var baseColor = Misc.getBasePlayerColor()
        var positiveColor = Misc.getPositiveHighlightColor()
        var sector = Global.getSector()

        var systems = sector.starSystems
        var constellations = ArrayList<Constellation>()

        var ruinsMainCount = 0
        var ruinsSecondaryCount = 0

        var remnantMainCount = 0
        var remnantSecondarCount = 0

        var miscCount = 0
        var miscSkipCount = 0

        var derelictCount = 0
        var derelictMothership = 0

        var gates = 0

        for (system in systems)
        {
            if (!constellations.contains(system.constellation))
            {
                constellations.add(system.constellation)
            }

            system.customEntities.forEach {
                when (it.customEntityType)
                {
                    "inactive_gate" -> gates++
                }
            }

            system.tags.forEach {
                when (it)
                {
                    Tags.THEME_REMNANT_MAIN -> remnantMainCount++
                    Tags.THEME_REMNANT_SECONDARY -> remnantSecondarCount++

                    Tags.THEME_MISC -> miscCount++
                    Tags.THEME_MISC_SKIP -> miscSkipCount++

                    Tags.THEME_RUINS_MAIN -> ruinsMainCount++
                    Tags.THEME_RUINS_SECONDARY -> ruinsSecondaryCount++

                    Tags.THEME_DERELICT -> derelictCount++
                    Tags.THEME_DERELICT_MOTHERSHIP -> derelictMothership++
                }
            }
        }

        output.addPara("Printing Procgen Data", 0f, positiveColor, positiveColor)
        output.addSpacer(10f)

        var generatedWith = "Vannila or Adjusted Sector"
        if (Global.getSector().memoryWithoutUpdate.isNotNull("\$rat_sector_generated_with_rat")) generatedWith = "Random Assortment of Things"

        output.addPara("Sector Data: ", 0f, highlightColor, highlightColor)
        output.addPara("Constellations: ${constellations.size}", 0f, baseColor, baseColor)
        output.addPara("Systems: ${systems.size}", 0f, baseColor, baseColor)

        output.addSpacer(5f)

        output.addPara("RAT Sector Data: ", 0f, highlightColor, highlightColor)
        output.addPara("Sector Generated with: $generatedWith", 0f, baseColor, baseColor)

        output.addSpacer(5f)

        output.addPara("Theme Data: ", 0f, highlightColor, highlightColor)
        output.addPara("Theme Misc Systems: $miscCount", 0f, baseColor, baseColor)
        output.addPara("Theme Misc Skip Systems: $miscSkipCount", 0f, baseColor, baseColor)

        output.addSpacer(5f)

        output.addPara("Theme Ruins-Main Systems: $ruinsMainCount", 0f, baseColor, baseColor)
        output.addPara("Theme Ruins-Secondary Systems: $ruinsSecondaryCount", 0f, baseColor, baseColor)

        output.addSpacer(5f)

        output.addPara("Theme Derelict Systems: $derelictCount", 0f, baseColor, baseColor)
        output.addPara("Theme Derelict-Mothership Systems: $derelictMothership", 0f, baseColor, baseColor)

        output.addSpacer(5f)

        output.addPara("Theme Remnant-Main Systems: $remnantMainCount", 0f, baseColor, baseColor)
        output.addPara("Theme Remnant-Secondary Systems: $remnantSecondarCount", 0f, baseColor, baseColor)

        output.addSpacer(10f)

        output.addPara("Entity Data: ", 0f, highlightColor, highlightColor)
        output.addPara("Gates: $gates", 0f, baseColor, baseColor)

    }
}