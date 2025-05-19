package assortment_of_things.abyss.terrain.fog

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.MiscReplacement
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.TerrainAIFlags
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.ArrayList

class SeaOfTranquilityTerrain() : BaseFogTerrain() {

    var id = Misc.genUID()

    var color = AbyssUtils.ABYSS_COLOR

    override fun advance(amount: Float) {
        super.advance(amount)
    }


    /*override fun getRenderColor(): Color {
        return getBiome()?.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
    }*/

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        super.renderOnMap(factor, alphaMult)
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        //super.applyEffect(entity, days)

        return //Handle effects from Central Terrain Plugin to avoid duplicate storm strikes etc


        if (!AbyssUtils.isPlayerInAbyss()) {
            return
        }

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

               /* if (fleet.isPlayerFleet) {
                    *//*if (!fleet.hasScriptOfClass(SpeedBoostScript::class.java)) {
                        fleet.addScript(SpeedBoostScript(fleet, this))
                    }*//*
                } else {
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_1", "In abyssal fog", 1.5f, fleet.stats.fleetwideMaxBurnMod)
                    fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_2", "In abyssal fog", 1.5f, fleet.stats.accelerationMult)
                }*/



                if (cell != null && cell.isSignaling && cell.signal < 0.2f) {
                    cell.signal = 0f
                }

                var goDark = Global.getSector().playerFleet.getAbility(Abilities.GO_DARK)
                var goDarkActive = false

                if (goDark != null) {
                    goDarkActive = goDark.isActive
                }

                if (cell != null && cell.isStorming && !MiscReplacement.isSlowMoving(fleet) && !goDarkActive) {

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