package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc

class AbyssSystemHigh(name: String, tier: AbyssProcgen.Tier) : BaseAbyssSystem(name, tier) {

    override fun generate(): StarSystemAPI {

        AbyssProcgen.generatePhotospheres(system, 3, 0.9f)

        AbyssProcgen.generateDomainResearchStations(system, 1, 0.7f)
        AbyssProcgen.generateTransmitters(system, 2, 0.5f)
        AbyssProcgen.generateCaches(system, 3,0.75f)

        AbyssProcgen.addDerelictAbyssalShips(system, 5, 0.75f)


       /*  var entity = system.addCustomEntity("test_${Misc.genUID()}", "Test", "rat_abyss_jumpvisual", Factions.NEUTRAL)
        entity.location.set(-2000f, 0f)

        AbyssUtils.clearTerrainAround(entity, 5000f)*/

        return system
    }
}