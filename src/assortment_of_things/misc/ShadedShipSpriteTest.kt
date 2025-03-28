package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

class ShadedShipSpriteTest(ship: ShipAPI) : ShadedShipSprite(ship)
{

    var shader = 0

    var replacement = Global.getSettings().getAndLoadSprite("graphics/ships/onslaught/onslaught_hegemony.png")

    var minRadius = -400f
    var maxRadius = 800f

    override fun applyShader(): Int {

        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_test_replacement_shader.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)
            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "replacementTex"), 1)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }

        GL20.glUseProgram(shader)

        var viewport = Global.getCombatEngine().viewport

        GL20.glUniform2f(GL20.glGetUniformLocation(shader, "resolution"), this.texWidth, this.texHeight)

        GL20.glUniform1f(GL20.glGetUniformLocation(shader, "minRadius"), ShaderLib.unitsToUV(minRadius) * viewport.viewMult)
        GL20.glUniform1f(GL20.glGetUniformLocation(shader, "maxRadius"), ShaderLib.unitsToUV(maxRadius) * viewport.viewMult)


        //Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ship.spriteAPI.textureId)

        //Replacement
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, replacement!!.textureId)

        //Reset Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)

        GL20.glUseProgram(0)

        return shader
    }

}