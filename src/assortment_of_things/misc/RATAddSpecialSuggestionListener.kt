package assortment_of_things.misc

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.campaign.items.ConsumeableIndustryBP
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.IndustrySpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lazywizard.console.BaseCommand.CommandContext
import org.lazywizard.console.BaseCommand.CommandResult
import org.lazywizard.console.CommandListener
import org.lazywizard.console.CommandListenerWithSuggestion
import java.util.*

class RATAddSpecialSuggestionListener : CommandListenerWithSuggestion {

    override fun getSuggestions(command: String, parameter: Int, previous: List<String>, context: CommandContext?): List<String>? {
        //This listener wants to add arguments to the "AddSpecial" command, and only for the 2nd parameter.
        if (!command.equals("AddSpecial", ignoreCase = true) || parameter != 1) return null
        if (previous.isEmpty()) return null
        val suggestions: MutableList<String> = ArrayList()
        val specialItemID = previous[0]

        //Check for matching special item
        when (specialItemID.lowercase()) {
            "rat_alteration_install" -> suggestions.addAll(Global.getSettings().allHullModSpecs.filter { it.effect is BaseAlteration }.map { it.id })
            "rat_consumeable_industry" -> suggestions.addAll(Global.getSettings().allIndustrySpecs.filter { it.hasTag("rat_consumeable_industry" ) }.map { it.id })
            "rat_artifact" -> suggestions.addAll(ArtifactUtils.artifacts.map { it.id })
        }

        return suggestions
    }

    override fun onPreExecute(command: String,
                              args: String,
                              context: CommandContext,
                              alreadyIntercepted: Boolean): Boolean {
        return false
    }

    override fun execute(command: String, args: String, context: CommandContext): CommandResult? {
        return null
    }

    override fun onPostExecute(command: String,
                               args: String,
                               result: CommandResult,
                               context: CommandContext,
                               interceptedBy: CommandListener?) {
    }
}