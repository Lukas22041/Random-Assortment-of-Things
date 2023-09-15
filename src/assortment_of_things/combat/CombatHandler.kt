package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.scripts.AbyssCombatHueApplier
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import org.lazywizard.lazylib.MathUtils


class CombatHandler : EveryFrameCombatPlugin
{


    override fun init(engine: CombatEngineAPI?)  {

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.starSystem ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {
                var data = AbyssUtils.getSystemData(system)

                var color = data.darkColor
                var depth = data.depth
                var darkness = AbyssProcgen.getAbyssDarknessTerrainPlugin(system)
                var background = system.backgroundTextureFilename

                Global.getCombatEngine().addLayeredRenderingPlugin(AbyssCombatHueApplier(color, depth, darkness!!))

                ResetBackgroundScript.resetBackground = true

                if (darkness!!.containsEntity(Global.getSector().playerFleet)) {
                    CombatEngine.getBackground().color = color.darker()
                }
                else {
                    if (depth == AbyssDepth.Shallow) CombatEngine.getBackground().color = color.brighter()
                    if (depth == AbyssDepth.Deep) CombatEngine.getBackground().color = color.brighter().brighter().brighter()
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
            var system = Global.getSector()?.playerFleet?.starSystem ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {

                var data = AbyssUtils.getSystemData(system)
                var depth = data.depth
                var darkness = AbyssProcgen.getAbyssDarknessTerrainPlugin(system)
                if (darkness != null) {

                    if (darkness.containsEntity(Global.getSector().playerFleet))
                    {

                        var path = "graphics/icons/hullsys/high_energy_focus.png"
                        Global.getSettings().loadTexture(path)

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
            }
        }
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}