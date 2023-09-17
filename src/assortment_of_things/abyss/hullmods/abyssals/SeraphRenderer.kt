package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.FaderUtil
import org.magiclib.kotlin.setAlpha
import java.util.*

class SeraphRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin(CombatEngineLayers.ABOVE_SHIPS_LAYER) {

    var fader = FaderUtil(1f, 2f, 2f, false, false)
    var sprite: SpriteAPI? = null

    private var baseGlowAlpha = 0.5f
    private var additiveGlowAlpha = 0.5f
    private var blink: Boolean = false
    private var lowest = 0.05f

    init {
        var path = ship.hullSpec.spriteName.replace(".png", "") + "_glow.png"

        Global.getSettings().loadTexture(path)
        sprite = Global.getSettings().getSprite(path)

       // enableBlink()
    }

    fun enableBlink() { blink = true }
    fun disableBlink() { blink = false }

    fun configureBlink(lowest: Float, inDuration: Float, outDuration: Float)
    {
        this.lowest = lowest
        fader.setDuration(inDuration, outDuration)
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        fader.advance(amount)
        if (fader.brightness >= 1 && blink)
        {
            fader.fadeOut()
        }
        else if (fader.brightness <= lowest)
        {
            fader.fadeIn()
        }
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        if (sprite == null) return
        if (!ship.isAlive || ship.isHulk) return

        sprite!!.angle = ship.facing + 270
        //sprite!!.color = c

       /* sprite!!.alphaMult = baseGlowAlpha
        sprite!!.setNormalBlend()
        sprite!!.renderAtCenter(ship.location.x, ship.location.y)*/

        var extra = 0.2f * ship.system.effectLevel

        sprite!!.setAdditiveBlend()
        sprite!!.alphaMult = (additiveGlowAlpha + extra) * fader.brightness
        sprite!!.renderAtCenter(ship.location.x, ship.location.y)

    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun getRenderRadius(): Float {
        return 10000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER)
    }

}