package assortment_of_things.abyss

import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.MapRevealerScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

class AbyssData {

    var hasGenerated = false
    var system: StarSystemAPI? = null
    var warper: AbyssBackgroundWarper? = null

    var biomeManager = AbyssBiomeManager()
    var mapRevealer = MapRevealerScript(biomeManager)

}