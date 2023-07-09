package assortment_of_things.abyss.intel.event

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color


class AbyssalDepthsEventIntel() : BaseEventIntel(), FleetEventListener {

    enum class Stage(var progress: Int) {
        START(0), INTO_THE_DEPTHS(200), RESOURCEFULNESS(350), RETURNAL(550), LIFETIME_EXPERIENCE(750), STARE_IN_TO(900)
    }

    companion object {
        @JvmStatic
        var KEY = "\$abyss_event_ref"

        @JvmStatic
        fun addFactorCreateIfNecessary(factor: EventFactor?, dialog: InteractionDialogAPI?) {
            if (get() == null) {
                AbyssalDepthsEventIntel()
            }
            if (get() != null) {
                get()!!.addFactor(factor, dialog)
            }
        }

        @JvmStatic
        fun get(): AbyssalDepthsEventIntel? {
            return Global.getSector().memoryWithoutUpdate[KEY] as AbyssalDepthsEventIntel?
        }
    }

    init {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);

        setMaxProgress(Stage.STARE_IN_TO.progress);

        addStage(Stage.START, 0);
        addStage(Stage.INTO_THE_DEPTHS, Stage.INTO_THE_DEPTHS.progress, StageIconSize.SMALL);
        addStage(Stage.RESOURCEFULNESS, Stage.RESOURCEFULNESS.progress, StageIconSize.SMALL);
        addStage(Stage.RETURNAL, Stage.RETURNAL.progress, StageIconSize.MEDIUM);
        addStage(Stage.LIFETIME_EXPERIENCE, Stage.LIFETIME_EXPERIENCE.progress, StageIconSize.MEDIUM);
        addStage(Stage.STARE_IN_TO, Stage.STARE_IN_TO.progress, StageIconSize.SMALL);

