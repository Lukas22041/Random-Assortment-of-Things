package assortment_of_things.abyss.terrain

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssTerrainPlugin() : HyperspaceTerrainPlugin() {

    var id = Misc.genUID()

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        //super.applyEffect(entity, days)

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity as CampaignFleetAPI
            val inCloud = this.isInClouds(fleet)
            val tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius())
            var cell: CellStateTracker? = null
            if (tile != null) {
                cell = activeCells[tile[0]][tile[1]]
            }

            fleet.stats.addTemporaryModMult(0.1f, this.modId + "abyss_1", "Abyss", 2f, fleet.stats.sensorRangeMod)

            if (isInClouds(fleet))
            {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_1", "In abyssal fog", 1.5f, fleet.stats.fleetwideMaxBurnMod)
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_2", "In abyssal fog", 1.5f, fleet.stats.accelerationMult)
            }

            if (cell != null && cell.isStorming && !Misc.isSlowMoving(fleet)) {

                applyStormStrikes(cell, fleet, days)
            }
        }
    }

    override fun getModId(): String {
        return super.getModId()
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        val player = Global.getSector().playerFleet
        if (player.containingLocation != entity.containingLocation)  return

        val inCloud = this.isInClouds(player)

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)


        tooltip!!.addPara("The strong forces of the depths of hyperspace are impossible to resist without proper shielding. " +
                "" +
                "Staying within it requires x amount of x per day. If the fleet runs out of it, it gets forced back in to hyperspace." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor())
        tooltip.addSpacer(5f)

        tooltip!!.addPara("The abyssal matter appears strengthen radio waves emitted by the fleet, increasing the Sensor Range by 100%%" +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sensor Range", "100%")

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
            tooltip!!.addPara("Abyssal Storms damage the fleets ships if they are moving quickly, making it best to avoid passing through them when possible." +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")
        }

    }



    override fun getTerrainName(): String {
        val player = Global.getSector().playerFleet
        val inCloud = this.isInClouds(player)



        if (isInStorm(player)) return "Abyssal Storm"
        if (inCloud) return "Abyssal Fog"
        return "Abyss"
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
}