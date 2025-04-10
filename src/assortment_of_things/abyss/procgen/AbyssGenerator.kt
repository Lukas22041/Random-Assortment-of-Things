package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
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

        system.addTerrain("rat_abyss_biome_test_renderer", null)



        //Biomes
        biomeManager.init()

    }

}