        getDataFor(Stage.INTO_THE_DEPTHS).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.RESOURCEFULNESS).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.RETURNAL).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.LIFETIME_EXPERIENCE).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.STARE_IN_TO).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.STARE_IN_TO).isRepeatable = true

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, true);
    }




    override fun getStageIconImpl(stageId: Any?): String {

        var spritePath = when(stageId) {
            Stage.START -> "graphics/icons/intel/events/abyssal1.png"
            Stage.INTO_THE_DEPTHS -> "graphics/icons/intel/events/abyssal2.png"
            Stage.RESOURCEFULNESS -> "graphics/icons/intel/events/abyssal3.png"
            Stage.RETURNAL -> "graphics/icons/intel/events/abyssal4.png"
            Stage.LIFETIME_EXPERIENCE -> "graphics/icons/intel/events/abyssal5.png"
            Stage.STARE_IN_TO -> "graphics/icons/intel/events/abyssal6.png"
            else -> "graphics/icons/intel/events/abyssal1.png"
        }


        Global.getSettings().loadTexture(spritePath)
        return spritePath
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Abyss")
    }



    override fun addBulletPoints(info: TooltipMakerAPI?,  mode: IntelInfoPlugin.ListInfoMode?,isUpdate: Boolean, tc: Color?,initPad: Float) {

        if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
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
                    Stage.START -> tooltip!!.addTitle("Abyssal Exploration")
                    Stage.INTO_THE_DEPTHS -> tooltip!!.addTitle("Into the Depths")
                    Stage.RESOURCEFULNESS -> tooltip!!.addTitle("Resourcefulness")
                    Stage.RETURNAL -> tooltip!!.addTitle("Returnal")
                    Stage.LIFETIME_EXPERIENCE -> tooltip!!.addTitle("Lifetime Experience")
                    Stage.STARE_IN_TO -> tooltip!!.addTitle("Stare in to the abyss")
                }

                addStageDesc(tooltip!!, stageId, 10f, true)

                esd.addProgressReq(tooltip, 10f)
            }
        }

    }

    fun addStageDesc(info: TooltipMakerAPI, stageId: Any?, initPad: Float, forTooltip: Boolean)
    {
        if (stageId == Stage.START)
        {
            info.addPara("As you dive in to the abyss, you are met with a landscape untouched by man for hundreds of cycles. Further exploration may improve your understanding of this strange space.", 0f)
        }
        if (stageId == Stage.INTO_THE_DEPTHS)
        {
            info.addPara("Getting accustomed to the unique terrain of the abyss, navigating through it becomes more manageable, increasing the fleets maximum burn by 1", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "unique terrain", "maximum burn", "1")
        }
        if (stageId == Stage.RESOURCEFULNESS)
        {
            info.addPara("Extended stay in the abyss requires the fleet to make careful decisions on the useage of resources, decreasing the amount of supplies used per day by 25%% within it.", 0f,  Misc.getTextColor(), Misc.getHighlightColor(), "supplies", "25%")
        }
        if (stageId == Stage.RETURNAL)
        {
            info.addPara("Through mapping out the abyss, the fleet is now capable of adjusting the destination of the \"Singularity Jump\" Ability. While Holding L-CTRL in Hyperspace and then activating the ability, the fleet now returns to the last point it left the abyss from.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "Singularity Jump", "returns the fleet to the last location", "L-CTRL")
        }
        if (stageId == Stage.LIFETIME_EXPERIENCE)
        {
            info.addPara("The experience aquirred from exploring the abyss gains the fleets captain an additional skill point.", 0f,  Misc.getTextColor(), Misc.getHighlightColor(),
                "skill point")
        }
        if (stageId == Stage.STARE_IN_TO)
        {
            info.addPara("Intensive exploration of the abyss procured further supplies, gaining an additional random hull alteration."
                , 0f,  Misc.getTextColor(), Misc.getHighlightColor(), "additional random hull alteration")

            info.addSpacer(10f)

            val min = getTopoResetMin()
            val max = getTopoResetMax()

            info.addPara("Event progress will be reset to between $min and $max points when this outcome is reached.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "$min",  "$max")
        }
    }

    override fun getName(): String {
        return "Abyssal Exploration"
    }

    override fun advanceImpl(amount: Float) {
        super.advanceImpl(amount)
        applyFleetEffects()
    }

    fun applyFleetEffects() {
        val id = "abyssal_event"
        val fleet = Global.getSector().playerFleet
        fleet.stats.fleetwideMaxBurnMod.unmodify(id)

        if (fleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) {

            if (isStageActive(Stage.INTO_THE_DEPTHS)) {
                fleet.stats.fleetwideMaxBurnMod.modifyFlat(id, 1f,"Abyssal Exploration")
            }


        }

        if (isStageActive(Stage.RESOURCEFULNESS))  {
            if (fleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
            {
                for (member in fleet.fleetData.membersListCopy)
                {
                    member.stats.suppliesPerMonth.modifyMult(id, 0.75f)
                }
            }
            else
            {
                for (member in fleet.fleetData.membersListCopy)
                {
                    member.stats.suppliesPerMonth.unmodify(id)
                }
            }
        }
    }

    override fun notifyStageReached(stage: EventStageData?) {
        super.notifyStageReached(stage)

        if (stage!!.id == Stage.LIFETIME_EXPERIENCE)
        {
            Global.getSector().playerFleet.commanderStats.addPoints(1)
        }

        if (stage!!.id == Stage.STARE_IN_TO)
        {
            resetEvent()
            var randomAlteration = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") }.random()

            val cargo = Global.getSector().playerFleet.cargo
            cargo.addSpecial(SpecialItemData("rat_secondary_install", randomAlteration!!.id), 1f)
            Global.getSector().campaignUI.messageDisplay.addMessage("Aquirred ${randomAlteration.displayName} from Abyssal Exploration")
        }
    }

    fun resetEvent()
    {
        val resetProgress: Int = getTopoResetMin() + getRandom().nextInt(getTopoResetMax() - getTopoResetMin() + 1)
        setProgress(resetProgress)
    }

    fun getTopoResetMin(): Int {
        val stage = getDataFor(Stage.LIFETIME_EXPERIENCE)
        return stage.progress
    }

    fun getTopoResetMax(): Int {
        return getTopoResetMin() + 50
    }

    override fun getBarColor(): Color {
        return AbyssUtils.ABYSS_COLOR.darker()
    }

    override fun getBarProgressIndicatorColor(): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    override fun getBarBracketColor(): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    override fun getProgressColor(delta: Int): Color {
        return Misc.getHighlightColor()
    }

    override fun getBarProgressIndicatorLabelColor(): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    override fun getTitleColor(mode: IntelInfoPlugin.ListInfoMode?): Color {
        return Misc.getBasePlayerColor()
    }

    override fun getStageColor(stageId: Any?): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    override fun getBaseStageColor(stageId: Any?): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    override fun getDarkStageColor(stageId: Any?): Color {
        return AbyssUtils.ABYSS_COLOR.darker()
    }

    override fun getStageIconColor(stageId: Any?): Color {
        return Color(255, 255, 255)
    }

    override fun getStageLabelColor(stageId: Any?): Color {
        return AbyssUtils.ABYSS_COLOR
    }


    override fun getCircleBorderColorOverride(): Color {
        return AbyssUtils.ABYSS_COLOR
    }

    //This sets the color for the headers, because alex.
    //Replace with boss factions color once ready
    override fun getFactionForUIColors(): FactionAPI {
        return Global.getSector().getFaction(Factions.NEUTRAL)
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
        Global.getSettings().loadTexture("graphics/icons/intel/events/abyssal1.png")
        return "graphics/icons/intel/events/abyssal1.png"
    }

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {


    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }

}