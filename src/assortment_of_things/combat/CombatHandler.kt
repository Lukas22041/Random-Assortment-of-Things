package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.combat.*
import assortment_of_things.abyss.entities.light.*
import assortment_of_things.abyss.entities.primordial.PrimordialPhotosphere
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.abyss.procgen.biomes.SeaOfSolitude
import assortment_of_things.backgrounds.neural.NeuralShardScript
import assortment_of_things.backgrounds.zero_day.ZeroDayScript
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.escort.EscortOrdersManager
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.campaign.WarpingSpriteRenderer
import exerelin.campaign.backgrounds.CharacterBackgroundUtils
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setBrightness


class CombatHandler : EveryFrameCombatPlugin
{


    override fun init(engine: CombatEngineAPI)  {

        //Global.getCombatEngine().addLayeredRenderingPlugin(ShadedSphere())

        if (RATSettings.enableAbyss!! && Global.getCurrentState() == GameState.TITLE) {

          /*  if (RATModPlugin.isHalloween *//*&& !engine.isSimulation*//*) {
                engine.addPlugin(AbyssSpecialTitleScreen())
            }*/
           /* else if (RATModPlugin.gameStartedForTitleScene) {
                RATModPlugin.gameStartedForTitleScene = false

                var random = MathUtils.getRandomNumberInRange(1, 100)

                if (random == 1) {
                    engine.addPlugin(AbyssTitleScreen())
                }

            }*/
        }

       /* engine!!.addPlugin(object : BaseEveryFrameCombatPlugin() {
            var played = false
            override fun advance(amount: Float, events: List<InputEventAPI>) {
                if (played || engine.isPaused()) return
                if (engine.getTotalElapsedTime(false) > 1f) {
                    try {
                        Global.getSoundPlayer().playCustomMusic(1, 0, "rat_test", true)
                    } catch (e: Exception) {
                        Global.getLogger(this.javaClass).error("Failed to play music set rat_test", e)
                    }
                    played = true
                }
            }
        })*/

        /*if (RATSettings.enableDPS!! && Global.getCurrentState() != GameState.TITLE && engine.isSimulation) {
            engine.addPlugin(DPSMeter())
        }*/

       /* if (ChangeMainMenuColorScript.isInAbyss && Global.getCurrentState() == GameState.TITLE && !RATModPlugin.isHalloween) {
            //Global.getCombatEngine().backgroundColor = ChangeMainMenuColorScript.lastAbyssColor
            engine.addPlugin(AbyssTitleScreen())
            ChangeMainMenuColorScript.isInAbyss = false

        }*/



     //   Global.getCombatEngine().setPlayerShipExternal(ship)

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {

            Global.getCombatEngine().addPlugin(EscortOrdersManager())

            if (Global.getSettings().modManager.isModEnabled("nexerelin")) {
                if (CharacterBackgroundUtils.isBackgroundActive("rat_neural_shard")) {
                    engine.addPlugin(NeuralShardScript())
                }

                if (CharacterBackgroundUtils.isBackgroundActive("rat_zero_day")) {
                    engine.addPlugin(ZeroDayScript())
                }
            }

            if (AbyssUtils.isPlayerInAbyss()) {
                initAbyss()
            }

            /*var system = Global.getSector()?.playerFleet?.starSystem ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {
                var data = AbyssUtils.getSystemData(system)

                var color = data.getDarkColor()
                var depth = data.depth
                var darkness = AbyssProcgen.getAbyssDarknessTerrainPlugin(system)
                var background = system.backgroundTextureFilename

                Global.getCombatEngine().addLayeredRenderingPlugin(AbyssCombatHueApplier(color, depth, darkness!!))

                if (data.system.hasTag(IonicStormAbyssType.STORM_TAG)) {
                    Global.getCombatEngine().addLayeredRenderingPlugin(IonicStormCombatRenderer())
                }

                ResetBackgroundScript.resetBackground = true

               *//* if (darkness!!.containsEntity(Global.getSector().playerFleet)) {
                    CombatEngine.getBackground().color = color.darker()
                }
                else {
                    if (depth == AbyssDepth.Shallow) CombatEngine.getBackground().color = color.brighter()
                    if (depth == AbyssDepth.Deep) CombatEngine.getBackground().color = color.brighter().brighter().brighter()
                }*//*

               // Global.getCombatEngine().isRenderStarfield = false
                if (darkness!!.containsEntity(Global.getSector().playerFleet)) {
                    Global.getCombatEngine().backgroundColor = color.darker()
                }
                else {
                    if (depth == AbyssDepth.Shallow)  Global.getCombatEngine().backgroundColor = color.brighter()
                    if (depth == AbyssDepth.Deep)  Global.getCombatEngine().backgroundColor = color.brighter().brighter().brighter()
                }

                var withinPhotosphere = false
                var photosphere: SectorEntityToken? = null
                var photospheres = Global.getSector().playerFleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
                for (source in photospheres)
                {
                    var plugin = source.customPlugin as AbyssalPhotosphere
                    if (MathUtils.getDistance(source.location, Global.getSector().playerFleet.location) < (plugin.radius / 10) - 10)
                    {
                        withinPhotosphere = true
                        photosphere = source
                        break
                    }
                }

                //engine!!.addLayeredRenderingPlugin(CombatWarpingBackgroundRenderer(background, color))

                if (withinPhotosphere && photosphere != null) {
                    engine!!.addLayeredRenderingPlugin(CombatPhotosphereRenderer(150f, photosphere))
                }

                var collosal = system.planets.find { it.spec.planetType == "rat_colossal_photosphere" }
                if (collosal != null) {
                    if (MathUtils.getDistance(collosal, Global.getSector().playerFleet.location) <= 9000) {
                        engine!!.addLayeredRenderingPlugin(CombatColossalPhotosphereRenderer(350f, collosal as PlanetAPI))

                    }
                }



            }
*/        }
    }

