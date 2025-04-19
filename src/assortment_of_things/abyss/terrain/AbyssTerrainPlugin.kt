package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin.CellStateTracker
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.util.vector.Vector2f
import java.util.EnumSet

class AbyssTerrainPlugin : BaseTerrain() {

    override fun containsEntity(other: SectorEntityToken?): Boolean {
        return true
    }

    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
        return true
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

    var layer = EnumSet.of(CampaignEngineLayers.ABOVE)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layer
    }


    fun isAnyInCloud(entityToken: SectorEntityToken) : Boolean {
        var manager = AbyssUtils.getBiomeManager()
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {
                if ((biome.terrain as BaseFogTerrain).isInClouds(entityToken)) {
                    return true
                }
            }
        }

        return false
    }

    fun getAnyTilePreferStorm(loc: Vector2f, radius: Float) : CellStateTracker? {
        var manager = AbyssUtils.getBiomeManager()

        var any: CellStateTracker? = null
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {

                var tile = (biome.terrain as BaseFogTerrain).getTilePreferStorm(loc, radius)
                var cell: CellStateTracker? = null
                if (tile != null) {
                    cell = (biome.terrain as BaseFogTerrain).activeCells.get(tile[0]).get(tile[1])
                }

                if (cell?.isStorming == true) {
                    return cell
                }

                any = cell //Return non storming
            }
        }

        return any
    }

    override fun getEffectCategory(): String {
        return "rat_abyss_terrain"
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        if (entity is CampaignFleetAPI) return


    }

    override fun getTerrainName(): String {
        var name = ""
        var player = Global.getSector().playerFleet

        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)

        name = dominant.getDisplayName()

        if (cell?.isStorming == true) name += " (Storm)"
        else if (inCloud) name += " (Fog)"

        return name
    }


    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        var player = Global.getSector().playerFleet

        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()

        var name = dominant.getDisplayName()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)
        var isStorming = cell?.isStorming == true

        tooltip.addTitle(name)
        tooltip.addSpacer(10f)

        dominant.addBiomeTooltip(tooltip)

        //Fog & Storm stuff here
    }
}