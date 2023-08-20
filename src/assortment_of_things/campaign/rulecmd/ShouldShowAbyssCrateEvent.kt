package assortment_of_things.campaign.rulecmd

import assortment_of_things.abyss.intel.AbyssCrateIntel
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory

class ShouldShowAbyssCrateEvent : BaseCommandPlugin() {

    override fun execute(ruleId: String?,  dialog: InteractionDialogAPI?,params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {
        var crate = Global.getSector().memoryWithoutUpdate.get("\$rat_singularity_cache") as SectorEntityToken? ?: return false

        if (crate.isExpired) return false

        if (Global.getSector().intelManager.hasIntelOfClass(AbyssCrateIntel::class.java)) return false

        return true
    }

}