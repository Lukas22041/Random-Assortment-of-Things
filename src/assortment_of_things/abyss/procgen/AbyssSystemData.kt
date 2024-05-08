package assortment_of_things.abyss.procgen

import assortment_of_things.RATModPlugin
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class AbyssSystemData(var system: StarSystemAPI) {

    var step: Int = 0
    var depth: AbyssDepth = AbyssDepth.Shallow

    var baseColor = AbyssUtils.ABYSS_COLOR
    var baseDarkColor = AbyssUtils.ABYSS_COLOR
    var previous: StarSystemAPI? = null
    var neighbours = ArrayList<StarSystemAPI>()
    var mapLocation: Vector2f? = Vector2f()

    var terrain: AbyssTerrainPlugin? = null

    var minorPoints = ArrayList<Vector2f>()
    var majorPoints = ArrayList<Vector2f>()
    var fracturePoints = ArrayList<Vector2f>()
    var uniquePoints = ArrayList<Vector2f>()

    var fractures = ArrayList<AbyssalFracture>()

    fun getColor() : Color {
        if (RATSettings.brighterAbyss!!) return baseColor.brighter()

        return baseColor
    }

    fun getDarkColor() : Color {
        if (RATSettings.brighterAbyss!!) return baseDarkColor.brighter()

        return baseDarkColor
    }

}