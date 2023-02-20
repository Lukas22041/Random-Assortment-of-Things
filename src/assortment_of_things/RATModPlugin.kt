package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.misc.PirateBaseDespawner
import assortment_of_things.misc.RATSettings
import assortment_of_things.campaign.procgen.customThemes.BlackmarketThemeGenerator
import assortment_of_things.misc.RATStrings
import assortment_of_things.snippets.ProcgenDebugSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.util.Misc
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
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        Global.getSector().listenerManager.addListener(RATSettings, true)
        Global.getSector().registerPlugin(RATCampaignPlugin())

        Global.getSector().addTransientScript(ParallelConstruction())

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


        /*var plugins = Global.getSector().genericPlugins
        plugins.addPlugin(RATDiscoveryPlugin(), true)*/
    }

    override fun onNewGame() {
        super.onNewGame()
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()
        //Makes sure that pirate bases added by the blackmarket theme despawn after being defeated
        for (market in BlackmarketThemeGenerator.pirateOutposts)
        {
            var fleet = Misc.getStationFleet(market)
            fleet?.addEventListener(PirateBaseDespawner(market))
        }
        BlackmarketThemeGenerator.pirateOutposts.clear()


        for (system in Global.getSector().getSystemsWithTag(RATStrings.THEME_CHIRAL_COPY))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }

        for (system in Global.getSector().getSystemsWithTag(RATStrings.THEME_CHIRAL_MAIN))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.STABLE_LOCATION || entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }


    }

}