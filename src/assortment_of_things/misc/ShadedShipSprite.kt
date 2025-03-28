package assortment_of_things.misc

import assortment_of_things.campaign.items.AICoreSpecialItemPlugin
import assortment_of_things.campaign.items.AICoreSpecialItemPlugin.Companion
import com.fs.graphics.Sprite
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lwjgl.opengl.GL20
import java.awt.Color

//Experimental
abstract class ShadedShipSprite(var ship: ShipAPI) : Sprite() {

    fun init() {
        var sprite = ReflectionUtils.invoke("getSprite", ship.spriteAPI)
        var texture = ReflectionUtils.invoke("getTexture", sprite!!)

        if (texture != null) {
            ReflectionUtils.invoke("setTexture", this, texture)

            var width =  ReflectionUtils.get("width", sprite) as Float
            var height =  ReflectionUtils.get("height", sprite) as Float
            var texX =  ReflectionUtils.get("texX", sprite) as Float
            var texY =  ReflectionUtils.get("texY", sprite) as Float
            var texWidth =  ReflectionUtils.get("texWidth", sprite) as Float
            var texHeight =  ReflectionUtils.get("texHeight", sprite) as Float
            var angle =  ReflectionUtils.get("angle", sprite) as Float
            var color =  ReflectionUtils.get("color", sprite) as Color
            var alphaMult =  ReflectionUtils.get("alphaMult", sprite) as Float
            var centerX =  ReflectionUtils.get("centerX", sprite) as Float
            var centerY =  ReflectionUtils.get("centerY", sprite) as Float
            var offsetX =  ReflectionUtils.get("offsetX", sprite) as Int
            var offsetY =  ReflectionUtils.get("offsetY", sprite) as Int
            var blendSrc =  ReflectionUtils.get("blendSrc", sprite) as Int
            var blendDest =  ReflectionUtils.get("blendDest", sprite) as Int
            var texClamp =  ReflectionUtils.get("texClamp", sprite) as Boolean

            this.setSize(width, height)
            this.setCenter(centerX, centerY)
            this.setOffset(offsetX, offsetY)

            this.setTexX(texX)
            this.setTexX(texY)
            this.setTexWidth(texWidth)
            this.setTexHeight(texHeight)

            this.setBlendFunc(blendSrc, blendDest)

            this.alphaMult = alphaMult
            this.setAngle(angle)

            this.setColor(color)
            this.setTexClamp(texClamp)


            ReflectionUtils.set("sprite", ship, this)
        }
    }

    abstract fun applyShader() : Int

    override fun render(var1: Float, var2: Float) {

        var shader = applyShader()

        GL20.glUseProgram(shader)

        GL20.glUniform1f(GL20.glGetUniformLocation(shader, "alphaMult"), alphaMult)
        GL20.glUniform3f(GL20.glGetUniformLocation(shader, "colorMix"), color.red / 255f, color.green / 255f, color.blue / 255f)

        super.render(var1, var2)

        GL20.glUseProgram(0)
    }
}