package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.misc.addPara
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.TerrainAIFlags
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssTerrainPlugin() : OldHyperspaceTerrainPlugin() {

    var id = Misc.genUID()

    @Transient
    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")

    @Transient
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var font: LazyFont? = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)

   /* override fun isInAbyss(other: SectorEntityToken?): Boolean {
        return false
    }*/

    override fun advance(amount: Float) {
        var currentsystem = entity?.containingLocation ?: return
        if (Global.getSector().playerFleet.containingLocation != currentsystem) return



        super.advance(amount)
    }


    override fun getRenderColor(): Color {
        return AbyssUtils.ABYSS_COLOR.darker().darker()
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