package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

object AbyssGenerator {

    fun init() {

        var data = AbyssUtils.getData()
        var biomeManager = data.biomeManager

        var system = Global.getSector().createStarSystem("Abyssal Depths")
        system.name = "The Abyssal Depths"
        data.system = system

        system.initNonStarCenter()

        //Telport
        var playerFleet = Global.getSector().playerFleet
        var currentLocation = playerFleet.containingLocation
        var targetSystem = system

        currentLocation.removeEntity(playerFleet)
        targetSystem.addEntity(playerFleet)
        Global.getSector().setCurrentLocation(targetSystem)

        system.backgroundTextureFilename = "graphics/backgrounds/abyss/Abyss2.jpg"
        system.backgroundColorShifter.shift(this, AbyssUtils.DARK_ABYSS_COLOR, 0f, 99999f, 1f)

        system.addTerrain("rat_abyss_biome_test_renderer", null)
        var warper = AbyssBackgroundWarper(system, 8, 0.33f)
        warper.overwriteColor = AbyssUtils.DARK_ABYSS_COLOR //TODO, dynamicly change the overwrite color


        //Biomes
        biomeManager.init()


        /*Global.getSector().addScript(object:EveryFrameScript {

            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            var time = 0f
            override fun advance(amount: Float) {
                time += 1f * amount
                if (time >= 0.5f) {
                    time = 0f
                    biomeManager.init()
                }
            }

        })*/






        //Should be added last so that it renders over everything else on the map
        system.addTerrain("rat_map_revealer", null)

    }

}