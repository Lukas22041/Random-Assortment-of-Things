package assortment_of_things.exotech.intel.event

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color


class ExotechEventIntel() : BaseEventIntel() {

    var faction = Global.getSector().getFaction("rat_exotech")

    enum class Stage(var progress: Int) {
        CONTACT(0), INTO_THE_DEPTHS(150), PERSISTANCE(300), IN_THE_DARK(450), STARE_IN_TO(600)
    }

    companion object {
        @JvmStatic
        var KEY = "\$exotech_event_ref"

        @JvmStatic
        fun addFactorCreateIfNecessary(factor: EventFactor?, dialog: InteractionDialogAPI?) {
            if (get() == null) {
                ExotechEventIntel()
            }
            if (get() != null) {
                get()!!.addFactor(factor, dialog)
            }
        }

        @JvmStatic
        fun get(): ExotechEventIntel? {
            return Global.getSector().memoryWithoutUpdate[KEY] as ExotechEventIntel?
        }
    }

    init {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);

        setMaxProgress(Stage.STARE_IN_TO.progress);

        addStage(Stage.CONTACT, Stage.CONTACT.progress, StageIconSize.SMALL);
        addStage(Stage.INTO_THE_DEPTHS, Stage.INTO_THE_DEPTHS.progress, StageIconSize.SMALL);
        addStage(Stage.PERSISTANCE, Stage.PERSISTANCE.progress, StageIconSize.SMALL);
        addStage(Stage.IN_THE_DARK, Stage.IN_THE_DARK.progress, StageIconSize.MEDIUM);
        addStage(Stage.STARE_IN_TO, Stage.STARE_IN_TO.progress, StageIconSize.MEDIUM);

