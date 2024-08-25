package assortment_of_things

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.math.sin

class ShaderTestRenderer : LunaCampaignRenderingPlugin {

    var distortion = Global.getSettings().getAndLoadSprite("graphics/fx/genesis_weapon_glow.png")
    var vertex = Global.getSettings().loadText("data/shaders/baseVertex.shader")
    var fragment = Global.getSettings().loadText("data/shaders/testFragment3.shader")
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

        GL11.glPushMatrix()


        //Link Shader
        GL20.glUseProgram(shader)




        //Render Screen and assign its texture to index 0
        beginDraw()
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

        GL11.glDisable(GL11.GL_BLEND);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0 + 0)
        exitDraw()
        //Screen Render End




        //Render Distortion

        renderSprite(distortion, loc, Vector2f(200f, 200f), 0f, 1f)

        //Distortion Render End



        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);







        //Unlink Shader
        GL20.glUseProgram(0)

        GL11.glPopMatrix()

    }

    fun renderSprite(sprite: SpriteAPI, loc: Vector2f, size: Vector2f, angle: Float, alphaMult: Float) {
        var color = Color.white

        var texX = 0f
        var texY = 0f
        var texWidth = sprite.textureWidth
        var texHeight = sprite.textureHeight

        var x = loc.x
        var y = loc.y

        var width = size.x
        var height = size.y




        //distortion.bindTexture()

        //sprite.bindTexture()

        GL11.glColor4ub(color.getRed().toByte(), color.getGreen().toByte(), color.getBlue().toByte(), (color.getAlpha().toFloat() * alphaMult).toInt().toByte())

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, distortion.textureId);

        GL11.glTranslatef(x, y, 0.0f)

        if (sprite.centerX != -1.0f && sprite.centerY != -1.0f) {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-sprite.centerX, -sprite.centerY, 0.0f)
        } else {
            GL11.glTranslatef(width / 2.0f, height / 2.0f, 0.0f)
            GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f)
            GL11.glTranslatef(-width / 2.0f, -height / 2.0f, 0.0f)
        }

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(texX, texY)
        GL11.glVertex2f(0.0f, 0.0f)
        GL11.glTexCoord2f(texX, texY + sprite.textureHeight)
        GL11.glVertex2f(0.0f, height)
        GL11.glTexCoord2f(texX + sprite.textureWidth, texY + sprite.textureHeight)
        GL11.glVertex2f(width, height)
        GL11.glTexCoord2f(texX + sprite.textureWidth, texY)
        GL11.glVertex2f(width, 0.0f)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnd()



    }

    var displayWidth = Global.getSettings().screenWidthPixels
    var displayHeight = Global.getSettings().screenHeightPixels

    //ShaderLib.beginDraw() without the program link
    fun beginDraw(/*shader: Int*/) {
        //GL20.glUseProgram(shader)
        GL11.glPushAttrib(1048575)
        GL11.glViewport(0,0,
            (Global.getSettings().screenWidthPixels * Display.getPixelScaleFactor()).toInt(),
            (Global.getSettings().screenHeightPixels * Display.getPixelScaleFactor()).toInt())

        GL11.glMatrixMode(5889)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, displayWidth.toDouble(), 0.0,displayHeight.toDouble(), -1.0, 1.0)
        GL11.glMatrixMode(5890)
        GL11.glPushMatrix()
        GL11.glMatrixMode(5888)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glEnable(3553)
    }

    //ShaderLib.exitDraw() without the program link
    fun exitDraw() {
        GL11.glMatrixMode(5888)
        GL11.glPopMatrix()
        GL11.glMatrixMode(5890)
        GL11.glPopMatrix()
        GL11.glMatrixMode(5889)
        GL11.glPopMatrix()
        GL11.glPopAttrib()
        GL13.glActiveTexture(33984)
        GL11.glBindTexture(3553, ShaderLib.getScreenTexture())
        //GL20.glUseProgram(0)
    }
}