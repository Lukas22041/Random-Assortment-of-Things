package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI

class ChangeMainMenuColorScript : EveryFrameScript {

    companion object {
        var isInAbyss = false
        var lastAbyssColor = AbyssUtils.ABYSS_COLOR
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        var system = Global.getSector().playerFleet?.containingLocation
        if (system?.hasTag(AbyssUtils.SYSTEM_TAG) == true) {
            isInAbyss = true
            var data = AbyssUtils.getSystemData(system as StarSystemAPI)
            lastAbyssColor = data.getDarkColor().brighter()
        }
        else {
            isInAbyss = false
        }

    }

}