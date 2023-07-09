package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.campaign.StarSystemAPI

class AbyssSystemHigh(name: String, tier: AbyssProcgen.Tier) : BaseAbyssSystem(name, tier) {

    override fun generate(): StarSystemAPI {

        AbyssProcgen.generatePhotospheres(system, 3, 0.9f)

        AbyssProcgen.generateDomainResearchStations(system, 2, 0.7f)
        AbyssProcgen.generateTransmitters(system, 2, 0.5f)
        AbyssProcgen.generateCaches(system, 4,0.75f)

        return system
    }
}