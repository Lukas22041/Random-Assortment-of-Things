package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.impl.combat.dweller.WarpingSpriteRendererUtilV2
import java.util.*

class MapRevealerTerrain : BaseTerrain() {

    @Transient
    var fog: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/rat_abyss_fog2.png")

    companion object {
        //Toggled active by the sensor interaction, should not be active normaly, as the radar bug out
        var isAboveMode = false
    }

    override fun advance(amount: Float) {

    }

    override fun renderOnMapAbove(factor: Float, alphaMult: Float) {
        if (isAboveMode) {
            render(factor, alphaMult)
        }
    }

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        if (!isAboveMode) {
            render(factor, alphaMult)
        }
    }

    fun render(factor: Float, alphaMult: Float) {
        if (fog == null) {
            fog = Global.getSettings().getAndLoadSprite("graphics/fx/rat_abyss_fog2.png")
        }

        fog!!.setSize(AbyssBiomeManager.cellSize * 2.1f * factor, AbyssBiomeManager.cellSize * 2.1f * factor)

        if (AbyssUtils.isShowFog()) {
            for (cell in AbyssUtils.getData().biomeManager.getCells()) {
                if (!cell.isDiscovered || cell.discoveryFader > 0f) {

                    //var alpha = 1f * (cell.discoveryFader * cell.discoveryFader)

                    //if (cell.getAdjacent().any { it.isDiscovered }) alpha = 0.6f
                    /*if (cell.isPartialyDiscovered && cell.discoveryFader >= 0.99) alpha = 0.6f + (0.4f * cell.partialDiscoveryFader * cell.partialDiscoveryFader)*/

                    var alpha = easeInOutSine(0.6f * cell.discoveryFader) + easeInOutSine(0.4f * cell.partialDiscoveryFader)

                    //var loc = cell.getWorldCenter()
                    var loc = cell.getWorldCenter()
                    fog!!.angle = cell.renderAngle
                    fog!!.alphaMult = alphaMult * alpha

                    fog!!.renderAtCenter(loc.x * factor, loc.y * factor)

                }
            }
        }
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    override fun getRenderRange(): Float {
        return 100000000000f
    }

    override fun getEffectCategory(): String {
        return "rat_map_revealer"
    }

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

}