package assortment_of_things.abyss.procgen.templates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

abstract class AbyssBaseTemplate(var name: String) {

    var system: StarSystemAPI
    init {
        system = Global.getSector().createStarSystem(name)
    }

    abstract fun getTier() : Int

    abstract fun generate() : StarSystemAPI

}