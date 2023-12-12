package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.PositionAPI

class SpriteBackgroundPanel(var background: String) : BaseCustomUIPanelPlugin() {

    var sprite: SpriteAPI = Global.getSettings().getAndLoadSprite(background);
    var position: PositionAPI? = null

    override fun positionChanged(position: PositionAPI?) {
        super.positionChanged(position)
        this.position = position
    }

    override fun renderBelow(alphaMult: Float) {
        if (position == null) return

        sprite.setSize(position!!.width, position!!.height + 100)
        sprite.alphaMult = alphaMult
        sprite.render(position!!.x, position!!.y - 100)
    }

}