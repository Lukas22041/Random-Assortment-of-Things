package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import org.dark.shaders.util.ShaderLib
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.util.glu.Sphere
import org.lwjgl.util.vector.Vector2f
import java.util.*

class ShadedSphere : BaseCombatLayeredRenderingPlugin() {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/planets/terran.jpg")
    var noise = Global.getSettings().getAndLoadSprite("graphics/fx/rat_shaded_planet_test.png")

    var sphere = Sphere()

    var rotation = 0f

    var loc = Vector2f(0f, 0f)
    var radius = 300f

    companion object {
        var shader = 0
    }

    init {
        sphere.setTextureFlag(true);
        sphere.setDrawStyle(100012);
        sphere.setNormals(100000);
        sphere.setOrientation(100020);

        if (shader == 0) {

        }
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return 100000000f
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        rotation += 3 * amount
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        if (Global.getCombatEngine().playerShip != null) loc = Global.getCombatEngine().playerShip.location.plus(
            Vector2f(500f, 500f))

        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_planet_shader_test.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)
            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "noiseTex"), 1)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }

        GL11.glPushMatrix();
        //where you want the sphere to render
        GL11.glTranslatef(loc.x + radius / 2, loc.y + radius / 2 , 1f);

        //rotates the sphere
        //not gonna lie, I've got no idea what's going on with this matrix maths, I'm just pressing buttons until it looks ok
        GL11.glRotatef(90f, 1f, 0f, 0f);

     /*   if (rotation < Float.MAX_VALUE)
        {
            if (isCloud)
            {
                rotation += 0.1f
            }
            else
            {
                rotation += 0.05f
            }
        }
*/

        GL11.glRotatef(-rotation, -0.2f, 0f, 1f);

        //set caps
        //blend should be disabled instead if you want it to be a solid colour
        //GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        //bind the texture to be used by the sphere
        sprite.bindTexture();

        //set colour to white so it gets drawn nomrally

        GL11.glColor4f(0.85f, 0.85f, 0.85f, 1f);







        GL20.glUseProgram(shader)

        var viewport = Global.getCombatEngine().viewport

        GL20.glUniform1f(GL20.glGetUniformLocation(shader, "alphaMult"), 1f)


        //Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.textureId)

        //Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, noise.textureId)

        //Reset Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)


        sphere.draw(radius, 32, 32);


        GL20.glUseProgram(0)


        //reset caps for safety, enable blend if you disabled it etc
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
       // GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();



    }


}