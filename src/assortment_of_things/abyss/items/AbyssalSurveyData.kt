package assortment_of_things.abyss.items

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalBeacon
import assortment_of_things.abyss.entities.light.AbyssalColossalPhotosphere
import assortment_of_things.abyss.entities.light.AbyssalDecayingPhotosphere
import assortment_of_things.abyss.entities.light.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.MapRevealerScript
import assortment_of_things.abyss.procgen.scripts.AbyssalLightDiscovery
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.state.AppDriver
import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.Random
import kotlin.random.asKotlinRandom

class AbyssalSurveyData : BaseSpecialItemPlugin() {

    companion object {
        var TAG_MAJOR_ENTITY = "rat_major_abyss_entity"
    }

    override fun init(stack: CargoStackAPI) {
        super.init(stack)
    }


    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 30000
    }

    override fun getName(): String? {
        return "Abyssal Survey Data"
    }

    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()
    }
    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 10f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)
        tooltip.addPara("A piece of survey data collected from within the abyssal depths. Any scientist in the sector would crave for a glimpse of it, making it sell well at most markets. \n\n" +
                "Can be used to reveal undiscovered parts of the abyss.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "sell", "reveal")

        tooltip.addSpacer(5f)
        tooltip.addPara("Right click to reveal a part of the abyssal depths map", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        if (!AbyssUtils.isPlayerInAbyss()) {
            tooltip.addSpacer(10f)
            tooltip.addNegativePara("Can only be used while in the abyssal depths.")
        }
        if (Global.getSector().campaignUI.currentInteractionDialog != null) {
            tooltip.addSpacer(10f)
            tooltip.addNegativePara("Can not be used while a dialog is open.")
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return AbyssUtils.isPlayerInAbyss() && Global.getSector().campaignUI.currentInteractionDialog == null
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    fun findEntitiesToReveal() : List<SectorEntityToken> {
        var entities = AbyssUtils.getSystem()?.customEntities ?: return ArrayList()
        var biomeManager = AbyssUtils.getBiomeManager() ?: return ArrayList();


        var random = getRandom()
        //30% chance for undiscovered photosphere & its orbiting entities
        if (random.nextFloat() >= 0.7f) {
            var photospheres = entities
                .filter { isPhotosphere(it) }

            var target = photospheres.filter { it.sensorProfile != null }.random(random.asKotlinRandom())
            if (target != null) {
                var orbitals = entities.filter { it.orbitFocus == target }
                var list = ArrayList<SectorEntityToken>()
                list.add(target)
                list.addAll(orbitals)
                return list
            }
        }
        //70% chance for entities, with major entities having a much higher chance until depleted
        else {
            var picker = WeightedRandomPicker<SectorEntityToken>()
            picker.random = random

            for (entity in entities) {
                if (!entity.isDiscoverable) continue
                if (isPhotosphere(entity)) continue
                var weight = 1f
                if (entity.hasTag(TAG_MAJOR_ENTITY)) weight *= 100f
                picker.add(entity, weight)
            }
            var pick = picker.pick() ?: return ArrayList()

            var picks = ArrayList<SectorEntityToken>()
            picks.add(pick)
            for (entity in entities) {
                if (MathUtils.getDistance(pick, entity) < 4500) {
                    picks.add(entity)
                }
            }

            return picks
        }

        return ArrayList()
    }

    fun isPhotosphere(entity: SectorEntityToken) : Boolean {
        return entity.customPlugin is AbyssalPhotosphere || entity.customPlugin is AbyssalBeacon
                || entity.customPlugin is AbyssalColossalPhotosphere || entity.customPlugin is AbyssalDecayingPhotosphere
    }

    fun getRandom() : Random {
        var random: Random? = Global.getSector().memoryWithoutUpdate.get("\$rat_abyss_survey_random") as Random?
        if (random == null) {
            random = Random()
            Global.getSector().memoryWithoutUpdate.set("\$rat_abyss_survey_random", random)
        }
        return random
    }

    override fun performRightClickAction() {


        //Reveal
        var targets = findEntitiesToReveal()
        if (targets.isEmpty()) {
            Global.getSector().campaignUI.addMessage("Could not find any entities.")
            return
        }

        Global.getSoundPlayer().playUISound("ui_sensor_burst_on", 0.9f, 0.9f)

        var manager = AbyssUtils.getBiomeManager() ?: return
        var photoScript = Global.getSector().scripts.find { it is AbyssalLightDiscovery } as AbyssalLightDiscovery?


        for (target in targets) {
            target.setDiscoverable(null)
            target.setSensorProfile(null)

            if (isPhotosphere(target)) {
                photoScript!!.reveal(target)
            }

            var cell = manager.getCell(target)
            cell.isDiscovered = true

            var rad = 2
            cell.getAround(rad).forEach {
                it.isDiscovered = true
                it.getAdjacent().forEach { it.isPartialyDiscovered = true }
            }
        }

        //Open Map
        MapRevealerScript.revealDelay = 0.15f

        Global.getSector().campaignUI.showCoreUITab(CoreUITabId.MAP)

        var state = AppDriver.getInstance().currentState ?: return

        var core: UIPanelAPI? = null;
        var dialog = ReflectionUtils.invoke("getEncounterDialog", state)
        if (dialog != null)
        {
            core = ReflectionUtils.invoke("getCoreUI", dialog) as UIPanelAPI?
        }

        if (core == null) {
            core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
        }

        if (core == null) return

        val mapTab = ReflectionUtils.invoke("getCurrentTab", core) as UIPanelAPI? ?: return
        var map = ReflectionUtils.invoke("getMap", mapTab)


        //ReflectionUtils.invoke("centerOn", mapTab, targets.first().location)
        var method = ReflectionUtils.getMethod("centerOnEntity", mapTab::class.java, parameters = null, parameterCount = 1)
        method!!.invoke(mapTab, targets.first())

    }
}