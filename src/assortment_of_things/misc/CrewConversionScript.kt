package assortment_of_things.misc

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.coreui.CaptainPickerDialog
import com.fs.starfarer.ui.impl.StandardTooltipV2
import com.fs.state.AppDriver

class CrewConversionScript : EveryFrameScript {

    /*var screenPanelField = ReflectionUtils.getField("screenPanel", CampaignState::class.java)!!
    var getCodexEntryMethod = ReflectionUtils.getMethod("getCodexEntryId", StandardTooltipV2::class.java)!!
    var getChildrenCopyMethod: ReflectionUtils.ReflectedMethod? = null*/

    var hmods = ArrayList<String>()

    init {
        hmods.add("rat_abyssal_conversion")
        hmods.add("rat_chronos_conversion")
        hmods.add("rat_cosmos_conversion")
        hmods.add("rat_seraph_conversion")
        hmods.add("rat_primordial_conversion")
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        if (!Global.getSector().isPaused) return
        var tab = Global.getSector().campaignUI.currentCoreTab

        //Only in refit or fleet menu
        if (tab != CoreUITabId.REFIT && tab != CoreUITabId.FLEET) return

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var core: UIPanelAPI? = null
        var dialog = ReflectionUtils.invoke("getEncounterDialog", state)
        if (dialog != null)
        {
            core = ReflectionUtils.invoke("getCoreUI", dialog) as UIPanelAPI?
        }
        if (core == null) {
            core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
        }
        if (core == null) return
        var picker = core.getChildrenCopy().find { it.javaClass.simpleName == "CaptainPickerDialog" } as UIPanelAPI? ?: return

        var memberField = ReflectionUtils.getField(null, picker::class.java, FleetMember::class.java) ?: return
        var member = memberField.get(picker) as FleetMemberAPI ?: return
        var variant = member.variant
        if (member.variant.hullMods.none { hmods.contains(it) }) return

        var isAutoField = ReflectionUtils.getField(null, picker::class.java, Boolean::class.javaPrimitiveType) ?: return
        if (isAutoField.get(picker) == true) {
            isAutoField.set(picker, false)
            ReflectionUtils.invoke("sizeChanged", picker, picker.position.width, picker.position.height)
        }


    }

    /*fun printUITree(element: UIPanelAPI, gap: Int) {


        if (element is CaptainPickerDialog) {
            var parent = element.getParent()
            var parentParent = parent.getParent()
            var parentParentParent = parentParent.getParent()
            var test = ""
        }
        println(" ".repeat(gap)+"- ${element.javaClass.name}")

        for (child in element.getChildrenCopy()) {
            if (child is UIPanelAPI) {
                printUITree(child, gap+3)
            }
        }
    }*/

}