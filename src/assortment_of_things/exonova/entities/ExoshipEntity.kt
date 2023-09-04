package assortment_of_things.exonova.entities

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ExoshipEntity : BaseCustomEntityPlugin() {


    @Transient
    var sprite: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/backgrounds/exo/exospace.jpg")

    var speed = 0f

    override fun getRenderRange(): Float {
        return 100000000f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (sprite == null) {
            sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/exo/exospace.jpg")
        }

        if (speed > 100000f) {
            speed = 0f
        }

       // renderScrollingTexture(sprite!!, entity.location, Vector2f(500f, 500f))

    }

    fun renderScrollingTexture(sprite: SpriteAPI, location: Vector2f, size: Vector2f) {

        if (!Global.getSector().isPaused) {
            speed += 0.01f
        }

        var color = Color(255, 255, 255)
        var alphaMult = 1f

        var posX = location.x
        var posY = location.y

        var width = size.x
        var height = size.y
        var angle = 0f

        var texX = 0f
        var texY = 0f

        GL11.glPushMatrix()
        sprite!!.bindTexture()
        GL11.glColor4ub(color.getRed().toByte(),
            color.getGreen().toByte(),
            color.getBlue().toByte(),
            (color.getAlpha().toFloat() * alphaMult).toInt().toByte())
        GL11.glTranslatef(posX + 0, posY + 0, 0.0f)
        if (sprite!!.centerX != -1.0f && sprite!!.centerY != -1.0f) {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-sprite!!.centerX, -sprite!!.centerY, 0.0f)
        } else {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-width / 2.0f, -height / 2.0f, 0.0f)
        }

        GL11.glEnable(3553)
        GL11.glEnable(3042)
        GL11.glEnable(GL11.GL_TEXTURE_WRAP_T)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        var var3 = 0.001f
        var3 = 0.0f
        GL11.glBegin(7)
        GL11.glTexCoord2f(texX + var3, texY + var3 + speed)
        GL11.glVertex2f(0.0f, 0.0f)
        GL11.glTexCoord2f(texX + var3, texY + sprite!!.textureHeight - var3 + speed)
        GL11.glVertex2f(0.0f, height)
        GL11.glTexCoord2f(texX + sprite!!.textureWidth - var3, texY + sprite!!.textureHeight - var3 + speed)
        GL11.glVertex2f(width, height)
        GL11.glTexCoord2f(texX + sprite!!.textureWidth - var3, texY + var3 + speed)
        GL11.glVertex2f(width, 0.0f)
        GL11.glEnd()
        GL11.glDisable(3042)
        GL11.glPopMatrix()
    }

}