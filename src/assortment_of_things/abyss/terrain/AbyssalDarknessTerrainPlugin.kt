package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.*
import assortment_of_things.abyss.entities.hyper.AbyssalFracture
import assortment_of_things.abyss.entities.light.AbyssalBeacon
import assortment_of_things.abyss.entities.light.AbyssalColossalPhotosphere
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.entities.light.AbyssalPhotosphere
import assortment_of_things.abyss.entities.primordial.PrimordialPhotosphere
import assortment_of_things.abyss.procgen.biomes.EtherealShores
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.state.AppDriver
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.bounty.ui.drawOutlined
import org.magiclib.kotlin.setAlpha
import second_in_command.misc.getHeight
import java.awt.Color
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
    var biomeText:LazyFont.DrawableString? = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)

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

    //Dont execute code from superclasses if your not in the system
    override fun advance(amount: Float) {
        var currentsystem = entity?.containingLocation ?: return
        if (Global.getSector().playerFleet.containingLocation != currentsystem) return
        super.advance(amount)
    }

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        var data = AbyssUtils.getData()
        var system = AbyssUtils.getSystem()

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
            biomeText = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
        }

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        for (source in lightsources)
        {
            var loc = Vector2f(source.location.x * factor, source.location.y * factor)

            var plugin = source.customPlugin as AbyssalLight
            var radius = plugin.radius * factor
            if (plugin is AbyssalBeacon) radius = (plugin.baseRadius + plugin.extraRadius) * factor
            var color = plugin.lightColor

            var skipRendering = false
            var isStenceling = false
            if (plugin is PrimordialPhotosphere) {
                var biome = AbyssUtils.getBiomeManager().getBiome("primordial_waters") as PrimordialWaters
                var level = biome.getLevel()
                if (level > 0 && level < 1) {
                    isStenceling = true
                    biome.startStencil(true)
                } else if (level <= 0) {
                    skipRendering = true
                }
            }

            if (!skipRendering) {
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

            if (isStenceling) GL11.glDisable(GL11.GL_STENCIL_TEST)

        }

        for (entity in system!!.customEntities) {


            if (entity.customEntitySpec.id == "rat_abyss_fracture") {
                var plugin = entity.customPlugin
                if (plugin !is AbyssalFracture) continue

                var destination = plugin.connectedEntity?.containingLocation ?: continue

                var destinationName = destination.nameWithNoType ?: continue
                fractureText!!.text = destinationName
                fractureText!!.fontSize = 600f * factor
                fractureText!!.baseColor = AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

                fractureText!!.drawOutlined(entity.location.x * factor - (fractureText!!.width / 2), (entity.location.y + 600) * factor + (fractureText!!.height))
            }

            if (entity.customEntitySpec.id == "rat_abyss_sensor" || entity.customEntitySpec.id == "rat_decaying_abyss_sensor") {

                if (!entity.hasTag("scanned") && !Global.getSettings().isDevMode) continue

                var plugin = entity.customPlugin as AbyssSensorEntity
                var biome = plugin.biome ?: continue

                fractureText!!.text = biome.getDisplayName()
                fractureText!!.fontSize = 600f * factor
                fractureText!!.baseColor = biome.getTooltipColor().setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

                var loc = entity.location
                var height = 700f
                if (entity.orbitFocus != null) {
                    loc = entity.orbitFocus.location
                    if (entity.orbitFocus.customPlugin is AbyssalColossalPhotosphere) {
                        height += 700f
                    }
                }

                fractureText!!.drawOutlined(loc.x * factor - (fractureText!!.width / 2), (loc.y + height) * factor + (fractureText!!.height))
            }

            if (entity.customEntitySpec.id == "rat_abyss_primordial_photosphere") {

                if (!entity.customEntitySpec.isShowIconOnMap) continue

                var plugin = entity.customPlugin as PrimordialPhotosphere
                var biome = plugin.biome ?: continue

                fractureText!!.text = biome.getDisplayName()
                fractureText!!.fontSize = 600f * factor
                fractureText!!.baseColor = biome.getTooltipColor().setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

                fractureText!!.drawOutlined(entity.location.x * factor - (fractureText!!.width / 2), (entity.location.y + 700) * factor + (fractureText!!.height))
            }

            if (entity.customEntitySpec.id == "rat_abyss_photosphere_sierra") {

                if (entity.sensorProfile == null) continue

                var plugin = entity.customPlugin as AbyssalPhotosphere
                var biome = AbyssUtils.getBiomeManager().getBiome(EtherealShores::class.java) ?: continue

                fractureText!!.text = biome.getDisplayName()
                fractureText!!.fontSize = 600f * factor
                fractureText!!.baseColor = biome.getTooltipColor().setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

                fractureText!!.drawOutlined(entity.location.x * factor - (fractureText!!.width / 2), (entity.location.y + 700) * factor + (fractureText!!.height))
            }
        }



    }

    override fun renderOnRadar(radarCenter: Vector2f, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)

        //Global.getSettings().setFloat("campaignRadarRadius", 8000f)

        var data = AbyssUtils.getData() ?: return
        var system = AbyssUtils.getSystem() ?: return
        var primordial = AbyssUtils.getBiomeManager().getBiome("primordial_waters") as PrimordialWaters

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
            biomeText = font!!.createText("", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
        }

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        val radarRadius = Global.getSettings().getFloat("campaignRadarRadius") + 2000
        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }

        for (sources in lightsources)
        {
            var plugin = sources.customPlugin as AbyssalLight

            var extra = 0f
            if (sources.customPlugin is AbyssalColossalPhotosphere) extra += 3500
           /* if (plugin is AbyssalColossalPhotosphere) {
                var primLevel = primordial.getLevel()
                *//* if (primLevel > 0 && primLevel < 0.5) {
                     var level = primLevel.levelBetween(0f, 0.25f)
                     radius *= 1 -(0.5f*level)
                 }
                 if (primLevel > 0.5 && primLevel < 1) {
                     var level = primLevel.levelBetween(1f, 0.755f)
                     radius *= 1 -(0.5f*level)
                 }*//*

                *//*if (primLevel > 0f && primLevel < 1f) {
                    extra = 0f
                }*//*
            }*/
            if (MathUtils.getDistance(sources.location, radarCenter) >= radarRadius + extra) continue



            var loc = Vector2f((sources.location.x - radarCenter.x) * factor, (sources.location.y - radarCenter.y) * factor)



            var radius = plugin.radius * factor

            var color = plugin.lightColor
            //if (plugin.radius >= 52000) continue

            var skipRendering = false
            var fadeIn = 1f
            if (plugin is PrimordialPhotosphere) {
                var biome = AbyssUtils.getBiomeManager().getBiome("primordial_waters") as PrimordialWaters
                var level = biome.getLevel()
                if (level > 0 && level < 1) {
                    fadeIn = biome.getLevel()
                    //biome.startStencil(true)
                } else if (level <= 0) {
                    skipRendering = true
                }
            }

            if (!skipRendering) {
                halo!!.alphaMult = 1f * alphaMult * fadeIn
                halo!!.color = color.setAlpha(75)

                halo!!.setSize(radius / 20, radius / 20)
                halo!!.setAdditiveBlend()
                halo!!.renderAtCenter(loc.x, loc.y)

                halo!!.alphaMult = 1f * alphaMult
                halo!!.color = color.setAlpha(55)

                halo!!.setSize(radius / 2f, radius / 2f)
                halo!!.setAdditiveBlend()
                halo!!.renderAtCenter(loc.x, loc.y)
            }

            //if (isStenceling) GL11.glDisable(GL11.GL_STENCIL_TEST)

        }

        for (entity in system.customEntities) {

            if (MathUtils.getDistance(entity.location, radarCenter) >= radarRadius) continue

            if (entity.customEntitySpec.id == "rat_abyss_fracture") {
                var plugin = entity.customPlugin
                if (plugin !is AbyssalFracture) continue

                var destination = plugin.connectedEntity?.containingLocation ?: continue

                var destinationName = destination.nameWithNoType ?: continue
                fractureText!!.text = destinationName
                fractureText!!.fontSize = 800f * factor
                fractureText!!.baseColor = AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA
                fractureText!!.drawOutlined((entity.location.x - radarCenter.x) * factor - (fractureText!!.width / 2), (entity.location.y - radarCenter.y + 800) * factor + (fractureText!!.height))
            }

            /*if (entity.customEntitySpec.id == "rat_abyss_sensor" || entity.customEntitySpec.id == "rat_decaying_abyss_sensor") {

                if (!entity.hasTag("scanned")) continue

                var plugin = entity.customPlugin as AbyssSensorEntity
                var biome = plugin.biome ?: continue

                fractureText!!.text = biome.getDisplayName()
                fractureText!!.fontSize = 800f * factor
                fractureText!!.baseColor = biome.getTooltipColor().setAlpha((255 * alphaMult).toInt())
                fractureText!!.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
                fractureText!!.blendSrc = GL11.GL_SRC_ALPHA

                fractureText!!.drawOutlined((entity.location.x - radarCenter.x) * factor - (fractureText!!.width / 2), (entity.location.y - radarCenter.y + 600) * factor + (fractureText!!.height))

            }*/

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

    fun getLightlevel(target: SectorEntityToken) : Float {
        //var point = Global.getSector().playerFleet.location
        var point = target.location
        var system = entity.starSystem
        var data = AbyssUtils.getData()

        var highestMult = 0f
        var inAny = false
        var lightsources = entity.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        for (source in lightsources)
        {
            var plugin = source.customPlugin as AbyssalLight


            var maxRadius = (plugin.radius / 10) + 10 + (target.radius / 2)
            var minRadius = maxRadius * 0.85f

            var distance = MathUtils.getDistance(source.location, point)

            if (plugin is PrimordialPhotosphere) {
                var biome = AbyssUtils.getBiomeManager().getBiome("primordial_waters") as PrimordialWaters
                var range = biome.getRadius()
                var centerDist = MathUtils.getDistance(target, biome.getStencilCenter())
                if (centerDist >= range + target.radius/2) continue
            }

            if (distance < maxRadius) {
                inAny = true
                var level = (distance - minRadius) / (maxRadius - minRadius)
                if (source.customEntitySpec?.id == "rat_abyss_decaying_photosphere") level = MathUtils.clamp(level, 0.5f, 1f)
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

    //Doesnt really need to be dependent on the entities position, as there would rarely be a case where the difference would matter
    fun getDarknessMult() : Float {
        var mananger = AbyssUtils.getBiomeManager()
        var levels = mananger.getBiomeLevels()

        var mult = 1f

        for ((biome, level) in levels) {
            var baseDarknessMult = biome.getMaxDarknessMult()
            //var negation = (1 - baseDarknessMult) * level

            var negation = 1-baseDarknessMult

            mult *= baseDarknessMult + negation * (1-level)  /* - negation*/
            var test = ""
        }

        return mult
    }

    fun getDarknessName(darknessMult: Float) : String {

        name = "Darkness"

        /*if (darknessMult <= 0.75f) {
            name = "Darkness"
        }*/

        if (darknessMult <= 0.65f) {
            name = "Extreme Darkness"
        }
        if (darknessMult <= 0.55f) {
            name = "Total Darkness"
        }
        return name
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        super.applyEffect(entity, days)
        var system = entity!!.starSystem

        var data = AbyssUtils.getData()

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity

            var lightLevel = getLightlevel(fleet) // 1 At total darkness

            //if (lightLevel != 0f) {

                var mult = getDarknessMult()

                var name = getDarknessName(mult)

                var current = 1 - ((1 - mult) * lightLevel)

                fleet.stats.addTemporaryModMult(0.1f, this.toString(), "$name", current, fleet.stats.detectedRangeMod)

                /*if (depth == AbyssDepth.Shallow) {
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Darkness", 1 - (0.25f * lightLevel), fleet.stats.detectedRangeMod)

                }

                if (depth == AbyssDepth.Deep) {
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Extreme Darkness", 1 -(0.50f * lightLevel), fleet.stats.detectedRangeMod)
                }

                if (AbyssalDepthsEventIntel.get()?.isStageActive(AbyssalDepthsEventIntel.Stage.IN_THE_DARK) == true) {
                    fleet.stats.addTemporaryModMult(0.1f, id,  "In the Dark",  1 - (0.10f * lightLevel), fleet.stats.detectedRangeMod)
                }*/
           // }
        }
    }

    override fun getTerrainName(): String {
        var system = entity.starSystem

        var name = getDarknessName(getDarknessMult())
        var mult = (getLightlevel(Global.getSector().playerFleet) * 100).toInt()

        return "$name ($mult%)"
    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun isTooltipExpandable(): Boolean {
        return false
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        //super.createTooltip(tooltip, expanded)

        var system = entity.starSystem

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)

        var dominant = AbyssUtils.getBiomeManager().getDominantBiome()
        var maxDarkness = 100-(dominant.getMaxDarknessMult() * 100).toInt()
        var mult = getDarknessMult()

        tooltip!!.addPara("The density of the abyssal matter makes barely any radiation able to get past it. Decreases the Sensor Detection range by up to $maxDarkness%%" +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Detection", "$maxDarkness%")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("Sensor Darkness", Alignment.MID, 0f)
        tooltip.addSpacer(5f)

        tooltip.addPara("No known maps of this location exists, combined with the limited sensor range, much of the map is uncharted. " +
                "Your fleet will document more about the enviroment as explore it.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "map")


        tooltip.addSpacer(5f)

    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (vignette == null) {
            vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")
        }
        if (layer == CampaignEngineLayers.ABOVE) {
            var playerfleet = Global.getSector().playerFleet
            if (entity.containingLocation == playerfleet.containingLocation) {

                var levels = AbyssUtils.getBiomeManager().getBiomeLevels()
                var biomeVignetteLevel = levels.map { it.key.getVignetteLevel() * it.value }.sum()


                var darknessMult = getDarknessMult()
                var level = 1 - darknessMult.levelBetween(0.5f, 0.75f)

                var offset = 400f - (200 * level) //Closer in darker biomes

                vignette.alphaMult = (0.85f + (0.05f * level)) * biomeVignetteLevel //Darker in darker biomes
                if (RATSettings.brighterAbyss!!) vignette.alphaMult = 0.6f + (0.05f * level)
                vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                vignette.render(viewport!!.llx - (offset / 2), viewport!!.lly - (offset / 2))

            }

        }
    }
}