package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.AbyssBackgroundWarper
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.StarTypes

class AbyssGenTier1(name: String, tier: AbyssProcgen.Tier) : AbyssBaseTemplate(name, tier) {

    override fun generate(): StarSystemAPI {

        var tier = AbyssProcgen.Tier.Low

        AbyssProcgen.generateDomainResearchStation(system, 1, 0.7f)
        AbyssProcgen.generateTransmitter(system, 2, 0.7f)
        AbyssProcgen.generateCache(system, 3,0.7f)

        AbyssProcgen.generatePhotospheres(system, 2, 0.7f)

        //system.initStar("test", StarTypes.ORANGE, 2000f, 200f)

        return system
    }
}