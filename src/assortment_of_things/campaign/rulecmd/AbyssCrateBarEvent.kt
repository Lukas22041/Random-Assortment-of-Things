package assortment_of_things.campaign.rulecmd

import assortment_of_things.abyss.intel.AbyssCrateIntel
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.makeImportant
import java.awt.Color

class AbyssCrateBarEvent : BaseCommandPlugin() {

    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var crate = Global.getSector().memoryWithoutUpdate.get("\$rat_singularity_cache") as SectorEntityToken?

        dialog!!.textPanel.addPara("While enjoying a drink at the bar, you overhear some spacers talking rumors about a lost but rediscovered shipment of domain-era cargo.",
            Misc.getTextColor(), Misc.getHighlightColor(), "domain-era")

        var label = dialog!!.textPanel.addPara("This shipment has appearently been discovered in the ${crate!!.containingLocation.nameWithNoType} system. " +
                "Some curious salvagers seem to have already made a voyage towards it, but have been quickly discouraged by the activity of automated hulls in the vicinity of the shipment.")

        label.setHighlight("${crate!!.containingLocation.nameWithNoType} system", "automated")
        label.setHighlightColors(Misc.getHighlightColor(), Color(70,255,235))

        var intel = AbyssCrateIntel(crate!!)
        Global.getSector().intelManager.addIntel(intel)

        intel.sendUpdate(null, dialog.textPanel)

        crate.makeImportant("Abyssal Crate")
        crate.setDiscoverable(null)
        crate.setSensorProfile(null)

        return false
    }

}