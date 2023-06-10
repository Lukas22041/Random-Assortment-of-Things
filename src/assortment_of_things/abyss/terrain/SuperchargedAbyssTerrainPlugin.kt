package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignClock
import com.fs.starfarer.combat.CombatEngine
import lunalib.lunaDelegates.LunaMemory

class SuperchargedAbyssTerrainPlugin() : HyperspaceTerrainPlugin() {


    var id = Misc.genUID()
    var shielding: Float? by LunaMemory("rat_abyss_currentShielding", 50000f)
    var siphon: Float = 0f

    init {
    }

    override fun stacksWithSelf(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        var fleet = Global.getSector().playerFleet
        siphon = AbyssUtils.getSiphonPerDay()
        if (isInClouds(fleet))
        {
            shielding = shielding!! + siphon / CampaignClock.SECONDS_PER_GAME_DAY * amount
        }
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {

        if (entity is CampaignFleetAPI)
        {
            var fleet = entity as CampaignFleetAPI

            if (entity is CampaignFleetAPI)
            {
                var fleet = entity as CampaignFleetAPI
                val inCloud = this.isInClouds(fleet)
                val tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius())
                var cell: CellStateTracker? = null
                if (tile != null) {
                    cell = activeCells[tile[0]][tile[1]]
                }


                if (cell != null && cell.isStorming && !Misc.isSlowMoving(fleet)) {

                    applyStormStrikes(cell, fleet, days)
                }
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

        if (isInClouds(player))
        {
            tooltip!!.addPara("This area within the abyss holds strongly charged particles, because of this it can be used to siphon and process shielding material, if the fleet" +
                    " is equipped to do so.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "siphon", "shielding", "equipped")
        }

        if (isInStorm(player))
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Supercharged Abyssal Storm", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Abyssal Storms damage the fleets ships if they are moving quickly, making it best to avoid passing through them when possible." +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")
        }


    }

    override fun getTerrainName(): String {
        val player = Global.getSector().playerFleet
        val inCloud = this.isInClouds(player)

        if (isInStorm(entity)) return "Supercharged Abyssal Storm"
        if (inCloud) return "Supercharged Abyssal Fog"
        return ""
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