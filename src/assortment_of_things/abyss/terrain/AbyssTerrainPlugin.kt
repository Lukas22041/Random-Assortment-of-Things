package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.MiscReplacement
import assortment_of_things.abyss.terrain.AbyssTerrainInHyperspacePlugin.SpeedBoostScript
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin.CellStateTracker
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.EnumSet

class AbyssTerrainPlugin : BaseTerrain() {

    var data = AbyssUtils.getData()
    var manager = AbyssUtils.getBiomeManager()

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
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {
                if ((biome.terrain as BaseFogTerrain).isInClouds(entityToken)) {
                    return true
                }
            }
        }

        return false
    }

    fun getAnyTilePreferStorm(loc: Vector2f, radius: Float) : Pair<CellStateTracker?, BaseFogTerrain> {
        var any: Pair<CellStateTracker?, BaseFogTerrain>? = null
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {

                var tile = (biome.terrain as BaseFogTerrain).getTilePreferStorm(loc, radius)
                var cell: CellStateTracker? = null
                if (tile != null) {
                    cell = (biome.terrain as BaseFogTerrain).activeCells.get(tile[0]).get(tile[1])
                }

                if (cell?.isStorming == true) {
                    return Pair(cell, biome.terrain as BaseFogTerrain)
                }

                any = Pair(cell, biome.terrain as BaseFogTerrain) //Return non storming
            }
        }

        return any!!
    }

    override fun getEffectCategory(): String {
        return "rat_abyss_terrain"
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        if (entity !is CampaignFleetAPI) return
        var fleet = entity

        var isInclouds = isAnyInCloud(entity)
        if (isInclouds) {

            var cell = getAnyTilePreferStorm(fleet.location, fleet.radius)

            if (fleet.isPlayerFleet) {
                if (!fleet.hasScriptOfClass(SpeedBoostScript::class.java)) {
                    fleet.addScript(SpeedBoostScript(
                        fleet,this))
                }
            } else {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_1", "In abyssal fog", 1.5f, fleet.stats.fleetwideMaxBurnMod)
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_2", "In abyssal fog", 1.5f, fleet.stats.accelerationMult)
            }

            if (cell.first != null && cell.first!!.isSignaling && cell.first!!.signal < 0.2f) {
                cell.first!!.signal = 0f
            }

            var goDark = Global.getSector().playerFleet.getAbility(Abilities.GO_DARK)
            var goDarkActive = false

            if (goDark != null) {
                goDarkActive = goDark.isActive
            }

            if (cell.first?.isStorming == true && !MiscReplacement.isSlowMoving(fleet) && !goDarkActive) {

                cell.second.applyStormStrikes(cell.first, fleet, days)
            }
        }

    }

    override fun getTerrainName(): String {
        var name = ""
        var player = Global.getSector().playerFleet

        var dominant = manager.getDominantBiome()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)

        name = dominant.getDisplayName()

        if (cell.first?.isStorming == true) name += " (Storm)"
        else if (inCloud) name += " (Fog)"

        return name
    }


    override fun hasTooltip(): Boolean {
        return true
    }

    override fun isTooltipExpandable(): Boolean {
        return false
    }

    override fun getNameColor(): Color {
        var dominant = manager.getDominantBiome()
        //return manager.getCurrentTooltipColor()
        return dominant.getTooltipColor()
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        var player = Global.getSector().playerFleet

        var dominant = manager.getDominantBiome()

        var name = dominant.getDisplayName()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)
        var isStorming = cell.first?.isStorming == true

        tooltip.addTitle(name)
        tooltip.addSpacer(10f)

        //Fog & Storm stuff here

        dominant.addBiomeTooltip(tooltip)
        tooltip.addSpacer(10f)

        if (inCloud)
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Fog", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("The charged particles within the fog combine with the exhaust of the fleet's engines, increasing the burn speed by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "burn speed", "50%")
        }

        if (isStorming)
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Storm", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Abyssal Storms may damage members of the fleet if it is moving through it at high speeds."
                , 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")

        }
    }

    class SpeedBoostScript(var fleet: CampaignFleetAPI, var terrain: AbyssTerrainPlugin) : EveryFrameScript {

        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return false
        }

        override fun advance(amount: Float) {

            var inCloud = false
            if (AbyssUtils.isPlayerInAbyss()) {
                inCloud = terrain.isAnyInCloud(fleet)
            }

            if (inCloud) {
                fleet.stats.fleetwideMaxBurnMod.modifyMult(terrain.modId + "fog_1", 1.5f, "In abyssal fog")
                fleet.stats.accelerationMult.modifyMult(terrain.modId + "fog_2", 1.5f, "In abyssal fog")
            } else {
                fleet.stats.fleetwideMaxBurnMod.unmodify(terrain.modId + "fog_1")
                fleet.stats.accelerationMult.unmodify(terrain.modId + "fog_2")
            }
        }
    }
}