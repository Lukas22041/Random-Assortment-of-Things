package assortment_of_things.campaign.bar

import assortment_of_things.campaign.intel.ChiralSystemIntel
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventIntel
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.getSystemsWithTag

class ChiralBarEvent : BaseBarEvent() {


    var gateSystem: StarSystemAPI? = null
    var finished = false

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {

        var systems = Global.getSector().getSystemsWithTag(RATTags.THEME_CHIRAL_MAIN)

        var givenIntel: Boolean? by LunaMemory("rat_gave_chiral_intel", false)

        if (systems.isNullOrEmpty() || givenIntel!!) return false
        gateSystem = systems.first()
        return true
    }

    override fun isDialogFinished(): Boolean {
        return finished
    }

    override fun addPromptAndOption(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.addPromptAndOption(dialog, memoryMap)

        dialog!!.textPanel.addPara("A table with men spreading rumors about some strange gate.")
        dialog!!.optionPanel.addOption("Rumors about a strange gate", this)

    }

    override fun init(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.init(dialog, memoryMap)

        dialog!!.optionPanel.clearOptions()
        dialog!!.textPanel.addPara("As you stroll around the bar, you encounter a group of drunks telling others about some kind of gate they discovered during their salvage expeditions in the fringe-worlds." +
                "\n\nThey blabber around about how they noted some tri-tach fleets moving around there. According to them this happened in the ${gateSystem!!.name}. Despite all their talk, they cant provide any information on whats " +
                "so strange about it to them, all just their intution, according to them.",
        Misc.getTextColor(), Misc.getHighlightColor(), "${gateSystem!!.name}")


        var intel = ChiralSystemIntel(gateSystem!!)
        Global.getSector().intelManager.addIntel(intel)

        intel.sendUpdate(null, dialog.textPanel)

        var givenIntel: Boolean? by LunaMemory("rat_gave_chiral_intel", false)
        givenIntel = true

        PortsideBarData.getInstance().removeEvent(this)
        finished = true




       // dialog.optionPanel.addOption("Leave", "LEAVE")

    }


    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)

        if (optionData is String)
        {
            if (optionData == "LEAVE")
            {
                finished = true
            }
        }
    }

}