package assortment_of_things.exotech.terrain

import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class ExotechHyperNebula() : OldHyperspaceTerrainPlugin() {

    var id = Misc.genUID()

    @Transient
    var background = Global.getSettings().getAndLoadSprite("graphics/backgrounds/exo/exospace.jpg")

    @Transient
    var vignette = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkness_vignette.png")

    @Transient
    var halo: SpriteAPI? = null

    var range = 1300f
    var levelMinRange = 200f
    var levelMaxRange = 1300f
    var centerClearRadius = 350f

   /* var color = Color.getHSBColor(0.04f, 1f, 0.3f)*/

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
            background = Global.getSettings().getAndLoadSprite("graphics/backgrounds/exo/exospace.jpg")
            vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette.png")
        }

        if (layer == CampaignEngineLayers.TERRAIN_2) {


            var width = viewport.visibleWidth
            var height = viewport.visibleHeight

            var x = viewport.llx
            var y = viewport.lly

            background.setSize(width, width)
            //background.color = color
            background.alphaMult = level * 0.2f
            background.render(x, y)

        }

        if (layer == CampaignEngineLayers.ABOVE) {
            var playerfleet = Global.getSector().playerFleet
            if (entity.containingLocation == playerfleet.containingLocation) {

                var offset = 400f


                vignette.alphaMult = 0.5f
                //vignette.alphaMult = 0.4f
                vignette.alphaMult *= level

                vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                vignette.render(viewport!!.llx - (offset / 2), viewport!!.lly - (offset / 2))

            }

        }

    }

    fun getLevel() : Float {
        var distance = MathUtils.getDistance(Global.getSector().playerFleet, entity)
        var level = (distance - levelMinRange) / (levelMaxRange - levelMinRange)
        level = level.coerceIn(0f, 1f)
        level = 1 - level
        return level
    }

    override fun renderOnMap(factor: Float, alphaMult: Float) {

    }

    override fun renderOnRadar(radarCenter: Vector2f, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)


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


        if (isInClouds(Global.getSector().playerFleet)) {
            for (member in Global.getSector().playerFleet.fleetData.membersListCopy) {
                if (member.repairTracker.cr > 0f) {
                    member.repairTracker.cr -= 0.015f * amount
                }
            }
        }

        super.advance(amount)
    }


    override fun getRenderColor(): Color {
        return Color(130, 64, 1)
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

        tooltip.addPara("An intensily radiated dust formation left from some kind of catastrophic event. Staying inside will rapidly damage all ships within the fleet. Avoid extended stay at all cost.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage","Avoid extended stay at all cost")

        tooltip.addSpacer(5f)


    }


    override fun getTerrainName(): String {
        val player = Global.getSector().playerFleet
        val inCloud = this.isInClouds(player)

        if (inCloud) return "Ionised Dust"
        return ""
    }

    override fun getNameColor(): Color {
        return Color(247, 173, 12)
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