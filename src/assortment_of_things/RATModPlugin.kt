package assortment_of_things

import ParallelConstruction
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.procgen.AbyssGenerator
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.procgen.AbyssalFleetInflationListener
import assortment_of_things.abyss.scripts.*
import assortment_of_things.abyss.skills.scripts.AbyssalBloodstreamCampaignScript
import assortment_of_things.artifacts.AddArtifactHullmod
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.scripts.ApplyRATControllerToPlayerFleet
import assortment_of_things.campaign.ui.*
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.ExoshipGenerator
import assortment_of_things.exotech.items.ExoProcessor
import assortment_of_things.exotech.scripts.ChangeExoIntelState
import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.relics.RelicsGenerator
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.DropgroupTestSnippet
import assortment_of_things.snippets.ProcgenDebugSnippet
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.JumpPoint
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaRefit.LunaRefitManager
import lunalib.lunaSettings.LunaSettings
import org.dark.shaders.light.LightData
import org.dark.shaders.util.ShaderLib
import org.dark.shaders.util.TextureData
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import javax.swing.text.html.HTML.Tag


class RATModPlugin : BaseModPlugin() {

    companion object {
        var added = false


    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()

       /* Global.getSettings().loadFont("graphics/fonts/monocraft24.fnt")
        Fonts.DEFAULT_SMALL = "graphics/fonts/monocraft24.fnt"*/


        LunaDebug.addSnippet(ProcgenDebugSnippet())
        LunaDebug.addSnippet(DropgroupTestSnippet())

        LootModifier.saveOriginalData()

        LunaSettings.addSettingsListener(RATSettings)

        FrontiersUtils.loadModifiersFromCSV()
        FrontiersUtils.loadFacilitiesFromCSV()

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

        Global.getSector().addTransientScript(ApplyRATControllerToPlayerFleet())

        initFrontiers()

        //Fixes a dumb crash in 0.97 for non-new saves
        for (jumppoint in Global.getSector().hyperspace.jumpPoints) {
            if (jumppoint.hasTag("rat_abyss_entrance") && jumppoint is JumpPointAPI && jumppoint.destinations.isEmpty()) {
                var system = AbyssUtils.getAbyssData().rootSystem

                var fracture = system!!.customEntities.find { it.customPlugin is AbyssalFracture }

                jumppoint.addDestination(JumpPointAPI.JumpDestination(fracture, "Failsafe"))
            }
        }

        //Global.getSector().intelManager.addIntel(DoctrineReportAbyssal())

        //Runcodes.findNearestGravityWell(Global.getSector().playerFleet, Global.getSector().hyperspace)

        var hyperspace = Global.getSector().hyperspace
        if (hyperspace.terrainCopy.none { it.plugin is HyperspaceRenderingTerrainPlugin }) {
            hyperspace.addTerrain("rat_hyperspace_rendering", null)
        }


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

        generateAbyss()

        if (RATSettings.relicsEnabled!! && Global.getSector().memoryWithoutUpdate.get("\$rat_relics_generated") == null) {
            var generator = RelicsGenerator()
            generator.generateStations()
            generator.generateConditions()
        }

        Global.getSector().addTransientScript(ChangeExoIntelState())
        generateExo()

        var bloodstreamScript = Global.getSector().scripts.find { it::class.java == AbyssalBloodstreamCampaignScript::class.java } as AbyssalBloodstreamCampaignScript?
        var skill = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")
        if (bloodstreamScript == null || !bloodstreamScript.shownFirstDialog)  {
            skill!!.name = "Abyssal Bloodstream"
        }
        else {
            skill!!.name = "Abyssal Requiem"
        }

        Global.getSector().addTransientScript(ResetBackgroundScript())

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

    fun generateAbyss() {
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
    }

    fun generateExo() {

        if (RATSettings.exoEnabled!! && Global.getSector().memoryWithoutUpdate.get("\$rat_exo_generated") == null) {
            //DaybreakSystem.generate()

            var data = ExoUtils.getExoData()

            var person = Global.getSector().getFaction("rat_exotech").createRandomPerson(FullName.Gender.FEMALE)
            person.portraitSprite = "graphics/portraits/rat_exo_comm.png"

           // person.name = FullName("Janssen", "", FullName.Gender.FEMALE)
            person.id = "rat_exo_comm"
            person.rankId = "spaceChief"
            person.postId = "rat_exo_comm"

            Global.getSector().importantPeople.addPerson(person)
            data.commPerson = person

            var names = arrayListOf("Nova", "Daybreak", "Aurora")
            for (name in names) {
                var exoship = ExoshipGenerator.generate(name) ?: continue
                data.exoships.add(exoship)
            }

            generateBrokenExoship()

            Global.getSector().memoryWithoutUpdate.set("\$rat_exo_generated", true)
        }
    }

    fun generateBrokenExoship() {
        var location = findBrokenLocation()
        var system = location.orbit.focus.starSystem

        var exoshipEntity = system.addCustomEntity("exoship_${Misc.genUID()}", "Exoship Remains", "rat_exoship_broken", Factions.NEUTRAL)
        exoshipEntity.orbit = location.orbit
    }

    fun findBrokenLocation() : EntityLocation {
        var systems = Global.getSector().starSystems.filter { it.planets.filter { !it.isStar }.isNotEmpty() && it.hasBlackHole() && (it.hasTag(Tags.THEME_RUINS) || it.hasTag(Tags.THEME_MISC)) }
        if (systems.isEmpty()) {
            systems = Global.getSector().starSystems.filter { it.planets.filter { !it.isStar }.isNotEmpty() && (it.hasTag(Tags.THEME_RUINS) || it.hasTag(Tags.THEME_MISC)) }
        }
        if (systems.isEmpty()) {
            systems = Global.getSector().starSystems
        }
        var system = systems.random()

        var location = BaseThemeGenerator.getLocations(Random(), system, MathUtils.getRandomNumberInRange(200f, 300f), linkedMapOf(
            BaseThemeGenerator.LocationType.PLANET_ORBIT to 10f, BaseThemeGenerator.LocationType.NEAR_STAR to 1f, BaseThemeGenerator.LocationType.STAR_ORBIT to 1f)).pick()

        if (location?.orbit == null) {
            location = findBrokenLocation()
        }

        return location
    }

    fun initFrontiers() {

        if (RATSettings.enableFrontiers!!) {
            FrontiersUtils.setFrontiersActive()
        }
    }

    override fun onNewGame() {
        super.onNewGame()

    }

    override fun onNewGameAfterProcGen() {
        generateAbyss()

        if (Global.getSector().characterData.memoryWithoutUpdate.get("\$rat_started_abyss") == true) {
            Global.getSector().memoryWithoutUpdate.set("\$nex_startLocation", "rat_abyss_gate")
        }

    }

    override fun onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad()

        generateExo()

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