package assortment_of_things.abyss

import assortment_of_things.abyss.procgen.AbyssBiomeManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

class AbyssData {

    var hasGenerated = false
    var system: StarSystemAPI? = null

    var biomeManager = AbyssBiomeManager()


}