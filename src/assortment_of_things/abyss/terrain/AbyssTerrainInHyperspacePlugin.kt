package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssBorder
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.entities.AbyssalLight
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.bounty.ui.drawOutlined
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssTerrainInHyperspacePlugin() : OldHyperspaceTerrainPlugin() {

    var id = Misc.genUID()

    @Transient
    var background = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2.jpg")

    @Transient
    var vignette = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkness_vignette.png")

    @Transient
    var halo: SpriteAPI? = null

    var range = 3000f
    var maxRange = 3500f * 1.4f
    var centerClearRadius = 550f

    var color = Color.getHSBColor(0.04f, 1f, 0.3f)

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        var combined = super.getActiveLayers()
        combined.addAll(EnumSet.of(CampaignEngineLayers.TERRAIN_2, CampaignEngineLayers.ABOVE))
        return combined
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI) {
        super.render(layer, viewport)

        var level = getLevel()
        if (level <= 0) return

        if (background == null || vignette == null) {
            background = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2.jpg")
            vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")
        }

        if (layer == CampaignEngineLayers.TERRAIN_2) {


            var width = viewport.visibleWidth
            var height = viewport.visibleHeight

            var x = viewport.llx
            var y = viewport.lly

            background.setSize(width, height)
            background.color = color
            background.alphaMult = level
            background.render(x, y)

        }

        if (layer == CampaignEngineLayers.ABOVE) {
            var playerfleet = Global.getSector().playerFleet
            if (entity.containingLocation == playerfleet.containingLocation) {

                var offset = 400f


                vignette.alphaMult = 0.9f
                if (RATSettings.brighterAbyss!!) vignette.alphaMult = 0.6f
                //vignette.alphaMult = 0.4f
                vignette.alphaMult *= level

                vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                vignette.render(viewport!!.llx - (offset / 2), viewport!!.lly - (offset / 2))

            }

        }

    }

    fun getLevel() : Float {
        var distance = MathUtils.getDistance(Global.getSector().playerFleet, entity)
        var level = (distance - range) / (maxRange - range)
        level = level.coerceIn(0f, 1f)
        level = 1 - level
        return level
    }

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        super.renderOnMap(factor, alphaMult)

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
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


    }

    override fun renderOnRadar(radarCenter: Vector2f, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)


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


    }

    override fun stacksWithSelf(): Boolean {
        return true
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

    override fun advance(amount: Float) {
        var currentsystem = entity?.containingLocation ?: return
        if (Global.getSector().playerFleet.containingLocation != currentsystem) return



        super.advance(amount)
    }


    override fun getRenderColor(): Color {
        return color
    }

    fun save()
    {
        params.tiles = null
        savedTiles = encodeTiles(tiles)

        savedActiveCells.clear()

        for (i in activeCells.indices) {
            for (j in activeCells[0].indices) {
                val curr = activeCells[i][j]
                if (curr != null && isTileVisible(i, j)) {
                    savedActiveCells.add(curr)
                }
            }
        }
    }




    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        //super.applyEffect(entity, days)

        var currentsystem = entity?.containingLocation ?: return
        if (Global.getSector().playerFleet.containingLocation != currentsystem) return

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity as CampaignFleetAPI
            val inCloud = this.isInClouds(fleet)
            val tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius())
            var cell: CellStateTracker? = null
            if (tile != null) {
                cell = activeCells[tile[0]][tile[1]]
            }

           // fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Abyss", 1.5f, fleet.stats.sensorRangeMod)

            if (isInClouds(fleet))
            {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_1", "In abyssal fog", 1.5f, fleet.stats.fleetwideMaxBurnMod)
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_2", "In abyssal fog", 1.5f, fleet.stats.accelerationMult)

                if (cell != null && cell.isSignaling && cell.signal < 0.2f) {
                    cell.signal = 0f
                }

                var goDark = Global.getSector().playerFleet.getAbility(Abilities.GO_DARK)
                var goDarkActive = false

                if (goDark != null) {
                    goDarkActive = goDark.isActive
                }

                if (cell != null && cell.isStorming && !Misc.isSlowMoving(fleet) && !goDarkActive) {

                    applyStormStrikes(cell, fleet, days)
                }
            }


        }
    }

    override fun getModId(): String {
        return super.getModId() + id
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        val player = Global.getSector().playerFleet
        if (player.containingLocation != entity.containingLocation)  return

        val inCloud = this.isInClouds(player)

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)

        tooltip.addPara("A unique location within hyperspace. Discovered with the settlement of the sector, and quicky exploited for its unique resources. " +
                "Post-collapse humanity however did not have the resources for working within this hostile enviroment, making it quasi isolated from the sector since.", 0f)

        tooltip.addSpacer(5f)

        tooltip!!.addPara("Due to a unique interaction between the fleets sensors and the abyssal matter, it is possible to detect structures at further distances than normal, but without any identifying data. " +
                "" +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "detect", "structures")

        tooltip.addSpacer(5f)

        tooltip.addPara("The Transverse Jump ability can not be used due to spatial interference. However the unique bending of the surrounding space allows the exit of this location by flying outside of its perceived bounds. This area is marked by a circle on the tripads map.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "Transverse Jump", "exit", "map")

        if (isInClouds(player))
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Fog", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("The charged particles within the fog combine with the exhaust of the fleets engines, increasing the burn speed by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "burn speed", "50%")
        }

        if (isInStorm(player))
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Storm", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Abyssal Storms may damage members of the fleet if it is moving through it at high speeds."
                , 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")
          /*  tooltip!!.addPara("Abyssal Storms damage the fleets ships if they are moving quickly. \n\n" +
                    "They emitt a unique type of wavelength that travels far through abyssal matter, making it possible to track their collission " +
                    "with large objects, with the site of impact showing up on the map. " +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage", "wavelength", "map")*/
        }

    }



    override fun getTerrainName(): String {
        val player = Global.getSector().playerFleet
        val inCloud = this.isInClouds(player)

        if (getLevel() <= 0.5f)  {
            return ""
        }

        if (isInStorm(player)) return "Abyssal Storm"
        if (inCloud) return "Abyssal Fog"
        return "Abyssal Depths"
    }

    fun isInStorm(entity: SectorEntityToken) : Boolean
    {
        val tile = getTilePreferStorm(entity.location, entity.radius)
        var cell: CellStateTracker? = null
        if (tile != null) {
            cell = activeCells[tile[0]][tile[1]]
        }
        if (cell != null && cell.isStorming) return true
        return false
    }

    override fun isTooltipExpandable(): Boolean {
        return false
    }

    //Cant have multiple systems with the terrain without this
    override fun getTerrainId(): String {
        return super.getTerrainId() + id
    }

    override fun hasAIFlag(flag: Any?): Boolean {
       // return super.hasAIFlag(flag)
        return false
    }

    override fun hasAIFlag(flag: Any?, fleet: CampaignFleetAPI?): Boolean {
        if (flag == TerrainAIFlags.MOVES_FLEETS) return true
        return false
    }

}