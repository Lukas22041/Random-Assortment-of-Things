package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.procgen.types.BaseAbyssType
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.lwjgl.util.vector.Vector2f

class AbyssSystemData(var system: StarSystemAPI) {

    var depth: AbyssDepth = AbyssDepth.Shallow

    var color = AbyssUtils.ABYSS_COLOR
    var darkColor = AbyssUtils.ABYSS_COLOR
    var previous: StarSystemAPI? = null
    var neighbours = ArrayList<StarSystemAPI>()
    var mapLocation = Vector2f()

    var terrain: AbyssTerrainPlugin? = null

    var majorPoints = ArrayList<Vector2f>()
    var fracturePoints = ArrayList<Vector2f>()
    var uniquePoints = ArrayList<Vector2f>()

    var fractures = ArrayList<AbyssalFracture>()

}