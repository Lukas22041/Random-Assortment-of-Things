package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.scripts.AbyssCombatHueApplier
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.modular_weapons.scripts.ModularWeaponCombatHandler
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f


class CombatHandler : EveryFrameCombatPlugin
{

    var modularHandler = ModularWeaponCombatHandler()

    override fun init(engine: CombatEngineAPI?)  {

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.containingLocation ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {
                var color = AbyssUtils.getSystemColor(system)
                var tier = AbyssUtils.getTier(system)
                var darkness = AbyssUtils.getAbyssDarknessTerrainPlugin(system)
                var background = system.backgroundTextureFilename

                Global.getCombatEngine().addLayeredRenderingPlugin(AbyssCombatHueApplier(color, tier, darkness!!))

                ResetBackgroundScript.resetBackground = true



                if (darkness!!.containsEntity(Global.getSector().playerFleet) || tier == AbyssProcgen.Tier.Low) {
                    CombatEngine.getBackground().color = color.darker()
                }
                else {
                    if (tier == AbyssProcgen.Tier.Mid) CombatEngine.getBackground().color = color.brighter()
                    if (tier == AbyssProcgen.Tier.High) CombatEngine.getBackground().color = color.brighter().brighter().brighter()

                }

               /* if (darkness!!.containsEntity(Global.getSector().playerFleet) || tier == AbyssProcgen.Tier.Low) {
                    color = color.darker()
                }
                else {
                    if (tier == AbyssProcgen.Tier.Mid) color = color.brighter()
                    if (tier == AbyssProcgen.Tier.High) color = color.brighter().brighter().brighter()

                }*/


                var withinPhotosphere = false
                var photospheres = Global.getSector().playerFleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalPhotosphere }
                for (source in photospheres)
                {
                    var plugin = source.customPlugin as AbyssalPhotosphere
                    if (MathUtils.getDistance(source.location, Global.getSector().playerFleet.location) < (plugin.radius / 10) - 10)
                    {
                        withinPhotosphere = true
                        break
                    }
                }

                //engine!!.addLayeredRenderingPlugin(CombatWarpingBackgroundRenderer(background, color))

                if (withinPhotosphere) {
                    engine!!.addLayeredRenderingPlugin(CombatPhotosphereRenderer(150f))
                }




            }
        }
    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?)
    {
        /*if (ArtifactUtils.getActiveArtifact() != null)
        {
            ArtifactUtils.getActivePlugin()!!.advanceInCombat(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
        }*/

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.containingLocation ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {

                var tier = AbyssUtils.getTier(system)
                var darkness = AbyssUtils.getAbyssDarknessTerrainPlugin(system)
                if (darkness != null) {

                    if (darkness.containsEntity(Global.getSector().playerFleet))
                    {

                        var path = "graphics/icons/hullsys/high_energy_focus.png"
                        Global.getSettings().loadTexture(path)

                        if (tier == AbyssProcgen.Tier.Mid) {
                            Global.getCombatEngine().maintainStatusForPlayerShip("rat_darkness",
                                path,
                                "Darkness",
                                "15% reduced vision range",
                                true)

                            for (ship in Global.getCombatEngine().ships) {
                                ship.mutableStats.sightRadiusMod.modifyMult("rat_darkness", 0.85f)
                            }
                        }

                        if (tier == AbyssProcgen.Tier.High) {

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
            }
        }



        modularHandler.advance(amount)
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}