package assortment_of_things.abyss.procgen

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.lwjgl.util.vector.Vector2f

class AbyssData {

    var hyperspaceLocation = Vector2f(0f, 0f)
    var hyperspaceFracture: SectorEntityToken? = null

    lateinit var biomeManager: AbyssBiomeManager

}