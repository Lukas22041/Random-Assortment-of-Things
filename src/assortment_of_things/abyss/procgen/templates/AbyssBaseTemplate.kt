package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.AbyssBackgroundWarper
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

abstract class AbyssBaseTemplate(var name: String, tier: AbyssProcgen.Tier) {

    var system: StarSystemAPI
    init {
        system = Global.getSector().createStarSystem(name)

        system.addTag(AbyssUtils.SYSTEM_TAG)
        var star = system.initNonStarCenter()
        system.generateAnchorIfNeeded()

       // AbyssBackgroundWarper(system, 8, 0.33f)

        AbyssUtils.setupTags(system)
        AbyssUtils.setTier(system, tier)

        //  AbyssUtils.generateAbyssTerrain(system, 0.3f)

    }

   abstract fun generate() : StarSystemAPI

}