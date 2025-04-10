package assortment_of_things.exotech.abilities

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.interactions.exoship.ExoFuelbar
import assortment_of_things.exotech.interactions.exoship.PlayerExoshipInteraction
import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.abilities.BaseAbilityPlugin
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.MapParams
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc

class ManageExoshipAbility : BaseDurationAbility() {
    override fun activateImpl() {
        var exoship = ExoUtils.getExoData().getPlayerExoship()
        var exoshipPlugin = ExoUtils.getExoData().getPlayerExoshipPlugin()

        if (!exoshipPlugin.isInTransit) {
            Global.getSector().campaignUI.showInteractionDialog(PlayerExoshipInteraction(true), exoship)
        }
    }

    var currentFrame = 1
    var frameDirection = 1
    var frameInterval = IntervalUtil(0.04f, 0.04f)
    var currentFramePath = "graphics/icons/abilities/rat_exoship_management_transit_frame1.png"

    var blinkInterval = IntervalUtil(0.5f, 0.5f)
    var isBlinking = false

    @Transient var map: UIPanelAPI? = null
    var systemUsedForMap: LocationAPI? = null

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        tooltip.addTitle("Manage Exoship")

        tooltip.addSpacer(10f)

        tooltip.addPara("Activate this ability to manage the exoship and order it to warp to a new location.", 0f)

        if (Global.CODEX_TOOLTIP_MODE) return

        var exoship = ExoUtils.getExoData().getPlayerExoship()
        var exoshipPlugin = ExoUtils.getExoData().getPlayerExoshipPlugin()
        var playerModule = exoshipPlugin.playerModule

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Fuel", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        var currentFuel = (playerModule.currentFuelPercent * 100f).toInt()
        var currentFuelChargeRate = ((playerModule.fuelPercentPerMonthMax * playerModule.fuelProductionLevel) * 100f).toInt()
        var cost = playerModule.computeMonthlyCost(playerModule.fuelProductionLevel)

        var costString = Misc.getDGSCredits(cost)

        tooltip.addPara("The ship is currently at $currentFuel%% of its fuel tank capacity and recharges $currentFuelChargeRate%% of it every month. This comes at a monthly cost of $costString credits." ,0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "$currentFuel%", "$currentFuelChargeRate%", "$costString")

        tooltip.addSpacer(10f)

        ExoFuelbar(playerModule.currentFuelPercent, playerModule.currentFuelPercent, playerModule.maxFuelPercent, tooltip, tooltip.widthSoFar, 30f)

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Current Location", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        if (systemUsedForMap != exoship.containingLocation || map == null) {
            systemUsedForMap = exoship.containingLocation

            var mapP = MapParams()

            mapP.location = Global.getSector().hyperspace
            mapP.centerOn = exoship.locationInHyperspace
            mapP.entityToShow = exoship

            mapP.showSystem(exoship.starSystem)

            mapP.zoomLevel = 6f

            map = tooltip.createSectorMap(tooltip.widthSoFar, tooltip.widthSoFar * 0.5f, mapP, null)
        }
        tooltip.addCustom(map, 0f)

        if (exoshipPlugin.isInTransit) {
            tooltip.addSpacer(10f)
            tooltip.addNegativePara("The ability can not be used as the ship is in transit.")
        }

       // tooltip.addSectorMap(tooltip.widthSoFar, tooltip.widthSoFar * 0.5f, exoship.starSystem, 0f)
    }


    override fun isTooltipExpandable(): Boolean {
        return false
    }

    override fun applyEffect(amount: Float, level: Float) {

    }

    override fun deactivateImpl() {

    }

    override fun cleanupImpl() {

    }

    override fun advance(amount: Float) {
        super.advance(amount)

        var exoshipPlugin = ExoUtils.getExoData().getPlayerExoshipPlugin()
        var realAmount = amount

        if (Global.getSector().isFastForwardIteration) {
            realAmount /= Global.getSettings().getFloat("campaignSpeedupMult")
        }

        if (!isBlinking && exoshipPlugin.isInTransit) {
            blinkInterval.advance(realAmount)
            if (blinkInterval.intervalElapsed()) {
                isBlinking = true
            }
        }

        if (isBlinking) {
            frameInterval.advance(realAmount)
            if (frameInterval.intervalElapsed()) {
                currentFrame += frameDirection
                if (currentFrame == 10) {
                    frameDirection = -1
                }
                if (currentFrame == 1) {
                    frameDirection = 1
                    isBlinking = false
                }
            }
        }

        currentFramePath =  "graphics/icons/abilities/rat_exoship_management_transit_frame$currentFrame.png"
    }

    override fun getSpriteName(): String {
        var path = currentFramePath

      //  Global.getSettings().getAndLoadSprite(path)
        return path
    }

    override fun isUsable(): Boolean {
        var exoshipPlugin = ExoUtils.getExoData().getPlayerExoshipPlugin()

        if (exoshipPlugin.isInTransit) return false
        return super.isUsable()
    }
}