    fun initAbyss() {
        var engine = Global.getCombatEngine()

        var data = AbyssUtils.getData()
        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()

        var darkness = data.darknessTerrain ?: return
        var lightLevel = 1-darkness.getLightlevel(Global.getSector().playerFleet) //1 If full brightness
        var darknessLevel = darkness.getDarknessMult() //Biome dependent darkness mult

        var currentColor = manager.getCurrentBiomeColor()
        var currentDarkColor = manager.getCurrentDarkBiomeColor()
        var currentBackgroundColor = manager.getCurrentBackgroundColor()
        var currentLightColor = manager.getCurrentSystemLightColor()

        //Background
        var backgroundBrightness = 40
        backgroundBrightness += (75 * lightLevel * darknessLevel).toInt()
        if (dominant is PrimordialWaters) {
            backgroundBrightness -= 20
            currentDarkColor = currentDarkColor.darker()
        }

        var background = currentBackgroundColor.setBrightness(backgroundBrightness)

        Global.getCombatEngine().backgroundColor = background

        //Background Warper
        var warper = CombatBackgroundWarper(8, 0.25f)
        ReflectionUtils.setFieldOfType(WarpingSpriteRenderer::class.java, Global.getCombatEngine(), warper)
        warper.overwriteColor = background


        //Hue
        engine.addLayeredRenderingPlugin(AbyssCombatHueApplier(currentDarkColor, lightLevel, darknessLevel))

        //Sea of Solitude rendering
        if (dominant is SeaOfSolitude) {
            Global.getCombatEngine().addLayeredRenderingPlugin(SolitudeStormCombatRenderer(dominant))
            Global.getCombatEngine().addLayeredRenderingPlugin(SolitudeStormParticleCombatRenderer(dominant.getParticleColor(), dominant.getDarkBiomeColor()))
        }

        //Display Lightsources in combat
        var lightSource: SectorEntityToken? = null
        var lightSources = Global.getSector().playerFleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalLight }
        for (source in lightSources)
        {
            var plugin = source.customPlugin as AbyssalLight
            if (MathUtils.getDistance(source.location, Global.getSector().playerFleet.location) < (plugin.radius / 10) - 10)
            {
                if (plugin is AbyssalPhotosphere || plugin is AbyssalBeacon || plugin is AbyssalDecayingPhotosphere || plugin is AbyssalColossalPhotosphere || plugin is PrimordialPhotosphere) {
                    lightSource = source
                }
                break
            }
        }

        if (lightSource != null) {
            var plugin = lightSource.customPlugin

            if (plugin is AbyssalPhotosphere) {
                engine!!.addLayeredRenderingPlugin(CombatPhotosphereRenderer(lightSource))
            }

            if (plugin is AbyssalColossalPhotosphere) {
                engine!!.addLayeredRenderingPlugin(CombatColossalPhotosphereRenderer(lightSource))
            }

            if (plugin is AbyssalDecayingPhotosphere) {
                engine!!.addLayeredRenderingPlugin(CombatDecayingPhotosphereRenderer(lightSource))
            }

            if (plugin is PrimordialPhotosphere) {
                var biome = AbyssUtils.getBiomeManager().getBiome(PrimordialWaters::class.java) as PrimordialWaters
                if (biome.getLevel() != 0f) {
                    engine!!.addLayeredRenderingPlugin(CombatPrimordialPhotosphereRenderer(lightSource))
                }
            }

            if (plugin is AbyssalBeacon) {
                engine!!.addLayeredRenderingPlugin(CombatBeaconRenderer(lightSource, currentLightColor))
            }
        }



