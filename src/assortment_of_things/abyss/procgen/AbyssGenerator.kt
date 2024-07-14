package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.util.Misc

object AbyssGenerator {

    fun generate() {

        var data = AbyssData()
        Global.getSector().memoryWithoutUpdate.set(AbyssUtils.ABYSS_DATA_KEY, data)

        var system = Global.getSector().createStarSystem("The Abyssal Depths")

        system.initNonStarCenter()
        system.name = "The Abyssal Depths"

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