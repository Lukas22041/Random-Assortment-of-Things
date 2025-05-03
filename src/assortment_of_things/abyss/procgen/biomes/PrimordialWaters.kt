package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

//Sierra Biome
class PrimordialWaters() : BaseAbyssBiome() {

    override fun getBiomeID(): String {
        return "primordial_waters"
    }

    override fun getDisplayName(): String {
        if (effectLevel <= 0) return "Nameless Sea"
        if (effectLevel >= 1) return "Primordial Waters"

        var radius = getRadius()
        if (MathUtils.getDistance(Global.getSector().playerFleet, getStencilCenter()) <= radius) {
            return "Primordial Waters"
        } else return "Nameless Sea"
    }


    private var biomeColor = Color(140, 0, 250)
    private var darkBiomeColor = Color(44, 0, 77)

    private var inactiveBiomeColor = Color(184, 176, 191)
    private var inactiveDarkBiomeColor = Color(66, 63, 69)


    override fun getBiomeColor(): Color {
        return biomeColor
    }

    override fun getDarkBiomeColor(): Color {
        return darkBiomeColor
    }

    fun getInactiveBiomeColor(): Color {
        return inactiveBiomeColor
    }

    fun getInactiveDarkBiomeColor(): Color {
        return inactiveDarkBiomeColor
    }

    override fun getTooltipColor(): Color {
        if (effectLevel <= 0) return getInactiveBiomeColor()
        if (effectLevel >= 1) return getBiomeColor()

        var radius = getRadius()
        if (MathUtils.getDistance(Global.getSector().playerFleet, getStencilCenter()) <= radius) {
            return getBiomeColor()
        } else return getInactiveBiomeColor()
    }

    override fun getSystemLightColor(): Color {
        var level = getLevel()
        if (level <= 0f) return getInactiveBiomeColor()
        if (level >= 1f) return getBiomeColor()
        else return Misc.interpolateColor(getInactiveBiomeColor(), getBiomeColor(), level)
    }

    override fun getParticleColor(): Color {
        var level = getLevel()
        if (level <= 0f) return getInactiveBiomeColor()
        if (level >= 1f) return getBiomeColor()
        else return Misc.interpolateColor(getInactiveBiomeColor(), getBiomeColor(), level)
    }

    override fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    override fun getMusicKeyId(): String? {
        return null
    }

    override fun getMaxDarknessMult(): Float {
        return 0.6f
    }

    //Do not let the normal generation handle this
    override fun shouldGenerateBiome(): Boolean {
        return false
    }

    //Should not be overwriteable by another minibiome
    override fun canBeOverwritten(): Boolean {
        return false
    }

    //Place the biome between two other biomes
    override fun preDepthScan() {
        var manager = AbyssUtils.getBiomeManager()
        var picks = manager.getCells()/*.filter { it.getBiome() == null }*/
        //Do not let it spawn at the edges
        //var cell = picks.filter { it.gridX != 0 && it.gridX != AbyssBiomeManager.width && it.gridY != 0 && it.gridY != AbyssBiomeManager.height }.random()
        var cellsFull = picks.filter { it.gridX > 0 && it.gridX < AbyssBiomeManager.rows -1 && it.gridY > 0 && it.gridY < AbyssBiomeManager.columns -1 }


        //Only cells where any of their surrounding biome cells is not their own biome,
        // and only on the border of the sea of tranquility and not the wastes
        var cells = cellsFull.filter { center -> center.getSurrounding().any {
            surrounding -> center.getBiome() != surrounding.getBiome() && !surrounding.isFake && surrounding.getBiome() is SeaOfTranquility } }

        cells = cells.filter { it.getAdjacent().none { it.getBiome() is AbyssalWastes } }

        //More generous search, in case it cant find a connection to the sea of tranquility without also being in the wastes
        if (cells.isEmpty()) {
            cells = cellsFull.filter { center -> center.getSurrounding().any {
                    surrounding -> center.getBiome() != surrounding.getBiome() && !surrounding.isFake && surrounding.getBiome() is SeaOfTranquility } }
        }

        var radius = 3

        cells = cells.filter { it.getAround(radius).none { it.isFake } }

        //Ensure it doesnt pick a biome border with any biome that can not be overwritten
        cells = cells.filter { center -> center.getAround(radius).none { it.getBiome()?.canBeOverwritten() == false} }

        var cell = cells.random()

        cell.setBiome(this)
        deepestCells.add(cell)

        for (surounding in cell.getAround(radius).shuffled()) {
            //if (surounding.getBiome() != null) continue
            if (surounding.isFake) continue
            surounding.setBiome(this)
        }
    }

