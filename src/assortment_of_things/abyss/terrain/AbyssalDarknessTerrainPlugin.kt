package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*

class AbyssalDarknessTerrainPlugin : BaseTerrain() {

    var id = Misc.genUID()

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 100000f
    }

    override fun getEffectCategory(): String {
        return "rat_abyss_darkness"
    }

    override fun getModId(): String {
        return super.getModId() + id
    }

    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
        var system = entity.starSystem
        var tier = AbyssUtils.getTier(system)
        if (tier == AbyssProcgen.Tier.Low) return false

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLightsource }
        var withinLight = false
        for (source in lightsources)
        {
            var plugin = source.customPlugin as AbyssalLightsource
            if (MathUtils.getDistance(source.location, point) < (plugin.radius / 10) + 100)
            {
                withinLight = true
                break
            }
        }

        var photospheres = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
        for (source in photospheres)
        {
            var plugin = source.customPlugin as AbyssalPhotosphere
            if (MathUtils.getDistance(source.location, point) < (plugin.radius / 10) + 100)
            {
                withinLight = true
                break
            }
        }

        return !withinLight
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        super.applyEffect(entity, days)
        var system = entity!!.starSystem
        var tier = AbyssUtils.getTier(system)

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity

            if (tier == AbyssProcgen.Tier.Mid) {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Darkness", 0.75f, fleet.stats.detectedRangeMod)

            }

            if (tier == AbyssProcgen.Tier.High) {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Extreme Darkness", 0.50f, fleet.stats.detectedRangeMod)
            }

        }
    }

    override fun getTerrainName(): String {
        var system = entity.starSystem
        var tier = AbyssUtils.getTier(system)

        if (tier == AbyssProcgen.Tier.Low) return ""
        if (tier == AbyssProcgen.Tier.Mid) return "Darkness"
        if (tier == AbyssProcgen.Tier.High) return "Extreme Darkness"

        return ""
    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        var system = entity.starSystem
        var tier = AbyssUtils.getTier(system)

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)

        if (tier == AbyssProcgen.Tier.Mid) {
            tooltip!!.addPara("The density of the abyssal matter makes barely any light able to get past it. Decreases the Sensor Detection range by 25%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "25%")
            tooltip.addSpacer(5f)
        }
        if (tier == AbyssProcgen.Tier.High) {
            tooltip!!.addPara("The density of the abyssal matter causes any light to diminish close to its source. Decreases the Sensor Detection range by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "50%")
            tooltip.addSpacer(5f)
        }
    }
}