        getDataFor(Stage.INTO_THE_DEPTHS).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.PERSISTANCE).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.IN_THE_DARK).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.STARE_IN_TO).keepIconBrightWhenLaterStageReached = true;
        //getDataFor(Stage.STARE_IN_TO).isRepeatable = true

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, true);
    }




    override fun getStageIconImpl(stageId: Any?): String {

        var spritePath = when(stageId) {
            Stage.CONTACT -> "graphics/icons/intel/events/rat_exo1.png"
            Stage.INTO_THE_DEPTHS -> "graphics/icons/intel/events/rat_exo1.png"
            Stage.PERSISTANCE -> "graphics/icons/intel/events/rat_exo1.png"
            Stage.IN_THE_DARK -> "graphics/icons/intel/events/rat_exo1.png"
            Stage.STARE_IN_TO -> "graphics/icons/intel/events/rat_exo1.png"
            else -> "graphics/icons/intel/events/rat_exo1.png"
        }


        Global.getSettings().loadTexture(spritePath)
        return spritePath
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("rat_exotech", Tags.INTEL_MAJOR_EVENT)
    }



    override fun addBulletPoints(info: TooltipMakerAPI?,  mode: IntelInfoPlugin.ListInfoMode?,isUpdate: Boolean, tc: Color?,initPad: Float) {

        if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
            return
        }

        val h = Misc.getHighlightColor()
        if (isUpdate && getListInfoParam() is EventStageData) {
            val esd = getListInfoParam() as EventStageData
            if (esd.id == Stage.INTO_THE_DEPTHS) {
                info!!.addPara("+1 Burn Speed & +1 to the burn speed at which the fleet is considered \"moving slowly\" within the abyss.", initPad, tc, h, "+1", "+1", "moving slowly")
            }
            if (esd.id == Stage.PERSISTANCE) {
                //info!!.addPara("25%% reduced supply useage in the abyss.", initPad, tc, h, "25%")
                info!!.addPara("50%% reduced damage from abyssal storms.", initPad, tc, h, "50%")
            }
            if (esd.id == Stage.IN_THE_DARK) {
                info!!.addPara("Additional sensor detection reduction in the dark.", initPad, tc, h)
            }
            if (esd.id == Stage.STARE_IN_TO) {
                info!!.addPara("Gained a skill point.", initPad, tc, h, "")
            }

            return
        }
    }

    override fun addStageDescriptionText(info: TooltipMakerAPI?, width: Float, stageId: Any?) {
        val opad = 10f
        val small = 0f
        val h = Misc.getHighlightColor()

        val stage = getDataFor(stageId) ?: return

        if (isStageActive(stageId)) {
            addStageDesc(info!!, stageId, small, false)
        }
    }

    override fun getStageTooltipImpl(stageId: Any?): TooltipMakerAPI.TooltipCreator {
        val esd = getDataFor(stageId)


        return object: BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                super.createTooltip(tooltip, expanded, tooltipParam)

                when (stageId) {
                    Stage.CONTACT -> tooltip!!.addTitle("Contact")
                    Stage.INTO_THE_DEPTHS -> tooltip!!.addTitle("Into the Depths")
                    Stage.PERSISTANCE -> tooltip!!.addTitle("Persistance")
                    Stage.IN_THE_DARK -> tooltip!!.addTitle("In the Dark")
                    Stage.STARE_IN_TO -> tooltip!!.addTitle("Stare in to the abyss")
                }

                addStageDesc(tooltip!!, stageId, 10f, true)

                esd.addProgressReq(tooltip, 10f)
            }
        }

    }

    fun addStageDesc(info: TooltipMakerAPI, stageId: Any?, initPad: Float, forTooltip: Boolean)
    {
        if (stageId == Stage.CONTACT)
        {
            info.addPara("",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "Daybreak", "Donating rare, almost once-a-sector grade equipment", "fulfilling certain requests")
        }
        if (stageId == Stage.INTO_THE_DEPTHS)
        {
            info.addPara("Getting accustomed to the unique terrain of the abyss, navigating through it becomes more manageable, increasing the fleets maximum burn by 1 and " +
                    "increasing the speed at which the fleet is considered \"moving slowly\" by 1.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "unique terrain", "maximum burn", "1", "moving slowly", "1")
        }
        if (stageId == Stage.PERSISTANCE)
        {
            info.addPara("After studying the patterns of abyssal of the abyssal enviroment, the fleet takes 50%% less damage from abyssal storms due to better pathing through the terrain.", 0f,  Misc.getTextColor(), Misc.getHighlightColor(),"50%", "abyssal storms")
        }
        if (stageId == Stage.IN_THE_DARK)
        {
            info.addPara("The fleet is capable of using the darkness of the abyss to to avoid other fleets. Additionaly decreases the fleets sensor detection range by 10%% in the dark.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "10%")
        }
        if (stageId == Stage.STARE_IN_TO)
        {
            info.addPara("The experience aquirred from exploring the abyss gains the fleets captain an additional skill point.", 0f,  Misc.getTextColor(), Misc.getHighlightColor(),
                "skill point")
        }
    }

    override fun getName(): String {
        return "Exo-Tech Influence"
    }

    override fun advanceImpl(amount: Float) {
        //super.advanceImpl(amount) Causes issues for some reason when the abyss generates after save load? weird stuff
        applyFleetEffects()
    }

    fun applyFleetEffects() {
        val id = "exo_event"
        val fleet = Global.getSector().playerFleet
        fleet.stats.fleetwideMaxBurnMod.unmodify(id)


       /* if (isStageActive(Stage.IN_THE_DARK)) {


        }*/

    }

    override fun notifyStageReached(stage: EventStageData?) {
        super.notifyStageReached(stage)

        /*if (stage!!.id == Stage.STARE_IN_TO)
        {

        }*/

    }

    override fun getBarColor(): Color {
        return faction.darkUIColor
    }

    override fun getBarProgressIndicatorColor(): Color {
        return faction.color
    }

    override fun getBarBracketColor(): Color {
        return faction.color
    }

    override fun getProgressColor(delta: Int): Color {
        return Misc.getHighlightColor()
    }

    override fun getBarProgressIndicatorLabelColor(): Color {
        return faction.color
    }

    override fun getTitleColor(mode: IntelInfoPlugin.ListInfoMode?): Color {
        return Misc.getBasePlayerColor()
    }

    override fun getStageColor(stageId: Any?): Color {
        return faction.color
    }

    override fun getBaseStageColor(stageId: Any?): Color {
        return faction.color
    }

    override fun getDarkStageColor(stageId: Any?): Color {
        return faction.darkUIColor
    }

    override fun getStageIconColor(stageId: Any?): Color {
        return Color(255, 255, 255)
    }

    override fun getStageLabelColor(stageId: Any?): Color {
        return faction.color
    }


    override fun getCircleBorderColorOverride(): Color {
        return faction.color
    }

    //This sets the color for the headers, because alex.
    //Replace with boss factions color once ready
    override fun getFactionForUIColors(): FactionAPI {


        return faction
    }

    override fun getBulletColorForMode(mode: IntelInfoPlugin.ListInfoMode?): Color {
        return Misc.getTextColor()
    }

    override fun withMonthlyFactors(): Boolean {
        return false
    }

    override fun withOneTimeFactors(): Boolean {
        return true
    }

    override fun getIcon(): String {
        return faction.crest
    }
}