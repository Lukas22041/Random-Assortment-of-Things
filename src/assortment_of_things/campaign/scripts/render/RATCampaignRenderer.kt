package assortment_of_things.campaign.scripts.render

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import java.util.*

class RATCampaignRenderer : LunaCampaignRenderingPlugin {

    companion object {
        fun getInstance() : RATCampaignRenderer {

            var renderer = LunaCampaignRenderer.getRendererOfClass(RATCampaignRenderer::class.java) as RATCampaignRenderer?
            if (renderer == null) {
                    renderer = RATCampaignRenderer()
                    LunaCampaignRenderer.addRenderer(renderer)
            }

            return renderer!!
        }

        fun getAfterimageRenderer() = getInstance().afterimageRenderer
        fun getGlowsRenderer() = getInstance().lensflareRenderer

    }

    private var afterimageRenderer = RATCampaignAfterimageRenderer()
    private var lensflareRenderer = RATCampaignGlowRenderer()

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {
        getAfterimageRenderer().advanceAfterimages(amount)
        getGlowsRenderer().advance(amount)
    }

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        var set = EnumSet.noneOf(CampaignEngineLayers::class.java)
        set.addAll(CampaignEngineLayers.values())
        return set
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {

        getAfterimageRenderer().renderAfterimages(layer, viewport)

        if (layer == CampaignEngineLayers.ABOVE) {
            getGlowsRenderer().render(layer, viewport)
        }
    }



}