        var levels = manager.getBiomeLevels()
        var saturation = levels.map { it.key.getSaturation() * it.value }.sum()

        engine.customData.set("rat_current_abyss_biome", dominant)
        engine.customData.set("rat_current_abyss_saturation", saturation)
    }


    fun advanceAbyss(amount: Float) {
        var asteroids = ArrayList(Global.getCombatEngine().asteroids)
        asteroids.forEach { Global.getCombatEngine().removeEntity(it) }

        var saturation = Global.getCombatEngine().customData.get("rat_current_abyss_saturation") as Float?
        if (saturation != null) {
            PostProcessShader.setSaturation(false, saturation)
        }

    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
    }

    /*var replacements = ArrayList<ShadedShipSpriteTest>()
    var radius = 0f*/

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?)
    {

      /*  for (ship in Global.getCombatEngine().ships) {
            if (!ship.hasTag("sprite_replaced2")) {
                ship.addTag("sprite_replaced2")
                var shaded = ThreatFragmentShader(ship)
                shaded.init()
            }
        }*/

       /* for (ship in Global.getCombatEngine().ships) {
            if (!ship.hasTag("sprite_replaced")) {
                ship.addTag("sprite_replaced")
                var shaded = ShadedShipSpriteTest(ship)
                shaded.init()
                replacements.add(shaded)
            }
        }

        radius += 500 * amount
        var baseOffset = 900f
        var min = 0 - baseOffset + radius
        var max = 0 + radius


        if (radius >= baseOffset * 2) radius = 0f

        for (replacement in replacements) {
            replacement.minRadius = min
            replacement.maxRadius = max
        }*/







        /*if (ArtifactUtils.getActiveArtifact() != null)
        {
            ArtifactUtils.getActivePlugin()!!.advanceInCombat(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
        }*/

       /* var playership = Global.getCombatEngine().playerShip
        var ships = Global.getCombatEngine().ships
        for (ship in ships) {
            if (playership == null) break
            if (ship == playership) continue
            ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 1f, playership)
        }*/



        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.starSystem ?: return

            if (AbyssUtils.isPlayerInAbyss()) {
                advanceAbyss(amount)
            }

            /*if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {

                var data = AbyssUtils.getSystemData(system)
                var depth = data.depth
                var darkness = AbyssProcgen.getAbyssDarknessTerrainPlugin(system)
                var path = "graphics/icons/hullsys/high_energy_focus.png"

                if (darkness != null) {

                    if (darkness.getDarknessMult() >= 0.9)
                    {

                        var path = "graphics/icons/hullsys/high_energy_focus.png"
                        Global.getSettings().getAndLoadSprite(path)

                        if (depth == AbyssDepth.Shallow) {
                            Global.getCombatEngine().maintainStatusForPlayerShip("rat_darkness",
                                path,
                                "Darkness",
                                "15% reduced vision range",
                                true)

                            for (ship in Global.getCombatEngine().ships) {
                                ship.mutableStats.sightRadiusMod.modifyMult("rat_darkness", 0.85f)
                            }
                        }

                        if (depth == AbyssDepth.Deep) {

                            Global.getCombatEngine().maintainStatusForPlayerShip("rat_darkness",
                                path,
                                "Extreme Darkness",
                                "30% reduced vision range",
                                true)

                            for (ship in Global.getCombatEngine().ships) {
                                ship.mutableStats.sightRadiusMod.modifyMult("rat_darkness", 0.70f)
                            }
                        }
                    }
                }

                if (system.hasTag(IonicStormAbyssType.STORM_TAG)) {
                    for (ship in Global.getCombatEngine().ships) {

                        Global.getCombatEngine().maintainStatusForPlayerShip("rat_ionic1",
                            path,
                            "Ionic Storm",
                            "10% increased energy weapon damage",
                            true)

                        Global.getCombatEngine().maintainStatusForPlayerShip("rat_ionic2",
                            path,
                            "Ionic Storm",
                            "15% reduced shield efficiency",
                            true)

                        Global.getCombatEngine().maintainStatusForPlayerShip("rat_ionic3",
                            path,
                            "Ionic Storm",
                            "20% more emp damage taken",
                            true)

                        ship.mutableStats.energyWeaponDamageMult.modifyMult("rat_ionicstorm", 1.10f)
                        ship.mutableStats.shieldDamageTakenMult.modifyMult("rat_ionicstorm", 1.15f)
                        ship.mutableStats.empDamageTakenMult.modifyMult("rat_ionicstorm", 1.20f)
                    }
                }
            }*/
        }
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}