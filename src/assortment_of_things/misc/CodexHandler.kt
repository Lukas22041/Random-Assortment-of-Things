package assortment_of_things.misc

import assortment_of_things.artifacts.ArtifactSpec
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.artifacts.ui.ArtifactTooltip
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ModSpecAPI
import com.fs.starfarer.api.impl.SharedUnlockData
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.codex.*
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaCodex.LunaCodex
import second_in_command.SCUtils
import second_in_command.misc.codex.CodexHandler
import second_in_command.misc.codex.CodexHandler.getAptitudEntryId
import second_in_command.specs.SCSpecStore
import java.util.*
import kotlin.math.max


object CodexHandler {

    var CODEX_CAT_ARTIFACTS = "codex_artifacts"
    var ARTIFACT_UNLOCK_CAT = "rat_codex_artifacts"

    fun getArtifactsCategory() : CodexEntryPlugin {
        return CodexDataV2.getEntry(CODEX_CAT_ARTIFACTS)
    }

    fun getArtifactsEntryId(artifactId: String) : String {
        return "${CODEX_CAT_ARTIFACTS}_$artifactId"
    }

    fun onAboutToStartGeneratingCodex() {

    }

    fun onAboutToLinkCodexEntries() {

    }

    fun onCodexDataGenerated() {
        var cat = LunaCodex.getModsCategory()

        var path = "graphics/icons/rat_luna_icon.png"
        Global.getSettings().loadTextureCached(path)
        var modEntry = ModEntry(LunaCodex.getModEntryId("assortment_of_things"), "Random Assortment of Things", path)
        cat.addChild(modEntry)
        CodexDataV2.ENTRIES.put(modEntry.id, modEntry)

        addArtifactsEntries(modEntry)

        //Link Abyssal Threat
        //TODO Add the Sariel version once done
        CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("rat_raguel"), CodexDataV2.getShipEntryId("rat_prayer"))
        CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("rat_gabriel"), CodexDataV2.getShipEntryId("rat_saint"))
    }

    fun addArtifactsEntries(modEntry: CodexEntryV2) {
        var path = "graphics/icons/codex/special_items.png"
        Global.getSettings().loadTextureCached(path)
        var artCategory = ArtifactsCatEntry(CODEX_CAT_ARTIFACTS,"Artifacts", path)
        modEntry.addChild(artCategory)
        CodexDataV2.ENTRIES.put(artCategory.id, artCategory)

        for (artifact in ArtifactUtils.artifacts) {
            var entry = ArtifactEntry(artifact, getArtifactsEntryId(artifact.id), artifact.name, artifact.spritePath)
            artCategory.addChild(entry)
            CodexDataV2.ENTRIES.put(entry.id, entry)
        }
    }

    class ModEntry(id: String, title: String, icon: String) : CodexEntryV2(id, title, icon) {

        override fun createTitleForList(info: TooltipMakerAPI?, width: Float, mode: CodexEntryPlugin.ListMode?) {
            info!!.addPara(title, Misc.getBasePlayerColor(), 0f)
            info.addPara("Mod", Misc.getGrayColor(), 0f)
        }
    }

    class ArtifactsCatEntry(id: String, title: String, icon: String) : CodexEntryV2(id, title, icon) {

        override fun createTitleForList(info: TooltipMakerAPI?, width: Float, mode: CodexEntryPlugin.ListMode?) {
            info!!.addPara(title, Misc.getBasePlayerColor(), 0f)
            info.addPara("Artifacts", Misc.getGrayColor(), 0f)
        }
    }

    class ArtifactEntry(var artifact: ArtifactSpec, id: String, title: String, icon: String) : CodexEntryV2(id, title, icon) {


        override fun createTitleForList(info: TooltipMakerAPI?, width: Float, mode: CodexEntryPlugin.ListMode?) {

            //Doing it this weird way since sic aptitude codex entries arent loaded yet
            if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
                for (aptitude in SCSpecStore.getAptitudeSpecs()) {
                    if (aptitude.categories.any { it.id == "sc_cat_automated" }) {
                        CodexDataV2.makeRelated(CodexHandler.getAptitudEntryId(aptitude.id), getArtifactsEntryId("computational_matrix"))
                    }
                }
            } else {
                CodexDataV2.makeRelated(getArtifactsEntryId("computational_matrix"), CodexDataV2.getSkillEntryId("automated_ships"))
            }

            info!!.addPara(title, Misc.getBasePlayerColor(), 0f)
            info.addPara("Artifact", Misc.getGrayColor(), 0f)
        }

        override fun isCategory(): Boolean {
            return false
        }

        override fun hasCustomDetailPanel(): Boolean {
            return true
        }

        override fun createCustomDetail(panel: CustomPanelAPI?, relatedEntries: UIPanelAPI?, codex: CodexDialogAPI?) {
            val opad = 10f

            val width = panel!!.position.width

            var initPad = 0f

            val horzBoxPad = 30f


            // the right width for a tooltip wrapped in a box to fit next to relatedEntries
            // 290 is the width of the related entries widget, but it may be null
            val tw = width - 290f - opad - horzBoxPad + 10f

            val text = panel!!.createUIElement(tw, 0f, false)

            ArtifactTooltip(artifact, false, isCodex = true).createTooltip(text, false, "")

            panel!!.updateUIElementSizeAndMakeItProcessInput(text)

            var box = panel!!.wrapTooltipWithBox(text)
            panel!!.addComponent(box).inTL(0f, 0f)
            if (relatedEntries != null) {
                panel!!.addComponent(relatedEntries).inTR(0f, 0f)
            }

            var height: Float = box.getPosition().getHeight()
            if (relatedEntries != null) {
                height = max(height, relatedEntries.position.height)
            }
            panel!!.position.setSize(width, height)
        }

        override fun getUnlockRelatedTags(): MutableSet<String> {
            return mutableSetOf(Tags.CODEX_UNLOCKABLE)
        }

        override fun isUnlockedIfRequiresUnlock(): Boolean {
            return isPlayerAwareOfThing(artifact.id, ARTIFACT_UNLOCK_CAT)
        }

        override fun getSourceMod(): ModSpecAPI {
            return Global.getSettings().modManager.getModSpec("assortment_of_things")
        }
    }



    fun reportPlayerAwareOfThing(thingId: String?, setId: String?, codexEntryId: String?, withSave: Boolean): Boolean {
        var shared = SharedUnlockData.get()
        val wasLocked: Boolean = shared.isEntryLocked(codexEntryId)
        if (shared.addToSet(setId, thingId)) {
            if (wasLocked && !shared.isEntryLocked(codexEntryId)) CodexIntelAdder.get().addEntry(codexEntryId)
            if (withSave) shared.saveIfNeeded()
            return true
        }
        return false
    }

    fun isPlayerAwareOfThing(thingId: String, setId: String) : Boolean {
        return SharedUnlockData.get().getSet(setId).contains(thingId)
    }

}