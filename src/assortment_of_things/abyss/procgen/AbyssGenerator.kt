package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import java.awt.Color

object AbyssGenerator {

    fun generate() {

        var system = Global.getSector().createStarSystem("The Abyssal Depths")
        var data = AbyssData(system)
        Global.getSector().memoryWithoutUpdate.set(AbyssUtils.ABYSS_DATA_KEY, data)

        system.initNonStarCenter()
        system.name = "The Abyssal Depths"

        system.generateAnchorIfNeeded()
        system.isProcgen = false
        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.THEME_UNSAFE)
        system.addTag(Tags.THEME_SPECIAL)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        system.addTag("do_not_show_stranded_dialog")

        system.addTag(AbyssUtils.SYSTEM_TAG)

        system.lightColor = Color(20, 20, 20)

        var playerFleet = Global.getSector().playerFleet
        var currentLocation = playerFleet.containingLocation
        var targetSystem = system

        currentLocation.removeEntity(playerFleet)
        targetSystem.addEntity(playerFleet)
        Global.getSector().setCurrentLocation(targetSystem)

        system.backgroundTextureFilename = "graphics/backgrounds/abyss/Abyss2.jpg"
        system.backgroundColorShifter.base = AbyssUtils.ABYSS_COLOR.darker().darker().darker()

        addAbyssTerrain(system)

        var manager = AbyssBiomeManager()
        data.biomeManager = manager
        Global.getSector().addScript(manager)

        manager.generate()
    }

    fun addAbyssTerrain(system: StarSystemAPI) {
        //Terrain

        val w = 600
        val h = 350

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }

        //var textureChoice = MathUtils.getRandomNumberInRange(1, 2)

        val nebula = system.addTerrain("rat_abyssal_depths",
            OldBaseTiledTerrain.TileParams(string.toString(),
                w,
                h,
                "rat_terrain",
                "depths1",
                4,
                4,
                null))
        nebula.id = "rat_depths_${Misc.genUID()}"
        nebula.location[0f] = 0f

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as AbyssTerrainPlugin
        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.35f)
        editor.regenNoise()
    }

}