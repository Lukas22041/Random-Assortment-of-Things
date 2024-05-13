package assortment_of_things.abyss.weapons

import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class SeraphLensflareEffect : BaseCombatLayeredRenderingPlugin(CombatEngineLayers.ABOVE_SHIPS_LAYER), EveryFrameWeaponEffectPlugin {


    var flare1: SpriteAPI? = null
    var flare2: SpriteAPI? = null
    var flare3: SpriteAPI? = null

    var weapon: WeaponAPI? = null

    var addedRenderer = false

    init {
        var path = "graphics/fx/rat_seraph_lensflare.png"
        Global.getSettings().loadTexture(path)

        flare1 = Global.getSettings().getSprite(path)
        flare2 = Global.getSettings().getSprite(path)
        flare3 = Global.getSettings().getSprite(path)
    }

    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {
        if (!addedRenderer) {
            engine!!.addLayeredRenderingPlugin(this)
            addedRenderer = true
        }
        this.weapon = weapon
    }

    override fun getRenderRadius(): Float {
        return 100000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if (weapon == null || flare1 == null) return

        var ship = weapon!!.ship

        var color = Color(242, 48, 65)

        var point = weapon!!.location


        var overwrite = ship.customData.get("rat_lensflare_level_overwrite") as Float? ?: 0f

        if (ship.system.canBeActivated())
        if (!ship.system.isActive && overwrite < 0.01) return
        var alphaMult = ship.system.effectLevel

        if (overwrite > 0.01 && ship.system.state != ShipSystemAPI.SystemState.IN && ship.system.state != ShipSystemAPI.SystemState.ACTIVE) {
            alphaMult = overwrite
        }


        var point1 = MathUtils.getRandomPointInCircle(point, MathUtils.getRandomNumberInRange(0f, 1.5f))
        var point2 = MathUtils.getRandomPointInCircle(point, MathUtils.getRandomNumberInRange(0f, 1.5f))
        var point3 = MathUtils.getRandomPointInCircle(point, MathUtils.getRandomNumberInRange(0f, 1.5f))

        flare1!!.color = color
        flare2!!.color = color
        flare3!!.color = color

        flare1!!.setAdditiveBlend()
        flare2!!.setAdditiveBlend()
        flare3!!.setAdditiveBlend()


        var extraWidth = 0f
        var extraHeight = 0f
        if (ship.baseOrModSpec().hullId == "rat_gabriel") {
            extraWidth += 0
            extraHeight += 0
        }

        flare1!!.alphaMult = 0.3f * alphaMult
        flare2!!.alphaMult = 0.3f * alphaMult
        flare3!!.alphaMult = 0.4f * alphaMult

        flare1!!.setSize(50f + extraWidth, 7f + extraHeight)
        flare2!!.setSize(50f + extraWidth, 5f + extraHeight)
        flare3!!.setSize(25f + extraWidth, 2f + extraHeight)

        flare1!!.angle = ship.facing + 90
        flare2!!.angle = ship.facing + 90
        flare3!!.angle = ship.facing + 90

        flare1!!.renderAtCenter(point1.x, point1.y)
        flare2!!.renderAtCenter(point2.x, point2.y)
        flare3!!.renderAtCenter(point3.x, point3.y)






        flare1!!.alphaMult = 0.2f * alphaMult
        flare2!!.alphaMult = 0.2f * alphaMult
        flare3!!.alphaMult = 0.3f * alphaMult

        flare1!!.setSize(50f, 7f)
        flare2!!.setSize(50f, 5f)
        flare3!!.setSize(25f, 2f)

        flare1!!.angle = ship.facing
        flare2!!.angle = ship.facing
        flare3!!.angle = ship.facing

        flare1!!.renderAtCenter(point1.x, point1.y)
        flare2!!.renderAtCenter(point2.x, point2.y)
        flare3!!.renderAtCenter(point3.x, point3.y)

    }
}