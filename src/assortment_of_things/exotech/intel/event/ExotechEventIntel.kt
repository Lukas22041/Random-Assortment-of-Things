package assortment_of_things.exotech.intel.event

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.loadTextureCached
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
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
        CONTACT(0), SUPPLY_ACCESS(100), EXPANDED_SUPPLY(300), LEADERSHIP(550), INDEBTED(ExoUtils.getExoData().maximumInfluenceRequired)
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

        setMaxProgress(Stage.INDEBTED.progress);

        addStage(Stage.CONTACT, Stage.CONTACT.progress, StageIconSize.SMALL);
        addStage(Stage.SUPPLY_ACCESS, Stage.SUPPLY_ACCESS.progress, StageIconSize.SMALL);
        addStage(Stage.EXPANDED_SUPPLY, Stage.EXPANDED_SUPPLY.progress, StageIconSize.MEDIUM);
        addStage(Stage.LEADERSHIP, Stage.LEADERSHIP.progress, StageIconSize.LARGE);
        addStage(Stage.INDEBTED, Stage.INDEBTED.progress, StageIconSize.MEDIUM);

        getDataFor(Stage.SUPPLY_ACCESS).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.EXPANDED_SUPPLY).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.LEADERSHIP).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.INDEBTED).keepIconBrightWhenLaterStageReached = true;

        isImportant = true

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, true);
    }




    override fun getStageIconImpl(stageId: Any?): String {

        var spritePath = when(stageId) {
            Stage.CONTACT -> "graphics/portraits/rat_exo1.png"
            Stage.SUPPLY_ACCESS -> "graphics/icons/intel/events/rat_exo2.png"
            Stage.EXPANDED_SUPPLY -> "graphics/icons/intel/events/rat_exo3.png"
            Stage.LEADERSHIP -> "graphics/icons/intel/events/rat_exo5.png"
            Stage.INDEBTED -> "graphics/icons/intel/events/rat_exo4.png"
            else -> "graphics/icons/intel/events/rat_exo1.png"
        }

        Global.getSettings().loadTextureCached(spritePath)
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
            if (esd.id == Stage.SUPPLY_ACCESS) {
                info!!.addPara("You now have access to advanced equipment at the exoship.", initPad, tc, h, "+1", "+1", "moving slowly")
            }
            if (esd.id == Stage.EXPANDED_SUPPLY) {
                //info!!.addPara("25%% reduced supply useage in the abyss.", initPad, tc, h, "25%")
                info!!.addPara("You can now purchase ships from Exotech.", initPad, tc, h, "50%")
            }
            if (esd.id == Stage.LEADERSHIP) {
                info!!.addPara("Amelie is now trusted enough to be in charge of bigger roles. Check with Xander for more new missions.", initPad, tc, h, "")
            }
            if (esd.id == Stage.INDEBTED) {
                info!!.addPara("Many within the faction are indebted to Amelie for her work. She will be able to from now on get better deals on equipment.", initPad, tc, h)
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
                    Stage.SUPPLY_ACCESS -> tooltip!!.addTitle("Supply Access")
                    Stage.EXPANDED_SUPPLY -> tooltip!!.addTitle("Expanded Supply")
                    Stage.LEADERSHIP -> tooltip!!.addTitle("Leadership")
                    Stage.INDEBTED -> tooltip!!.addTitle("Indepted")
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
            info.addPara("You entered a contract with Amelie. You plan to increase her influence in the faction, resulting in her being able to offer you more access to the factions unique equipment." +
                    "You can increase her influence by donating domain-grade artifacts or completing missions offered by Xander.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "Amelie", "domain-grade artifacts", "completing missions offered by Xander")
        }
        if (stageId == Stage.SUPPLY_ACCESS)
        {
            info.addPara("With increased influence, Amelie manages to provide access to the factions supply of weapons and unique hull modifications.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "weapons and unique hull modifications")
        }
        if (stageId == Stage.EXPANDED_SUPPLY)
        {
            info.addPara("Amelie is now able to purchase ships under her name and transfer them to your inventory. Purchased ships come with an existing loadout of weapons. Exotech ships under your ownership can also now have their hull restored to remove d-mods.",
                0f,  Misc.getTextColor(), Misc.getHighlightColor(),"purchase ships", "restored")
        }
        if (stageId == Stage.LEADERSHIP)
        {
            info.addPara("Amelie is now trusted enough to be in charge of bigger roles. Check with Xander for more new missions.", 0f,  Misc.getTextColor(), Misc.getHighlightColor(),
                "")
        }
        if (stageId == Stage.INDEBTED)
        {
            info.addPara("Many within the faction are indebted to Amelie for her work. She will be able to from now on get better deals on equipment.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "Xander will now have more missions available.")
        }
    }

    override fun getName(): String {
        return "Exo-Tech Influence"
    }

    override fun advanceImpl(amount: Float) {
        //super.advanceImpl(amount) Causes issues for some reason
    }



    override fun notifyStageReached(stage: EventStageData?) {
        super.notifyStageReached(stage)

        if (stage!!.id == Stage.SUPPLY_ACCESS)
        {
            ExoUtils.getExoData().canBuyItems = true
        }

        if (stage!!.id == Stage.EXPANDED_SUPPLY)
        {
            ExoUtils.getExoData().canRepairShips = true
            ExoUtils.getExoData().canBuyShips = true
        }



        if (stage!!.id == Stage.LEADERSHIP) {
            ExoUtils.getExoData().reachedLeadershipGoal = true
        }

        if (stage!!.id == Stage.INDEBTED)
        {

        }
    }

    override fun getBarColor(): Color {
        return faction.baseUIColor.darker()
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