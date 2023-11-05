package assortment_of_things

import ParallelConstruction
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssGenerator
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.procgen.AbyssalFleetInflationListener
import assortment_of_things.abyss.scripts.*
import assortment_of_things.abyss.skills.scripts.AbyssalBloodstreamCampaignScript
import assortment_of_things.artifacts.AddArtifactHullmod
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.*
import assortment_of_things.exotech.systems.DaybreakSystem
import assortment_of_things.misc.RATSettings
import assortment_of_things.relics.RelicsGenerator
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.DropgroupTestSnippet
import assortment_of_things.snippets.ProcgenDebugSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
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

        var isHalloween = false

        init {
            //Global.getSettings().isDevMode = true
        }
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        val currentDate = Date()
        //var currentDate = Date(1698530401L * 1000)
        val startDate = Date(1698530400L * 1000)
        val endDate = Date(1698793200L * 1000)
        if (startDate.before(currentDate) && endDate.after(currentDate)) {
            isHalloween = true
        }

        LunaDebug.addSnippet(ProcgenDebugSnippet())
        LunaDebug.addSnippet(DropgroupTestSnippet())

        LootModifier.saveOriginalData()

        LunaSettings.addSettingsListener(RATSettings)

        ArtifactUtils.loadArtifactsFromCSV()

        LunaRefitManager.addRefitButton(AlterationRefitButton())

        LunaRefitManager.addRefitButton(CrewConversionChronosRefitButton())
        LunaRefitManager.addRefitButton(CrewConversionCosmosRefitButton())
        LunaRefitManager.addRefitButton(CrewConversionSeraphRefitButton())
        LunaRefitManager.addRefitButton(CrewConversionRemoveIntegratedRefitButton())

        LunaRefitManager.addRefitButton(DeltaAIRefitButton())

        LunaRefitManager.addRefitButton(CyberneticInterfaceRefitButton())

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

        //Global.getSector().intelManager.addIntel(DoctrineReportAbyssal())



   /*     for (artifact in ArtifactUtils.artifacts)
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_artifact", artifact.id), 1f)
        }

        for (hullmod in Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && !it.hasTag("rat alteration_no_drop") })
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", hullmod.id), 3f)
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", "rat_primordial_stream"), 3f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", "rat_upscale_protocol"), 3f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 30f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_charged_forge", null), 5f)

      //  Global.getSector().playerFleet.fleetData.officersCopy.random().person.stats.setSkillLevel("rat_auto_engineer", 1f)*/


        Global.getSector().addTransientScript(AbyssAmbientSoundPlayer())
        Global.getSector().addTransientListener(AbyssDoctrineListener(false))
        Global.getSector().listenerManager.addListener(AbyssalFleetInflationListener(), true)
        if (RATSettings.enableAbyss!!)
        {
            if (AbyssUtils.getAbyssData().systemsData.isEmpty()) {
                for (faction in Global.getSector().allFactions)
                {
                    if (faction.id == "rat_abyssals" || faction.id == "rat_abyssals_deep") continue
                    faction.adjustRelationship("rat_abyssals", -100f)
                    faction.adjustRelationship("rat_abyssals_deep", -100f)
                }

                var random = Random(Misc.genRandomSeed())
                Global.getSector().memoryWithoutUpdate.set("\$rat_alteration_random", random)

                AbyssGenerator().beginGeneration()
            }
        }

        if (RATSettings.relicsEnabled!! && Global.getSector().memoryWithoutUpdate.get("\$rat_relics_generated") == null) {
            var generator = RelicsGenerator()
            generator.generateStations()
            generator.generateConditions()
        }

        generateExo()

        var bloodstreamScript = Global.getSector().scripts.find { it::class.java == AbyssalBloodstreamCampaignScript::class.java } as AbyssalBloodstreamCampaignScript?
        var skill = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")
        if (bloodstreamScript == null || !bloodstreamScript.shownFirstDialog)  {
            skill!!.name = "Abyssal Bloodstream"
        }
        else {
            skill!!.name = "Abyssal Requiem"
        }

        if (!Global.getSector().hasScript(ResetBackgroundScript::class.java)) {
            Global.getSector().addTransientScript(ResetBackgroundScript())
        }

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

    fun generateExo() {
       /* if (Global.getSector().memoryWithoutUpdate.get("\$rat_nova_generated") == null) {
            DaybreakSystem.generate()

            Global.getSector().memoryWithoutUpdate.set("\$rat_nova_generated", true)
        }*/
    }

    override fun onNewGame() {
        super.onNewGame()

        generateExo()
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