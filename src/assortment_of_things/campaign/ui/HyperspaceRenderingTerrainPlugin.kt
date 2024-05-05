package assortment_of_things.campaign.ui

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
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

        var fracture = AbyssUtils.getAbyssData().hyperspaceFracture
        if (fracture != null) {
            fractureText.fontSize = 800f * factor
            fractureText.baseColor = AbyssUtils.ABYSS_COLOR.setAlpha((255 * alphaMult).toInt())
            fractureText.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            fractureText.blendSrc = GL11.GL_SRC_ALPHA
            fractureText.drawOutlined(fracture.location.x * factor - (fractureText.width / 2), (fracture.location.y + 700) * factor + (fractureText.height))
        }

       /* val font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
        var tapText = font.createText("Hypershunt", Color(100, 0, 255, (255 * alphaMult).toInt()), 750f * factor)
        var cryoText = font.createText("Cryosleeper", Color(0, 200, 200, (255 * alphaMult).toInt()), 750f * factor)
        var beaconText = font.createText("Beacon", Color(250, 50, 50, (255 * alphaMult).toInt()), 300f * factor)

        for (tap in Global.getSector().getCustomEntitiesWithType("coronal_tap")) {
            var range = ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS * Global.getSettings().unitsPerLightYear

            renderCircle(tap.locationInHyperspace, factor, range, Color(100, 0, 255, 25), alphaMult)
            renderBorder(tap.locationInHyperspace, factor, range, Color(100, 0, 255), alphaMult)

            tapText.drawOutlined(tap.locationInHyperspace.x * factor - (tapText.width / 2), (tap.locationInHyperspace.y + 1000) * factor + (tapText.height))
        }

        for (cryo in Global.getSector().getCustomEntitiesWithType("derelict_cryosleeper")) {
            var range = 10f * Global.getSettings().unitsPerLightYear
            renderCircle(cryo.locationInHyperspace, factor, range, Color(0, 200, 200, 25), alphaMult)
            renderBorder(cryo.locationInHyperspace, factor, range, Color(0, 200, 200), alphaMult)

            cryoText.drawOutlined(cryo.locationInHyperspace.x * factor - (cryoText.width / 2), (cryo.locationInHyperspace.y + 1000) * factor + (cryoText.height))
        }

*//*
        for (system in Global.getSector().starSystems) {
            if (system.hasTag(Tags.THEME_REMNANT_MAIN)) {
                if (system.hasTag(Tags.THEME_REMNANT_RESURGENT)) {
                    var range = 1000f
                   *//**//* renderCircle(system.location, factor, range, Color(250, 50, 50, 25), alphaMult)
                    renderBorder(system.location, factor, range, Color(250, 50, 50), alphaMult)*//**//*

                    beaconText.drawOutlined(system.location.x * factor - (beaconText.width / 2), (system.location.y + 1000) * factor + (beaconText.height))

                }
                if (system.hasTag(Tags.THEME_REMNANT_SUPPRESSED)) {
                    var range = 1000f
                    renderCircle(system.location, factor, range, Color(250, 200, 50, 25), alphaMult)
                    renderBorder(system.location, factor, range, Color(250, 200, 50), alphaMult)
                }
                if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
                    var range = 1000f
                    renderCircle(system.location, factor, range, Color(0, 255, 100, 25), alphaMult)
                    renderBorder(system.location, factor, range, Color(0, 255, 100), alphaMult)
                }
            }*//*

        }*/


    }

    override fun renderOnRadar(radarCenter: Vector2f?, factor: Float, alphaMult: Float) {
        super.renderOnRadar(radarCenter, factor, alphaMult)

        if (font == null) {
            font = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
            fractureText = font!!.createText("The Abyssal Depths", AbyssUtils.ABYSS_COLOR.setAlpha(255), 800f)
        }

        var fracture = AbyssUtils.getAbyssData().hyperspaceFracture
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
        Global.getSettings().getSpec(StarGenDataSpec::class.java, "", true)

        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

}