package assortment_of_things.combat

import assortment_of_things.RATModPlugin
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.backgrounds.neural.NeuralShardScript
import assortment_of_things.backgrounds.zero_day.ZeroDayScript
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import exerelin.campaign.backgrounds.CharacterBackgroundUtils
import org.lazywizard.lazylib.MathUtils


class CombatHandler : EveryFrameCombatPlugin
{


    override fun init(engine: CombatEngineAPI)  {


        if (RATSettings.enableAbyss!! && Global.getCurrentState() == GameState.TITLE) {
            if (RATModPlugin.gameStartedForTitleScene) {
                RATModPlugin.gameStartedForTitleScene = false

                var random = MathUtils.getRandomNumberInRange(1, 100)


            }
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


     //   Global.getCombatEngine().setPlayerShipExternal(ship)

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {

            if (Global.getSettings().modManager.isModEnabled("nexerelin")) {
                if (CharacterBackgroundUtils.isBackgroundActive("rat_neural_shard")) {
                    engine.addPlugin(NeuralShardScript())
                }

                if (CharacterBackgroundUtils.isBackgroundActive("rat_zero_day")) {
                    engine.addPlugin(ZeroDayScript())
                }
            }

            var system = Global.getSector()?.playerFleet?.starSystem ?: return

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

        }
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}