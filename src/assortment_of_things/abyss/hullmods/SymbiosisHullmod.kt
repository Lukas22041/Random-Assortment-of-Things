package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.combat.threat.FragmentSwarmHullmod
import com.fs.starfarer.api.impl.combat.threat.FragmentWeapon
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.ThreatHullmod
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.magiclib.util.MagicIncompatibleHullmods
import kotlin.math.max

class SymbiosisHullmod : BaseHullMod() {


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip.addSpacer(10f)
        tooltip.addPara("A swarm of Threat fragments roils around the ship, providing a regenerating supply of fragments for fragment-based weapons. \n\n" +
                "The base number of fragments is 50/100/200. Every 100 units of hull and armor damage dealt towards opponents or received by the ship itself generate 4 additional replacement fragments. \n\n" +
                "The ship is capable of using its subsystem to turn nearby wrecks, friend or foe, in to 40/80/120/160 additional fragments, based on the hullsize of the targeted ship. Fragments generated this way can temporarily go past the maximum capacity of the ship.\n\n" +
                "The ships sensor profile is reduced by 50%% and damage towards its weapons, engines and any kind of EMP damage is reduced by 25%%. Energy weapons have their range increased by 100 units.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "50", "100", "200",      "100", "hull and armor", "4",      "40", "80", "120", "160",      "50%", "25%", "100")

    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {

        if(stats.getVariant().getHullMods().contains(HullMods.FRAGMENT_SWARM)){
            //if someone tries to install heavy armor, remove it
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                stats.getVariant(),
                HullMods.FRAGMENT_SWARM,
                "rat_abyssal_threat"
            );
        }

        stats.sensorProfile.modifyMult(id, 0.5f)

        stats.empDamageTakenMult.modifyMult(id, 0.75f)
        stats.engineDamageTakenMult.modifyMult(id, 0.75f)
        stats.weaponDamageTakenMult.modifyMult(id, 0.75f)

        stats.energyWeaponRangeBonus.modifyFlat(id, 100f)

    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        if (!ship.hasListenerOfClass(SymbiosisListener::class.java)) {
            ship.addListener(SymbiosisListener(ship))
        }
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }
}

class SymbiosisListener(var ship: ShipAPI) : AdvanceableListener {

    var despawnInterval = IntervalUtil(0.1f, 0.1f)

    override fun advance(amount: Float) {
        if (amount <= 0f) return

        var swarm = RoilingSwarmEffect.getSwarmFor(ship)
        if (swarm == null) {
            swarm = FragmentSwarmHullmod.createSwarmFor(ship)
        }

        if (ship.isFighter) return

        val playerShip = Global.getCurrentState() == GameState.COMBAT && Global.getCombatEngine() != null && Global.getCombatEngine().playerShip === ship

        var sizeToMainain = when(ship.hullSize) {
            ShipAPI.HullSize.CRUISER -> 200
            ShipAPI.HullSize.DESTROYER -> 100
            ShipAPI.HullSize.FRIGATE -> 500
            else -> 0
        }

        //swarm.addMember()

        val params = swarm!!.params
        params.baseMembersToMaintain = sizeToMainain
        params.maxNumMembersToAlwaysRemoveAbove = sizeToMainain
        params.removeMembersAboveMaintainLevel = false
        params.memberRespawnRate = 0f
        params.initialMembers = params.baseMembersToMaintain

        //swarm.addMembers(10)

        //Despawn members, 10 per second, if over the limit
        despawnInterval.advance(amount)
        if (despawnInterval.intervalElapsed()) {
            if (swarm.numActiveMembers > sizeToMainain) {
                swarm.despawnMembers(1)
            }
        }


        if (playerShip) {
            val active = swarm!!.numActiveMembers

            var maxRequired = 0
            for (w in ship.allWeapons) {
                if (w.effectPlugin is FragmentWeapon) {
                    val fw = w.effectPlugin as FragmentWeapon
                    maxRequired = max(maxRequired.toDouble(), fw.numFragmentsToFire.toDouble()).toInt()
                }
            }

            val debuff = active < maxRequired
            Global.getCombatEngine().maintainStatusForPlayerShip(FragmentSwarmHullmod.STATUS_KEY1,
                Global.getSettings().getSpriteName("ui", "icon_tactical_fragment_swarm"),
                "Symbiosis",
                "FRAGMENTS: $active",
                debuff)
        }
    }

}