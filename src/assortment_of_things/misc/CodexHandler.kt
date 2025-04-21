package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.impl.codex.CodexEntryPlugin
import com.fs.starfarer.api.impl.codex.CodexEntryV2
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaCodex.LunaCodex


object CodexHandler {

    fun onAboutToStartGeneratingCodex() {

    }

    fun onAboutToLinkCodexEntries() {

    }

    fun onCodexDataGenerated() {
        var cat = LunaCodex.getModsCategory()

        var path = "graphics/icons/rat_luna_icon.png"
        Global.getSettings().loadTexture(path)
        var modEntry = ModEntry(LunaCodex.getModEntryId("assortment_of_things"), "Random Assortment of Things", path)

        cat.addChild(modEntry)
        CodexDataV2.ENTRIES.put(modEntry.id, modEntry)
    }

    class ModEntry(id: String, title: String, icon: String) : CodexEntryV2(id, title, icon) {

        override fun createTitleForList(info: TooltipMakerAPI?, width: Float, mode: CodexEntryPlugin.ListMode?) {
            info!!.addPara(title, Misc.getBasePlayerColor(), 0f)
            info.addPara("Mod", Misc.getGrayColor(), 0f)
        }

    }

}