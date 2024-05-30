package assortment_of_things.exotech

import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.exotech.entities.ExoLightsource
import assortment_of_things.exotech.terrain.ExotechHyperNebula
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

object ExotechGenerator {

    fun setup() {



        generateExoshipRemains()
    }


    fun generateExoshipRemains() {

        var hyper = Global.getSector().hyperspace

        var width = Global.getSettings().getFloat("sectorWidth")
        var height = Global.getSettings().getFloat("sectorHeight")

        var exoshipEntity = hyper.addCustomEntity("exoship_${Misc.genUID()}", "Exoship Remains", "rat_exoship_broken", Factions.NEUTRAL)

        var loc = Vector2f((width /2) * MathUtils.getRandomNumberInRange(0.4f, 0.6f), -height/2 - 6000)
        exoshipEntity.location.set(loc)
        exoshipEntity.facing = 35f

        val w = 100
        val h = 100

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }


        val nebula = hyper.addTerrain("rat_exo_nebula",
            OldBaseTiledTerrain.TileParams(string.toString(),
                w,
                h,
                "rat_terrain",
                "depths1",
                4,
                4,
                null))
        nebula.id = "rat_depths_in_hyper_${Misc.genUID()}"
        nebula.location.set(loc)

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as ExotechHyperNebula
        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.65f)
        editor.regenNoise()

        //Clear all but a part on the right to make it less even
        editor.clearArc(loc.x, loc.y, nebulaPlugin.range, 100000f, 0f, 360f)
        editor.clearArc(loc.x, loc.y, 0f, nebulaPlugin.centerClearRadius, 0f, 360f)

        var clearRandom = MathUtils.getRandomPointOnCircumference(loc, 250f)
        editor.clearArc(clearRandom.x, clearRandom.y, 0f, 450f, 0f, 360f)

        var lightsource = hyper.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_exo_lightsource", Factions.NEUTRAL)
        lightsource.setCircularOrbit(exoshipEntity, 0f, 0f, 1000f)

        var plugin = lightsource.customPlugin as ExoLightsource
        plugin.radius = 10000f
        plugin.color = Color(130, 64, 1, 75)
    }

}