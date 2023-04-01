package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.misc.RATSettings
import assortment_of_things.strings.RATTags
import assortment_of_things.snippets.ProcgenDebugSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.campaign.CampaignEngine
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaExtensions.getSystemsWithTag


class RATModPlugin : BaseModPlugin() {

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        if (Global.getSettings().modManager.isModEnabled("parallel_construction"))
        {
            throw Exception("\"Parallel Construction\" as a mod is included in \"Random Assortment of Things\" and should be disabled/removed to avoid issues.")
        }

        LunaDebug.addSnippet(ProcgenDebugSnippet())

        LootModifier.saveOriginalData()

    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        Global.getSector().listenerManager.addListener(RATSettings, true)
        Global.getSector().registerPlugin(RATCampaignPlugin())
        Global.getSector().addTransientScript(ParallelConstruction())



        LootModifier.modifySpawns()


        if (RATSettings.disableHelp!!)
        {
            try {
                CampaignEngine.getInstance().campaignHelp.isEnabled = false
            }
            catch (e: Throwable)
            {
                Global.getLogger(this.javaClass).error("Failed to disable Help Popups.")
            }
        }
    }

    override fun onNewGame() {
        super.onNewGame()
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        var chirality = Global.getSector().getFaction("chirality")
        chirality?.adjustRelationship("player", -1f)

        for (system in Global.getSector().getSystemsWithTag(RATTags.THEME_CHIRAL_COPY))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }

        for (system in Global.getSector().getSystemsWithTag(RATTags.THEME_CHIRAL_MAIN))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }
    }
}