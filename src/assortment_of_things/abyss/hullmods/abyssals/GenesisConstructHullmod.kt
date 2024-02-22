package assortment_of_things.abyss.hullmods.abyssals

import activators.ActivatorManager
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.activators.PerseveranceActivator
import assortment_of_things.abyss.boss.GenesisBossScript
import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.abyss.shipsystem.activators.PrimordialSeaActivator
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.util.*

class GenesisConstructHullmod : BaseHullMod() {



    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (stats!!.fleetMember?.fleetData?.fleet?.faction?.id == "rat_abyssals_primordials") {
            stats.crewLossMult.modifyMult("test", 0f)
            stats.crLossPerSecondPercent.modifyMult("test", 0f)
        }

        stats.energyWeaponDamageMult.modifyMult(id, 1.2f)
        stats.energyRoFMult.modifyMult(id, 1.2f)
        stats!!.energyWeaponFluxCostMod.modifyMult(id, 0.8f)

        stats!!.energyWeaponRangeBonus.modifyFlat(id, 200f)

        stats.empDamageTakenMult.modifyMult(id, 0.5f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship.shield != null) {
            ship.shield.setRadius(ship.shieldRadiusEvenIfNoShield, "graphics/fx/rat_primordial_shields256.png", "graphics/fx/rat_primordial_shields256ring.png")
        }
        ship.addListener(GenesisAfterImageRenderer(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }

    class GenesisAfterImageRenderer(var ship: ShipAPI) : AdvanceableListener {

        var color = AbyssUtils.GENESIS_COLOR
        var afterimageInterval = IntervalUtil(0.1f, 0.1f)

        override fun advance(amount: Float) {
            /*afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused && ship!!.isAlive)
            {
                var image = AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(50), color.setAlpha(0), 1f, 0f, Vector2f().plus(ship!!.location))
            }*/
        }

    }
}