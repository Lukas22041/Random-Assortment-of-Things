package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SpriteWithShader(var texture: String, var vertex: String, var fragment: String) {

    var sprite: SpriteAPI = Global.getSettings().getAndLoadSprite(texture)
    var alphaMult = 1f
    var width = sprite.width
    var height = sprite.height
    var angle = 0f
    var shader = ShaderLib.loadShader(vertex, fragment)

    init {

    }

    fun renderAtCenter(x: Float, y: Float) {
       render(x - width / 2.0f, y - height / 2.0f)
    }

    fun render(vec: Vector2f) {
        render(vec.x, vec.y)
    }

    fun render(x: Float, y: Float) {

        var color = Color.white

        var texX = 0f
        var texY = 0f
        var texWidth = sprite.textureWidth
        var texHeight = sprite.textureHeight

        GL20.glUseProgram(shader)

        GL11.glPushMatrix()
        //sprite.bindTexture()

       // GL11.glColor4ub(color.getRed().toByte(), color.getGreen().toByte(), color.getBlue().toByte(), (color.getAlpha().toFloat() * alphaMult).toInt().toByte())

      /*  GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)*/


      /*  GL11.glTranslatef(x, y, 0.0f)

        if (sprite.centerX != -1.0f && sprite.centerY != -1.0f) {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-sprite.centerX, -sprite.centerY, 0.0f)
        } else {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-width / 2.0f, -height / 2.0f, 0.0f)
        }*/





        GL11.glDisable(GL11.GL_TEXTURE_2D)
       GL11.glRectf(x, y, x + width, y + height)

       /* GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(texX, texY)
        GL11.glVertex2f(0.0f, 0.0f)
        GL11.glTexCoord2f(texX, texY + sprite.textureHeight)
        GL11.glVertex2f(0.0f, height)
        GL11.glTexCoord2f(texX + sprite.textureWidth, texY + sprite.textureHeight)
        GL11.glVertex2f(width, height)
        GL11.glTexCoord2f(texX + sprite.textureWidth, texY)
        GL11.glVertex2f(width, 0.0f)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnd()*/


        GL11.glPopMatrix()
        GL20.glUseProgram(0)

    }

}