package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.entities.AbyssalFractureSmall
import assortment_of_things.abyss.scripts.AbyssDoctrineLearnedListener
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import org.lwjgl.util.vector.Vector2f

class AbyssData {

    var hyperspaceLocation = Vector2f(0f, 0f)
    var hyperspaceFracture: SectorEntityToken? = null

    var rootSystem: StarSystemAPI? = null
    var finalSystem: StarSystemAPI? = null
    var systemsData = ArrayList<AbyssSystemData>()
    var generatedSteps = 0

    var lastExitFracture: SectorEntityToken? = null
    var lastExitFractureDestination: Vector2f? = null
    var lastExitFractureSystem: StarSystemAPI? = null

    var abyssalsDestroyed = 0
    var hasAbyssalDoctrine = false

    var seraphsDestroyed = 0
    var hasSeraphDoctrine = false

    var doctrineLearnedListeners = ArrayList<AbyssDoctrineLearnedListener>()

}