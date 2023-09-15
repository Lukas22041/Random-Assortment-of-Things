package assortment_of_things

import ParallelConstruction
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssGenerator
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.procgen.AbyssalFleetInflationListener
import assortment_of_things.abyss.scripts.ForceNegAbyssalRep
import assortment_of_things.abyss.scripts.HullmodRemoverListener
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.artifacts.AddArtifactHullmod
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.*
import assortment_of_things.misc.RATSettings
import assortment_of_things.relics.RelicsGenerator
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.DropgroupTestSnippet
import assortment_of_things.snippets.ProcgenDebugSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaRefit.LunaRefitManager
import lunalib.lunaSettings.LunaSettings
import org.dark.shaders.light.LightData
import org.dark.shaders.util.ShaderLib
import org.dark.shaders.util.TextureData
import java.util.*


class RATModPlugin : BaseModPlugin() {

    companion object {
        var added = false

        init {
            //Global.getSettings().isDevMode = true
        }
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        LunaDebug.addSnippet(ProcgenDebugSnippet())
        LunaDebug.addSnippet(DropgroupTestSnippet())

        LootModifier.saveOriginalData()

        LunaSettings.addSettingsListener(RATSettings)

        ArtifactUtils.loadArtifactsFromCSV()

        LunaRefitManager.addRefitButton(AlterationRefitButton())

        LunaRefitManager.addRefitButton(CrewConversionChronosRefitButton())
        LunaRefitManager.addRefitButton(CrewConversionCosmosRefitButton())
        LunaRefitManager.addRefitButton(CrewConversionRemoveIntegratedRefitButton())

        LunaRefitManager.addRefitButton(DeltaAIRefitButton())

        LunaRefitManager.addRefitButton(AugmentedRefitButton())

        if (Global.getSettings().modManager.isModEnabled("nexerelin")) {
            //RATNexManager.addStartingFleets()
        }

        if (Global.getSettings().modManager.isModEnabled("shaderLib")) {
            ShaderLib.init()
            LightData.readLightDataCSV("data/config/rat_lights_data.csv");
            TextureData.readTextureDataCSV("data/config/rat_texture_data.csv")
        }
    }

    override fun onDevModeF8Reload() {

        if (Global.getSettings().modManager.isModEnabled("shaderLib")) {
            LightData.readLightDataCSV("data/config/rat_lights_data.csv");
        }
    }


    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)



        for (artifact in ArtifactUtils.artifacts)
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_artifact", artifact.id), 1f)
        }

        for (hullmod in Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && !it.hasTag("rat alteration_no_drop") })
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", hullmod.id), 3f)
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", "rat_primordial_stream"), 3f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_remover", null), 5f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 10f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_discovery", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_hostility", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_supplies", null), 5f)



        if (RATSettings.enableAbyss!!)
        {
            if (AbyssUtils.getAbyssData().systemsData.isEmpty()) {
                for (faction in Global.getSector().allFactions)
                {
                    if (faction.id == AbyssUtils.FACTION_ID) continue
                    faction.adjustRelationship(AbyssUtils.FACTION_ID, -100f)
                }

                var random = Random(Misc.genRandomSeed())
                Global.getSector().memoryWithoutUpdate.set("\$rat_alteration_random", random)

                AbyssGenerator().beginGeneration()
            }
        }

        if (RATSettings.relicsEnabled!! && Global.getSector().memoryWithoutUpdate.get("\$rat_relics_generated") == null) {
            RelicsGenerator().generate()
        }




        if (!Global.getSector().hasScript(ResetBackgroundScript::class.java)) {
            Global.getSector().addTransientScript(ResetBackgroundScript())
        }

        Global.getSector().listenerManager.addListener(AbyssalFleetInflationListener(), true)
        Global.getSector().addTransientScript(ForceNegAbyssalRep())
        Global.getSector().addTransientListener(HullmodRemoverListener())

        Global.getSector().addTransientScript(AddArtifactHullmod())

        if (RATSettings.enableMinimap!!){
            Global.getSector().addTransientScript(MinimapUI())
        }

        LootModifier.modifySpawns()

        Global.getSector().registerPlugin(RATCampaignPlugin())
        Global.getSector().addTransientScript(ParallelConstruction())

        Global.getSector().addTransientListener(AtMarketListener())

        if (RATSettings.disableHelp!!)
        {
            try {
                CampaignEngine.getInstance().campaignHelp.isEnabled = false
            }
            catch (e: Throwable)
            {
                Global.getLogger(this.javaClass).error("Failed to disable help pop-ups.")
            }
        }
    }

    override fun onNewGame() {
        super.onNewGame()
    }

    override fun onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad()

     /*   //Exoship test
        var exoshipSystem = Global.getSector().starSystems.filter { it.planets.any { planet -> !planet.isStar } }.random()
        var location = BaseThemeGenerator.getLocations(Random(), exoshipSystem, 100f, linkedMapOf(LocationType.PLANET_ORBIT to 100f)).pick()
        var ship = exoshipSystem.addCustomEntity("exoship_${Misc.genUID()}", "Exoship", "rat_exoship", Factions.NEUTRAL)
        ship.orbit = location.orbit

        //Exospace
        var system = Global.getSector().createStarSystem("Exospace")
        system.backgroundTextureFilename = "graphics/backgrounds/exo/exospace.jpg"
        system.initNonStarCenter()
        system.generateAnchorIfNeeded()
        system.addTag(Tags.THEME_HIDDEN)
        AbyssBackgroundWarper(system, 16, 1f)*/

    }

    override fun beforeGameSave() {
        super.beforeGameSave()

        for (system in Global.getSector().starSystems.filter { it.hasTag(AbyssUtils.SYSTEM_TAG) })
        {
            var abyssPlugin = AbyssProcgen.getAbyssTerrainPlugin(system)
            if (abyssPlugin != null)
            {
                abyssPlugin.save()
            }
        }
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

    }
}