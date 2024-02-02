package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.bounty.ui.drawOutlined
import org.magiclib.kotlin.setAlpha
import java.util.*

class AbyssalDarknessTerrainPlugin : BaseTerrain() {

    @Transient
    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")

    @Transient
    var halo: SpriteAPI? = null

    var font: LazyFont? = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)



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

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        var system = AbyssUtils.getSystemData(entity.starSystem)

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
        }

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        var photospheres = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
        for (photosphere in photospheres)
        {
            var color = AbyssUtils.getSystemData(entity.starSystem).getColor()
            var loc = Vector2f(photosphere.location.x * factor, photosphere.location.y * factor)

            var plugin = photosphere.customPlugin as AbyssalPhotosphere
            var radius = plugin.radius * factor

            halo!!.alphaMult = 1f * alphaMult
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)

            halo!!.alphaMult = 1f * alphaMult
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)
        }

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLightsource }
        for (source in lightsources)
        {
            var color = AbyssUtils.getSystemData(entity.starSystem).getColor()
            var loc = Vector2f(source.location.x * factor, source.location.y * factor)

            var plugin = source.customPlugin as AbyssalLightsource
            var radius = plugin.radius * factor

            halo!!.alphaMult = 0.6f * alphaMult
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)

            halo!!.alphaMult = 0.8f * alphaMult
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)
        }

        for (fracture in system.system.customEntities) {
            if (fracture.customEntitySpec.id != "rat_abyss_fracture") continue
            var plugin = fracture.customPlugin
            if (plugin !is AbyssalFracture) continue

            var destination = plugin.connectedEntity?.containingLocation ?: continue
            if ((destination is StarSystemAPI && !destination.isEnteredByPlayer)) continue

            var destinationName = destination.nameWithNoType ?: continue
            var text = font!!.createText(destinationName, AbyssUtils.ABYSS_COLOR.setAlpha((225 * alphaMult).toInt()), 400f * factor)
            text.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            text.blendSrc = GL11.GL_SRC_ALPHA

            text.drawOutlined(fracture.location.x * factor - (text.width / 2), (fracture.location.y + 600) * factor + (text.height))

        }
    }

    override fun renderOnRadar(radarCenter: Vector2f, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)

        var system = AbyssUtils.getSystemData(entity.starSystem) ?: return

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }


        val radarRadius = Global.getSettings().getFloat("campaignRadarRadius") + 2000
        var photospheres = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }

        for (photosphere in photospheres)
        {
            if (MathUtils.getDistance(photosphere.location, radarCenter) >= radarRadius) continue

            var color = AbyssUtils.getSystemData(entity.starSystem).getColor()
            var loc = Vector2f((photosphere.location.x - radarCenter.x) * factor, (photosphere.location.y - radarCenter.y) * factor)


            var plugin = photosphere.customPlugin as AbyssalPhotosphere
            var radius = plugin.radius * factor

            halo!!.alphaMult = 1f * alphaMult
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)

            halo!!.alphaMult = 1f * alphaMult
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)
        }

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLightsource }
        for (source in lightsources)
        {
            if (MathUtils.getDistance(source.location, radarCenter) >= radarRadius) continue

            var color = AbyssUtils.getSystemData(entity.starSystem).getColor()
            var loc = Vector2f((source.location.x - radarCenter.x) * factor, (source.location.y - radarCenter.y) * factor)

            var plugin = source.customPlugin as AbyssalLightsource
            var radius = plugin.radius * factor

            halo!!.alphaMult = 0.8f * alphaMult
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)

            halo!!.alphaMult = 1f * alphaMult
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(loc.x, loc.y)
        }


        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
        }

        for (fracture in system.system.customEntities) {

            if (MathUtils.getDistance(fracture.location, radarCenter) >= radarRadius) continue

            if (fracture.customEntitySpec.id != "rat_abyss_fracture") continue
            var plugin = fracture.customPlugin
            if (plugin !is AbyssalFracture) continue

            var destination = plugin.connectedEntity?.containingLocation ?: continue
            if ((destination is StarSystemAPI && !destination.isEnteredByPlayer)) continue

            var destinationName = destination.nameWithNoType ?: continue
            var text = font!!.createText(destinationName, AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt()), 800f * factor)
            text.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            text.blendSrc = GL11.GL_SRC_ALPHA
            text.drawOutlined((fracture.location.x - radarCenter.x) * factor - (text.width / 2), (fracture.location.y - radarCenter.y + 800) * factor + (text.height))
        }
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
                if (RATSettings.brighterAbyss!!) vignette.alphaMult = 0.6f
                vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                vignette.render(viewport!!.llx - (offset / 2), viewport!!.lly - (offset / 2))

            }

        }
    }
}