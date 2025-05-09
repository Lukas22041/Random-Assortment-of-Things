package assortment_of_things.artifacts.ui

import assortment_of_things.artifacts.ArtifactSpec
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc

class ArtifactTooltip(var artifact: ArtifactSpec?, var addInstallDesc: Boolean) : TooltipCreator {
    override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
        return false
    }

    override fun getTooltipWidth(tooltipParam: Any?): Float {
        return 450f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {

        if (artifact == null) {

            tooltip.addTitle("Artifacts")

            tooltip.addSpacer(10f)

            tooltip.addPara("Artifacts are extraordinary devices that can be found throughout the sector. They can provide a fleet with unique benefits, but due to their complexity only one can be active at a time.")
            tooltip.addSpacer(10f)

            var hasAnyArtifact = ArtifactUtils.getArtifactsInFleet().isNotEmpty()
            if (!hasAnyArtifact) {
                tooltip.addPara("You have no artifacts within your fleets inventory.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
            } else {
                tooltip.addPara("Click to integrate an artifact.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
            }

            return
        }

        var plugin = ArtifactUtils.getPlugin(artifact!!)

        tooltip.addTitle(artifact!!.name, Misc.getBasePlayerColor())

        tooltip.addSpacer(10f)

        var designType = artifact!!.designType
        var designColor = Misc.getDesignTypeColor(designType)

        tooltip.addPara("Design type: $designType", 0f, Misc.getGrayColor(), designColor, designType)

        tooltip.addSpacer(10f)

        var desc = Global.getSettings().getSpecialItemSpec("rat_artifact").desc
        tooltip.addPara(desc, 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "Fleet")

        tooltip.addSpacer(10f)

        plugin.addDescription(tooltip)

        if (addInstallDesc) {
            tooltip.addSpacer(10f)
            tooltip.addPara("Click to integrate another artifact.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        } else {
            tooltip.addSpacer(10f)
            var active = ArtifactUtils.getActiveArtifact()

            if (active?.id == artifact!!.id) {
                tooltip.addPara("Click to remove this artifact.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
            } else {
                tooltip.addPara("Click to integrate this artifact.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
            }
        }

    }
}