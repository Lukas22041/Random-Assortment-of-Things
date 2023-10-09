package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssDepth
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*

class AbyssalDarknessTerrainPlugin : BaseTerrain() {

    @Transient
    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")

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

    override fun getTerrainId(): String {
        return super.getTerrainId() + id
    }

    fun containingPhotosphere(other: SectorEntityToken): SectorEntityToken? {
        var photospheres = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
        for (source in photospheres)
        {
            var plugin = source.customPlugin as AbyssalPhotosphere
            if (MathUtils.getDistance(source.location, other.location) < (plugin.radius / 10) - 10)
            {
                return source
            }
        }

        return null
    }

    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLightsource }
        var withinLight = false
        for (source in lightsources)
        {
            var plugin = source.customPlugin as AbyssalLightsource
            if (MathUtils.getDistance(source.location, point) < (plugin.radius / 10) - 10)
            {
                withinLight = true
                break
            }
        }

        var photospheres = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
        for (source in photospheres)
        {
            var plugin = source.customPlugin as AbyssalPhotosphere
            if (MathUtils.getDistance(source.location, point) < (plugin.radius / 10) - 10)
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

        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity

            if (depth == AbyssDepth.Shallow) {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Darkness", 0.75f, fleet.stats.detectedRangeMod)

            }

            if (depth == AbyssDepth.Deep) {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Extreme Darkness", 0.50f, fleet.stats.detectedRangeMod)
            }

        }
    }

    override fun getTerrainName(): String {
        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        if (depth == AbyssDepth.Shallow) return "Darkness"
        if (depth == AbyssDepth.Deep) return "Extreme Darkness"

        return ""
    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)

        if (depth == AbyssDepth.Shallow) {
            tooltip!!.addPara("The density of the abyssal matter makes barely any radiation able to get past it. Decreases the Sensor Detection range by 25%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "25%")
            tooltip.addSpacer(5f)
        }
        if (depth == AbyssDepth.Deep) {
            tooltip!!.addPara("The density of the abyssal matter causes any light or other type of radiation to diminish close to its source. Decreases the Sensor Detection range by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "50%")
            tooltip.addSpacer(5f)
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (vignette == null) {
            vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")
        }
        if (layer == CampaignEngineLayers.ABOVE) {
            var playerfleet = Global.getSector().playerFleet
            if (entity.containingLocation == playerfleet.containingLocation) {

                var offset = 400f

                vignette.alphaMult = 0.9f
                vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                vignette.render(viewport!!.llx - (offset / 2), viewport!!.lly - (offset / 2))

            }

        }
    }
}