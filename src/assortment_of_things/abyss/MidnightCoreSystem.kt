package assortment_of_things.abyss

import assortment_of_things.abyss.procgen.AbyssChainGenerator
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI

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
        Global.getSector().memoryWithoutUpdate.set("\$rat_abyss_midnight_token", token)


        //var planet = system.addPlanet("Test2", star, "Test Planet", Planets.PLANET_TERRAN, 0f, 300f, 3000f, 90f)
       // system.autogenerateHyperspaceJumpPoints(true, true, false)

        AbyssBackgroundWarper(system, 8, 0.33f)




        AbyssUtils.generateAbyssTerrain(system, 0.3f)

        AbyssUtils.generateRareNebula(system, token.location, 300, 0.8f, false)


        var generator = AbyssChainGenerator()
        generator.startChain(system)

      /*  var fractures = AbyssUtils.createFractures(system, system)
        fractures.fracture2.location.set(1000f, 1000f)

        AbyssUtils.clearTerrainAroundFractures(fractures)*/

        teleport(token)
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