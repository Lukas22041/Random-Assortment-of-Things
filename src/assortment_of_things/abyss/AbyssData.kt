package assortment_of_things.abyss

import assortment_of_things.abyss.entities.hyper.AbyssalFracture
import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeParticleManager
import assortment_of_things.abyss.procgen.MapRevealerScript
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI

class AbyssData {

    var hasGenerated = false
    var system: StarSystemAPI? = null
    var warper: AbyssBackgroundWarper? = null

    var biomeManager = AbyssBiomeManager()
    var mapRevealer = MapRevealerScript(biomeManager)
    var particleManager = BiomeParticleManager(biomeManager)
    var darknessTerrain: AbyssalDarknessTerrainPlugin? = null

    var hyperspaceFracture: SectorEntityToken? = null
    var abyssFracture: SectorEntityToken? = null

}