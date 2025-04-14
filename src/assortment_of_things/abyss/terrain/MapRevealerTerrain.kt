package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import java.util.*

class MapRevealerTerrain : BaseTerrain() {

    @Transient
    var fog: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/rat_abyss_fog2.png")

    override fun renderOnMap(factor: Float, alphaMult: Float) {

        if (fog == null) {
            fog = Global.getSettings().getAndLoadSprite("graphics/fx/rat_abyss_fog2.png")
        }

        fog!!.setSize(AbyssBiomeManager.cellSize * 1.8f * factor, AbyssBiomeManager.cellSize * 1.8f * factor)

        var index = 0

        if (Global.getSettings().isDevMode) {
            for (cell in AbyssUtils.getData().biomeManager.getCells()) {
                index += 1
                if (!cell.isDiscovered) {

                    var alpha = 1f

                    //if (cell.getAdjacent().any { it.isDiscovered }) alpha = 0.6f
                    if (cell.isPartialyDiscovered) alpha = 0.6f

                    //var loc = cell.getWorldCenter()
                    var loc = cell.getWorldCenter()
                    fog!!.angle = cell.renderAngle
                    fog!!.alphaMult = alphaMult * alpha
                    fog!!.renderAtCenter(loc.x * factor, loc.y * factor)
                }
            }
        }



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