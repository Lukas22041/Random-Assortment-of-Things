package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.AbyssBackgroundWarper
import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

class TestSystemTemplate(name: String) : AbyssBaseTemplate(name) {
    override fun getTier(): Int {
        return 1
    }

    override fun generate(): StarSystemAPI {
        system.addTag(AbyssUtils.SYSTEM_TAG)

        var star = system.initNonStarCenter()
        system.generateAnchorIfNeeded()


        //var planet = system.addPlanet("Test2", star, "Test Planet", Planets.PLANET_TERRAN, 0f, 300f, 3000f, 90f)
        // system.autogenerateHyperspaceJumpPoints(true, true, false)

        AbyssBackgroundWarper(system, 8, 0.33f)


        AbyssUtils.generateAbyssTerrain(system, 0.3f)


        /*var fractures = AbyssUtils.createFractures(system, system)
        fractures.fracture2.location.set(1000f, 1000f)

        AbyssUtils.clearTerrainAroundFractures(fractures)*/

        return system
    }
}