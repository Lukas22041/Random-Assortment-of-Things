package assortment_of_things.campaign.rulecmd

import assortment_of_things.eastereggs.ut.UTPanelDelegate
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.CharacterCreationData
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class RATShowUT : BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>): Boolean {
        dialog.showCustomVisualDialog(Global.getSettings().screenWidth, Global.getSettings().screenHeight, UTPanelDelegate())
        return false
    }
}