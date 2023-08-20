package assortment_of_things.abyss.intel

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*


class AbyssCrateIntel(var crate: SectorEntityToken) : BaseIntelPlugin() {


    var system = crate.starSystem

    override fun getName(): String? {
        return "Lost Shipment"
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {


        if (crate != null && !crate.isExpired) {
            info.addPara("You managed to acquire information of a rediscovered shipment that dates back to the domain-era and has likely been untouched since.\n\n" +
                    "It is located in the ${crate.containingLocation.nameWithNoType} system", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
                "domain-era", "${crate.containingLocation.nameWithNoType} system")
        }
        else {
            info.addPara("You recovered the contents of the lost shipment.", 0f)
        }


    }

    override fun getIcon(): String? {
        return "graphics/icons/cache0.png"
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("Abyss")
        return tags
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        if (crate != null && !crate.isExpired) return crate
        return system.center

    }



}