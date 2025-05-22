package assortment_of_things.campaign.ui

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.Fonts
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.bounty.ui.drawOutlined
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class HyperspaceRenderingTerrainPlugin : BaseTerrain() {

    @Transient
    var font: LazyFont? = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)

    @Transient
    var fractureText:LazyFont.DrawableString = font!!.createText("The Abyssal Depths", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)


    override fun renderOnMapAbove(factor: Float, alphaMult: Float) {
        super.renderOnMapAbove(factor, alphaMult)

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("The Abyssal Depths", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
        }

        var fracture = AbyssUtils.getData().hyperspaceFracture
        if (fracture != null) {
            fractureText.fontSize = 800f * factor
            fractureText.baseColor = AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt())
            fractureText.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            fractureText.blendSrc = GL11.GL_SRC_ALPHA
            fractureText.drawOutlined(fracture.location.x * factor - (fractureText.width / 2), (fracture.location.y + 700) * factor + (fractureText.height))
        }

    }

    override fun renderOnRadar(radarCenter: Vector2f?, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("The Abyssal Depths", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
        }

        var fracture = AbyssUtils.getData().hyperspaceFracture
        if (fracture != null) {
            fractureText.fontSize = 800f * factor
            fractureText.baseColor = AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt())
            fractureText.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            fractureText.blendSrc = GL11.GL_SRC_ALPHA
            fractureText.drawOutlined((fracture.location.x - radarCenter!!.x) * factor - (fractureText.width / 2), (fracture.location.y + 1000 - radarCenter.y) * factor + (fractureText.height))
        }
    }

    fun renderBorder(location: Vector2f, factor: Float, radius: Float, color: Color, alpha: Float) {
        var c = color
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (alpha))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        val x = location.x * factor
        val y = location.y * factor
        var realRadius = radius * factor

        var points = 50

        for (i in 0..points) {
            val angle: Double = (2 * Math.PI * i / points)
            val vertX: Double = Math.cos(angle) * (realRadius)
            val vertY: Double = Math.sin(angle) * (realRadius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }

    fun renderCircle(location: Vector2f, factor: Float, radius: Float, color: Color, alpha: Float) {
        var c = color
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (alpha))

        GL11.glBegin(GL11.GL_POLYGON)

        val x = location.x * factor
        val y = location.y * factor
        var realRadius = radius * factor

        var points = 50

        for (i in 0..points) {
            val angle: Double = (2 * Math.PI * i / points)
            val vertX: Double = Math.cos(angle) * (realRadius)
            val vertY: Double = Math.sin(angle) * (realRadius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }


    override fun getEffectCategory(): String {
        return "rat_map_renderer"
    }

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

}