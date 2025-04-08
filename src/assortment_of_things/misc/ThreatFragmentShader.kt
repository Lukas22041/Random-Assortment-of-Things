package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.awt.Color

class ThreatFragmentShader(ship: ShipAPI) : ShipSpriteDelegate(ship)
{

    var shader = 0

    var overlay = Global.getSettings().getAndLoadSprite("graphics/fx/rat_fragment_tex.png")

    override fun preRender() {

    }

    override fun postRender() {

        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_test_replacement_shader2.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)
            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "overlayTex"), 1)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }

        GL11.glPushMatrix()

        GL20.glUseProgram(shader)

        var viewport = Global.getCombatEngine().viewport

        GL20.glUniform2f(GL20.glGetUniformLocation(shader, "resolution"), this.texWidth, this.texHeight)

        GL20.glUniform1f(GL20.glGetUniformLocation(shader, "alphaMult"), ship.alphaMult)
        GL20.glUniform3f(GL20.glGetUniformLocation(shader, "colorMix"), ship.spriteAPI.color.red / 255f, ship.spriteAPI.color.green / 255f, ship.spriteAPI.color.blue / 255f)

        //Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ship.spriteAPI.textureId)

        //Replacement
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlay!!.textureId)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)

        //Reset Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)

        ship.shield?.innerColor = Color(156,186,174,75)
        ship.shield?.ringColor = Color(255,255,255,255)
        ship.engineController.fadeToOtherColor(ship.id + "_fragment", Color(130,155,145, 100), Color(130,155,145,25), 1f, 0.85f)

        var c = ship.spriteAPI.color


        ship.addTag("skipSpriteDelegate")
        ship.spriteAPI.setNormalBlend()
        ship.spriteAPI.alphaMult = 1f
        ship.spriteAPI.color = Color(255, 255, 255, 255)
        ship.spriteAPI.renderAtCenter(0f, 0f) //0, 0 Because Alex does a GLTranslate before this render is called.
        ship.removeTag("skipSpriteDelegate")
        ship.spriteAPI.setNormalBlend()
        //ship.spriteAPI.color = c


        GL20.glUseProgram(0)

        GL11.glPopMatrix()


    }

}