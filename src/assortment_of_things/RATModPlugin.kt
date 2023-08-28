package assortment_of_things

import ParallelConstruction
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssalFleetInflationListener
import assortment_of_things.abyss.scripts.DisableTransverseScript
import assortment_of_things.abyss.scripts.ForceNegAbyssalRep
import assortment_of_things.abyss.scripts.HullmodRemoverListener
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.abyss.systems.MidnightCoreSystem
import assortment_of_things.abyss.systems.SingularityCrateGeneration
import assortment_of_things.artifacts.AddArtifactHullmod
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.*
import assortment_of_things.misc.RATSettings
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.DropgroupTestSnippet
import assortment_of_things.snippets.ProcgenDebugSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Items
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

        //Global.getSector().characterData.person.stats.setSkillLevel("rat_augmented", 1f)
      //  Global.getSector().playerFleet.fleetData.officersCopy.random().person.stats.setSkillLevel("rat_augmented", 1f)


      /*  Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_destabilizer", null), 1f)
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
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_supplies", null), 5f)*/

        if (RATSettings.enableAbyss!!)
        {
            if (AbyssUtils.getAllAbyssSystems().isEmpty()) {

                var data = Global.getSector().characterData

                if (data.memoryWithoutUpdate.get("\$rat_abyssWithCustomStart") == null) {
                    var cache = SingularityCrateGeneration.generate()
                }

                MidnightCoreSystem().generate()

               /* Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
                Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_singularity_jump_ability", true, 0f);*/

                Global.getSector().addScript(DisableTransverseScript())
                for (faction in Global.getSector().allFactions)
                {
                    if (faction.id == AbyssUtils.FACTION_ID) continue
                    faction.adjustRelationship(AbyssUtils.FACTION_ID, -100f)
                }

                var random = Random(Misc.genRandomSeed())
                Global.getSector().memoryWithoutUpdate.set("\$rat_alteration_random", random)
            }
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

    }



    override fun beforeGameSave() {
        super.beforeGameSave()

        for (system in Global.getSector().starSystems.filter { it.hasTag(AbyssUtils.SYSTEM_TAG) })
        {
            var abyssPlugin = AbyssUtils.getAbyssTerrainPlugin(system)
            if (abyssPlugin != null)
            {
                abyssPlugin.save()
            }
            var superchargedAbyss = AbyssUtils.getSuperchargedAbyssTerrainPlugin(system)
            if (superchargedAbyss != null)
            {
                superchargedAbyss.save()
            }
        }
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

    }
}