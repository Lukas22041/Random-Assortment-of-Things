package assortment_of_things.campaign.ui

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.getChildrenCopy
import assortment_of_things.misc.setOpacity
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.MapParams
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver
import lunalib.lunaExtensions.isPlayerInHyperspace
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class MinimapUI : EveryFrameScript {

    var frames = 0f
    var added = false
    var panel: CustomPanelAPI? = null
    var map: UIPanelAPI? = null
    var lastLocation: LocationAPI = Global.getSector().playerFleet.containingLocation

    companion object {
        var reset = false
    }

    init {
        reset = false
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }



    override fun advance(amount: Float) {
        frames++
        frames = MathUtils.clamp(frames, 0f, 100f)
        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        //var core = state.core as UIPanelAPI
        var core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI ?: return

        var size = when(RATSettings.minimapShape!!) {
            "Wide" -> Vector2f(350f, 200f)
            "Square" -> Vector2f(180f, 180f)
            else -> Vector2f(350f, 200f)
        }


        var paused =
            (Global.getSector().getCampaignUI().getCurrentCoreTab() == null &&
                    !Global.getSector().getCampaignUI().isShowingDialog() &&
                    !Global.getSector().getCampaignUI().isShowingMenu());


        if (paused && panel != null && !Global.getSector().playerFleet.isInHyperspaceTransition)
        {
            var x = MathUtils.clamp(panel!!.position.x - 30f, Global.getSettings().screenWidth - (size.x + 30), Global.getSettings().screenWidth + size.x)
            panel!!.position.inTL(x, Global.getSettings().screenHeight - (size.y + 10))
        }
        else if (panel != null)
        {
            var x = MathUtils.clamp(panel!!.position.x + 30f, Global.getSettings().screenWidth - (size.x + 30), Global.getSettings().screenWidth + size.x)
            panel!!.position.inTL(x, Global.getSettings().screenHeight - (size.y + 10))
        }

        if (map != null && !Global.getSector().isPaused)
        {
            ReflectionUtils.invoke("centerOn", map!!, Global.getSector().playerFleet.location, declared = true)
            //(map as void).centerOn(Global.getSector().playerFleet.location)
        }
        if ((frames > 1 && !added) || lastLocation != Global.getSector().playerFleet.containingLocation || reset)
        {
            lastLocation = Global.getSector().playerFleet.containingLocation
            for (child in core.getChildrenCopy())
            {
                // if (child.javaClass.name.contains("o0OoOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO") )
                if (child.position.width == 200f && child.position.height == 200f)
                {
                  //  core.removeComponent(child)
                    child.position.inTL(10000f, 10000f)
                }
            }


            if (panel != null)
            {
                core.removeComponent(panel)
                panel = null
            }


            panel = Global.getSettings().createCustom(size.x, size.y, null)
            core.addComponent(panel)

            panel!!.position.inTL(Global.getSettings().screenWidth + size.x, Global.getSettings().screenHeight - (size.y + 10))

            var element = panel!!.createUIElement(size.x, size.y, false)
            var params = MapParams()
            params.withLayInCourse = true
            params.centerOn = Global.getSector().playerFleet.location
            params.location = Global.getSector().playerFleet.containingLocation
            params.entityToShow = Global.getSector().playerFleet
            params.zoomLevel = 1.5f
            if (Global.getSector().isPlayerInHyperspace()) {
                params.zoomLevel = 2f
            }
            params.filterData.starscape = RATSettings.minimapStarscape!!
            params.filterData.factions = true
            params.filterData.legend = false
            params.filterData.fuel = RATSettings.minimapFueloverlay!!
            params.filterData.fuelRangeMult = Global.getSector().playerFleet.stats.fuelUseHyperMult.modifiedValue

            map = element.createSectorMap(size.x, size.y, params, null)
            element.addCustom(map, 0f)
            // element.addSectorMap(400f, 200f, Global.getSector().playerFleet.starSystem, 0f)
            panel!!.addUIElement(element)

            element.position.inTL(0f, 0f)


            reset = false
            added = true
        }
    }
}