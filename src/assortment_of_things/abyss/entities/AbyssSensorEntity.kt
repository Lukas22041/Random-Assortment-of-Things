package assortment_of_things.abyss.entities

import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssSensorEntity : BaseCustomEntityPlugin() {

    var biome: BaseAbyssBiome? = null

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI, level: SectorEntityToken.VisibilityLevel) {
        if (biome != null) {
            tooltip.addSpacer(10f)
            tooltip.addPara("This array monitors the general area of the ${biome!!.getDisplayName()}", 0f,
                Misc.getTextColor(), biome!!.getTooltipColor(), "${biome!!.getDisplayName()}")
        }
    }

}