    override fun preGenerate() {

    }

    override fun getBackgroundColor(): Color {
        return super.getBackgroundColor().darker()
    }

    /** Called after all cells are generated */
    override fun init() {
        generateFogTerrain("rat_primordial_waters", "rat_terrain", "depths1", 0.70f)
        var fog = terrain as BaseFogTerrain
        var editor = OldNebulaEditor(fog)

        var system = AbyssUtils.getSystem()

        var center = deepestCells.first()



        var whirlwind = system!!.addCustomEntity("rat_abyss_whirlwind_${Misc.genUID()}", "", "rat_abyss_primordial_whirlwind", Factions.NEUTRAL)
        whirlwind.location.set(center.getWorldCenter())


        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, 0f, AbyssBiomeManager.cellSize * 0.6f, 0f, 360f)


        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 2.35f, AbyssBiomeManager.cellSize * 5f, 0f, 360f)

        //Make it look less circular

        var angle1 = MathUtils.getRandomNumberInRange(0f, 360f)
        var angle2 = MathUtils.getRandomNumberInRange(0f, 360f)

        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 2f, AbyssBiomeManager.cellSize * 5f, angle1, angle1+80f)
        editor.clearArc(center.getWorldCenter().x, center.getWorldCenter().y, AbyssBiomeManager.cellSize * 1.9f, AbyssBiomeManager.cellSize * 5f, angle2, angle2+50f)


        var pLoc = center!!.getWorldCenter()

        //Photosphere

        var photosphere = system!!.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_primordial_photosphere", Factions.NEUTRAL)
        photosphere.setLocation(pLoc.x, pLoc.y)
        photosphere.radius = 100f

        /*ReflectionUtils.invoke("setShowIconOnMap", photosphere.customEntitySpec, false)
        photosphere.addTag(Tags.NO_ENTITY_TOOLTIP)
        photosphere.addTag(Tags.NON_CLICKABLE)*/

        majorLightsources.add(photosphere)

        var plugin = photosphere.customPlugin as AbyssalLight
        plugin.radius = 15000f

        //Activation Station
        var angle = MathUtils.getRandomNumberInRange(0f, 360f)
        var catalyst = system!!.addCustomEntity("rat_primordial_catalyst_${Misc.genUID()}", "Primordial Catalyst", "rat_abyss_primordial_activator", Factions.NEUTRAL)
        catalyst.setCircularOrbitWithSpin(photosphere, angle, 800f, -120f, 5f, 6f)

        this.catalyst = catalyst

        var fabricator = system!!.addCustomEntity("rat_abyss_primordial_fabricator_${Misc.genUID()}", "Primordial Fabricator", "rat_abyss_primordial_fabricator", Factions.NEUTRAL)
        fabricator.setCircularOrbitWithSpin(photosphere, angle + MathUtils.getRandomNumberInRange(60f, 90f), 500f, -110f, 5f, 6f)
        fabricator.setDiscoverable(null)
        fabricator.setSensorProfile(null)
    }


    var effectLevel = 0.0f
    fun getMaxRadius() = 5000f

    private var catalyst: SectorEntityToken? = null
    fun getStencilCenter() : Vector2f {
        //return deepestCells.first().getWorldCenter()
        return catalyst?.location ?: Vector2f()
    }

    fun getRadius() : Float {
        return getMaxRadius() * easeInOutSine(effectLevel)
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    fun getLevel() : Float {
        return MathUtils.clamp(effectLevel, 0f, 1f)
    }

    fun startStencil(reverse: Boolean) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        GL11.glBegin(GL11.GL_POLYGON) // Middle circle

        var center = getStencilCenter()

        var radius = getRadius()
        val x = center.x
        val y = center.y
        var points = 100

        for (i in 0..points) {

            val angle: Double = (2 * Math.PI * i / points)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()

        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.
        //GL11.glDepthMask(true);

        var ref = 1
        if (reverse) ref = 0
        GL11.glStencilFunc(GL11.GL_EQUAL, ref, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}