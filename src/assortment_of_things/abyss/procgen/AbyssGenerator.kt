package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import assortment_of_things.abyss.procgen.scripts.AbyssalLightDiscovery
import assortment_of_things.abyss.procgen.scripts.MiscAbyssScript
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Tags

object AbyssGenerator {




    fun init() {

        var data = AbyssUtils.getData()
        var biomeManager = data.biomeManager

        var system = Global.getSector().createStarSystem("Abyssal Depths")
        system.name = "The Abyssal Depths"
        data.system = system

        system.initNonStarCenter()

        system.addTag(AbyssUtils.SYSTEM_TAG)

        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.THEME_UNSAFE)
        system.addTag(Tags.THEME_SPECIAL)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        system.addTag(Tags.DO_NOT_SHOW_STRANDED_DIALOG)

        system.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_music_abyss")


        system.generateAnchorIfNeeded()


        system.backgroundTextureFilename = "graphics/backgrounds/abyss/Abyss2.jpg"
        system.backgroundColorShifter.shift(this, AbyssUtils.DARK_ABYSS_COLOR, 0f, 99999f, 1f)

        system.addTerrain("rat_biome_grid_renderer", null)
        data.warper = AbyssBackgroundWarper(system, 8, 0.33f)
        data.warper!!.overwriteColor = AbyssUtils.DARK_ABYSS_COLOR


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




        system.mapGridWidthOverride = biomeManager.width.toFloat() /*- 4000*/
        system.mapGridHeightOverride = biomeManager.height.toFloat() /*- 4000*/






        //Add later to ensure running after all other terrains
        system.addTerrain("rat_abyss_terrain", null)

        var darkness = system.addTerrain("rat_depths_darkness", null)
        data.darknessTerrain = (darkness as CampaignTerrainAPI).plugin as AbyssalDarknessTerrainPlugin

        //Should be added last so that it renders over everything else on the map
        system.addTerrain("rat_map_revealer", null)




        Global.getSector().addScript(MiscAbyssScript())
        Global.getSector().addScript(AbyssalLightDiscovery())



        //Teleport
       /* var playerFleet = Global.getSector().playerFleet
        var currentLocation = playerFleet.containingLocation
        var targetSystem = system

        currentLocation.removeEntity(playerFleet)
        targetSystem.addEntity(playerFleet)
        Global.getSector().setCurrentLocation(targetSystem)

        //playerFleet.location.set(data.abyssFracture!!.location)
        playerFleet.setLocation(data.abyssFracture!!.location.x, data.abyssFracture!!.location.y)*/

        //generateHyperspaceEntrance() //Moved to tranquility biome plugin
    }



}