package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.scripts.AbyssCombatHueApplier
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.modular_weapons.scripts.ModularWeaponCombatHandler
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.FleetMemberDeploymentListener
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine


class CombatHandler : EveryFrameCombatPlugin
{

    var modularHandler = ModularWeaponCombatHandler()

    override fun init(engine: CombatEngineAPI?)  {
/*
        if (ArtifactUtils.getActiveArtifact() != null)
        {
            var plugin =  ArtifactUtils.getActivePlugin()!!
            plugin.onCombatStart(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
            plugin.onInstall(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)

            Global.getCombatEngine().listenerManager.addListener( object : FleetMemberDeploymentListener {
                override fun reportFleetMemberDeployed(member: DeployedFleetMemberAPI?) {

                    if (Global.getSector().playerFleet.fleetData.membersListCopy.contains(member!!.member))
                    {
                        plugin.applyToMembers(member.member, ArtifactUtils.STAT_MOD_ID)
                    }
                }
            })
        }*/


        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.containingLocation ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {
                var color = AbyssUtils.getSystemColor(system)
                var tier = AbyssUtils.getTier(system)

                Global.getCombatEngine().addLayeredRenderingPlugin(AbyssCombatHueApplier(color, tier))

                ResetBackgroundScript.resetBackground = true
                CombatEngine.getBackground().color = color.darker()
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

        modularHandler.advance(amount)
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}