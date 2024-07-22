package assortment_of_things

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import java.util.*

class ShaderTestRenderer : LunaCampaignRenderingPlugin {

    var distortion = Global.getSettings().getAndLoadSprite("graphics/fx/genesis_weapon_glow.png")
    var vertex = Global.getSettings().loadText("data/shaders/testVertex.shader")
    var fragment = Global.getSettings().loadText("data/shaders/testFragment2.shader")
    var shader: Int = ShaderLib.loadShader(vertex, fragment)

    init {
        if (shader == 0) {
            var test = ""
        }

        GL20.glUseProgram(shader)

        GL20.glUniform1i(GL20.glGetUniformLocation(shader, "screen"), 0)
        GL20.glUniform1i(GL20.glGetUniformLocation(shader, "distortion"), 1)

        GL20.glUseProgram(0)
    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(advance: Float) {

    }

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }



    override fun render(p0: CampaignEngineLayers?, p1: ViewportAPI?) {
        var loc = Global.getSector().playerFleet.location

        //distortion.renderAtCenter(loc.x, loc.y)

        ShaderLib.beginDraw(shader)


        //Bind Screen Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

        //Bind Distortion Texture
        GL13.glActiveTexture( GL13.GL_TEXTURE0 + 1)
        distortion.bindTexture()

        GL11.glDisable(GL11.GL_BLEND);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0)
        ShaderLib.exitDraw()
    }

}