package assortment_of_things.abyss

import assortment_of_things.abyss.intel.AbyssMap
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.procgen.AbyssChainGenerator
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f

class MidnightCoreSystem {

    var system: StarSystemAPI
    init {
        system = Global.getSector().createStarSystem("Midnight")
    }

    fun generate()
    {
        system.generateAnchorIfNeeded()
        AbyssUtils.setupTags(system)
        AbyssUtils.addAbyssSystemToMemory(system)

        //var star = system.initStar("Test", StarTypes.ORANGE, 800f, 200f)
        var star = system.initNonStarCenter()


        var token = system.createToken(0f, 0f)
        system.addEntity(token)
        Global.getSector().memoryWithoutUpdate.set("\$rat_abyss_midnight_token", token)


        //var planet = system.addPlanet("Test2", star, "Test Planet", Planets.PLANET_TERRAN, 0f, 300f, 3000f, 90f)
       // system.autogenerateHyperspaceJumpPoints(true, true, false)

        //AbyssBackgroundWarper(system, 8, 0.33f)

        AbyssUtils.generateBaseDetails(system, AbyssProcgen.Tier.Low)
        //AbyssUtils.generateAbyssTerrain(system, 0.5f)
        AbyssUtils.clearTerrainAround(token, 150f)

        //AbyssUtils.generateSuperchargedTerrain(system, Vector2f(600f, -400f), 300, 0.8f, false)

        var station = system.addCustomEntity("rat_domain_research_${Misc.genUID()}", "Domain Research Station", "rat_abyss_research", Factions.NEUTRAL)
        station.location.set(5000f, 0f)
        station.addTag(AbyssTags.DOMAIN_RESEARCH)
        station.addTag(AbyssTags.LOOTABLE)
        //station.addTag(AbyssTags.DOMAIN_RESEARCH_PRODUCTION)
        station.addTag(AbyssTags.DOMAIN_RESEARCH_SURVEY)

        var cache = system.addCustomEntity("rat_abyss_cache_${Misc.genUID()}", "Lost Crate", "rat_abyss_cache", Factions.NEUTRAL)
        cache.location.set(3000f, 0f)
        cache.addTag(AbyssTags.LOST_CRATE)


        var generator = AbyssChainGenerator()
        generator.startChain(system)

      /*  var fractures = AbyssUtils.createFractures(system, system)
        fractures.fracture2.location.set(1000f, 1000f)

        AbyssUtils.clearTerrainAroundFractures(fractures)*/

       addMapIntel()
       teleport(token)

        AbyssProcgen.generatePhotospheres(system, 3, 1f)
    }


    fun addMapIntel()
    {
        system.addScript( object : EveryFrameScript {

            var done = false

            override fun isDone(): Boolean {
                return done
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {

                if (Global.getSector().playerFleet.containingLocation == system)
                {
                    Global.getSector().intelManager.addIntel(AbyssMap())
                    done = true
                }
            }
        })
    }

    fun teleport(destination: SectorEntityToken)
    {
        Global.getSector().addTransientScript( object : EveryFrameScript {

            var frame = false
            var done = false

            override fun isDone(): Boolean {
                return done
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                if (!frame) {
                    frame = true
                }
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(destination , ""))
                done = true
            }
        })
    }
}