package assortment_of_things.campaign.plugins.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import java.awt.Color


class DimensionalTearEntity : BaseCustomEntityPlugin()
{

    @Transient
    private var sprite: SpriteAPI? = Global.getSettings().getSprite("rat", "dimensional_tear_core");

    @Transient
    private var sprite2: SpriteAPI? = Global.getSettings().getSprite("rat", "dimensional_tear");

    var ogHeight: Float = 50.0f
    var ogWidth: Float = 50.0f

    var wormholeAlpha = 200
    var wormholeSizeMult = 0.5f
    var wormholeRotationSpeedMult = 1f
    var wormholeColor = Color(0, 150, 250, 255)

    var teleportLocation: SectorEntityToken? = null

    init {
        if (sprite != null)
        {
            ogHeight = sprite!!.height
            ogWidth = sprite!!.width
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {

        if (sprite == null) sprite = Global.getSettings().getSprite("rat", "dimensional_tear_core");
        if (sprite2 == null) sprite2 = Global.getSettings().getSprite("rat", "dimensional_tear");

        var color = Color(wormholeColor.red, wormholeColor.green, wormholeColor.blue, wormholeAlpha)

        sprite!!.renderAtCenter(entity.location.x, entity.location.y)
        sprite!!.color = color

        sprite2!!.renderAtCenter(entity.location.x, entity.location.y)
        sprite2!!.color = color

        if (Global.getSector().isPaused()) return
        sprite!!.angle -= 0.2f * wormholeRotationSpeedMult
        sprite2!!.angle -= 0.1f * wormholeRotationSpeedMult

        sprite!!.setSize(ogWidth * wormholeSizeMult, ogHeight * wormholeSizeMult)
        sprite2!!.setSize(ogWidth * wormholeSizeMult, ogHeight * wormholeSizeMult)
    }
}