package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.*
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
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

    @Transient
    var font: LazyFont? = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)

    @Transient
    var fractureText:LazyFont.DrawableString? = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)

    @Transient
    var exitText:LazyFont.DrawableString? = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)

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
            fractureText = font!!.createText("", system.getColor().setAlpha(255), 800f)
        }

        if (exitText == null) {
            exitText = font!!.createText("", system.getColor().setAlpha(255), 800f)
        }

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        var border = system.system.customEntities.find { it.customEntitySpec.id == "rat_abyss_border" } ?: return
        var plugin = border.customPlugin
        if (exitText != null && plugin is AbyssBorder) {
            var radius = plugin.radius * factor

            exitText!!.text = "Exit"
            exitText!!.fontSize = 600f * factor
            exitText!!.baseColor = system.getColor().setAlpha((255 * alphaMult).toInt())
            exitText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            exitText!!.blendSrc = GL11.GL_SRC_ALPHA

            exitText!!.drawOutlined(-exitText!!.width / 2, radius + (exitText!!.height / 2))
            exitText!!.drawOutlined(-exitText!!.width / 2, -radius + (exitText!!.height / 2))
            exitText!!.drawOutlined(-radius - (exitText!!.width / 2), exitText!!.height / 2)
            exitText!!.drawOutlined(radius - (exitText!!.width / 2), exitText!!.height / 2)
        }




        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        for (source in lightsources)
        {
            var loc = Vector2f(source.location.x * factor, source.location.y * factor)

            var plugin = source.customPlugin as AbyssalLight
            var radius = plugin.radius * factor
            var color = plugin.color

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
            fractureText!!.text = destinationName
            fractureText!!.fontSize = 400f * factor
            fractureText!!.baseColor = system.getColor().setAlpha((255 * alphaMult).toInt())
            fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

            fractureText!!.drawOutlined(fracture.location.x * factor - (fractureText!!.width / 2), (fracture.location.y + 600) * factor + (fractureText!!.height))

        }
    }

    override fun renderOnRadar(radarCenter: Vector2f, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)

        var system = AbyssUtils.getSystemData(entity.starSystem) ?: return


        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }


        val radarRadius = Global.getSettings().getFloat("campaignRadarRadius") + 2000
        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }

        for (sources in lightsources)
        {
            if (MathUtils.getDistance(sources.location, radarCenter) >= radarRadius) continue

            var loc = Vector2f((sources.location.x - radarCenter.x) * factor, (sources.location.y - radarCenter.y) * factor)


            var plugin = sources.customPlugin as AbyssalLight
            var radius = plugin.radius * factor
            var color = plugin.color
            if (plugin.radius >= 50000) continue

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





        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("", system.getColor().setAlpha(255), 800f)
        }

        for (fracture in system.system.customEntities) {

            if (MathUtils.getDistance(fracture.location, radarCenter) >= radarRadius) continue

            if (fracture.customEntitySpec.id != "rat_abyss_fracture") continue
            var plugin = fracture.customPlugin
            if (plugin !is AbyssalFracture) continue

            var destination = plugin.connectedEntity?.containingLocation ?: continue
            if ((destination is StarSystemAPI && !destination.isEnteredByPlayer)) continue

            var destinationName = destination.nameWithNoType ?: continue
            fractureText!!.text = destinationName
            fractureText!!.fontSize = 800f * factor
            fractureText!!.baseColor = system.getColor().setAlpha((255 * alphaMult).toInt())
            fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            fractureText!!.blendSrc = GL11.GL_SRC_ALPHA
            fractureText!!.drawOutlined((fracture.location.x - radarCenter.x) * factor - (fractureText!!.width / 2), (fracture.location.y - radarCenter.y + 800) * factor + (fractureText!!.height))
        }
    }


    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
       /* var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        var withinLight = false
        for (source in lightsources)
        {
            var plugin = source.customPlugin as AbyssalLight
            if (MathUtils.getDistance(source.location, point) < (plugin.radius / 10) - 10)
            {
                withinLight = true
                break
            }
        }

        return !withinLight*/
        return true
    }

    fun getDarknessMult() : Float {
        var point = Global.getSector().playerFleet.location
        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        var highestMult = 0f
        var inAny = false
        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        for (source in lightsources)
        {
            var plugin = source.customPlugin as AbyssalLight

            var maxRadius = (plugin.radius / 10) + 10 + (Global.getSector().playerFleet.radius / 2)
            var minRadius = maxRadius * 0.85f

            var distance = MathUtils.getDistance(source.location, point)
            if (distance < maxRadius) {
                inAny = true
                var level = (distance - minRadius) / (maxRadius - minRadius)
                if (level >= highestMult) {
                    highestMult = level
                }
            }
        }

        if (!inAny) {
            return 1f
        }

        return highestMult
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        super.applyEffect(entity, days)
        var system = entity!!.starSystem

        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity

            var darknessMult = getDarknessMult()

            if (darknessMult != 0f) {
                if (depth == AbyssDepth.Shallow) {
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Darkness", 1 - (0.25f * darknessMult), fleet.stats.detectedRangeMod)

                }

                if (depth == AbyssDepth.Deep) {
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Extreme Darkness", 1 -(0.50f * darknessMult), fleet.stats.detectedRangeMod)
                }

                if (AbyssalDepthsEventIntel.get()?.isStageActive(AbyssalDepthsEventIntel.Stage.IN_THE_DARK) == true) {
                    fleet.stats.addTemporaryModMult(0.1f, id,  "In the Dark",  1 - (0.10f * darknessMult), fleet.stats.detectedRangeMod)
                }
            }
        }
    }

    override fun getTerrainName(): String {
        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        var mult = (getDarknessMult() * 100).toInt()


        if (depth == AbyssDepth.Shallow) return "Darkness ($mult%)"
        if (depth == AbyssDepth.Deep) return "Extreme Darkness ($mult%)"

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
            tooltip!!.addPara("The density of the abyssal matter makes barely any radiation able to get past it. Decreases the Sensor Detection range by up to 25%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "25%")
            tooltip.addSpacer(5f)
        }
        if (depth == AbyssDepth.Deep) {
            tooltip!!.addPara("The density of the abyssal matter causes any light or other type of radiation to diminish close to its source. Decreases the Sensor Detection range by up to 50%%" +
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