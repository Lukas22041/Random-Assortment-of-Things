package assortment_of_things.abyss.terrain.fog

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.TerrainAIFlags
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PrimordialWatersTerrain() : BaseFogTerrain() {

    var id = Misc.genUID()

    var color = AbyssUtils.ABYSS_COLOR

    var manager = AbyssUtils.getBiomeManager()

    override fun advance(amount: Float) {
        var currentsystem = entity?.containingLocation ?: return
        if (Global.getSector().playerFleet.containingLocation != currentsystem) return
        super.advance(amount)
    }


    /*override fun getRenderColor(): Color {
        return getBiome()?.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
    }*/

    override fun getRenderColor(): Color {
        return color
    }

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        var biome = getBiome()
        if (biome !is PrimordialWaters) return

        var level = biome.getLevel()

        if (level > 0 && level < 1) {
            //Inner
            color = biome.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) biome.getDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            biome.startStencil(false)
            super.renderOnMap(factor, alphaMult)
            biome.endStencil()

            //Outer
            color = biome.getInactiveDarkBiomeColor().setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) biome.getInactiveDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            biome.startStencil(true)
            super.renderOnMap(factor, alphaMult)
            biome.endStencil()

            renderBorder(biome)
        }

        else if (level <= 0f) {
            color = biome.getInactiveDarkBiomeColor().setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) color = biome.getInactiveDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            super.renderOnMap(factor, alphaMult)
        }
        else if (level >= 1f) {
            color = biome.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) color = biome.getDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            super.renderOnMap(factor, alphaMult)
        }

    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        var biome = getBiome()
        if (biome !is PrimordialWaters) return

        var level = biome.getLevel()

        if (level > 0 && level < 1) {
            //Inner
            color = biome.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) biome.getDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            biome.startStencil(false)
            super.render(layer, viewport)
            biome.endStencil()

            //Outer
            color = biome.getInactiveDarkBiomeColor().setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) biome.getInactiveDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            biome.startStencil(true)
            super.render(layer, viewport)
            biome.endStencil()

            renderBorder(biome)
        }

        else if (level <= 0f) {
            color = biome.getInactiveDarkBiomeColor().setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) color = biome.getInactiveDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            super.render(layer, viewport)
        }
        else if (level >= 1f) {
            color = biome.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            if (RATSettings.brighterAbyss!!) color = biome.getDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
            super.render(layer, viewport)
        }



    }

    fun renderBorder(biome: PrimordialWaters) {
        var c = biome.getBiomeColor().setAlpha(33)

        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        var center = biomePlugin?.deepestCells?.first()

        var radius = biome.getRadius()
        val x = center!!.getWorldCenter().x
        val y = center.getWorldCenter().y
        var points = 100


        for (i in 0..points) {
            val angle: Double = (2 * Math.PI * i / points)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }


        GL11.glEnd()
        GL11.glPopMatrix()
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        //super.applyEffect(entity, days)

        return //Handle effects from Central Terrain Plugin to avoid duplicate storm strikes etc

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

        tooltip.addPara("A unique location within hyperspace. Discovered with the settlement of the sector, and quickly exploited for its unique resources. " +
                "Post-collapse humanity however did not have the resources for working within this hostile environment, making it quasi isolated from the sector since.", 0f)

        tooltip.addSpacer(5f)

        tooltip!!.addPara("Due to a unique interaction between the fleet's sensors and the abyssal matter, it is possible to detect structures at further distances than normal, but without any identifying data. " +
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
            tooltip!!.addPara("The charged particles within the fog combine with the exhaust of the fleet's engines, increasing the burn speed by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "burn speed", "50%")
        }

        if (isInStorm(player))
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Storm", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Abyssal Storms may damage members of the fleet if it is moving through it at high speeds."
                , 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")
          /*  tooltip!!.addPara("Abyssal Storms damage the fleet's ships if they are moving quickly. \n\n" +
                    "They emit a unique type of wavelength that travels far through abyssal matter, making it possible to track their collision " +
                    "with large objects, with the site of impact showing up on the map. " +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage", "wavelength", "map")*/
        }

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